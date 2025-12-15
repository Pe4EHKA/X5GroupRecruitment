package com.x5.recruitment.application.service;

import com.x5.recruitment.api.dto.ApplicationDto;
import com.x5.recruitment.api.dto.HmDecisionRequest;
import com.x5.recruitment.domain.model.*;
import com.x5.recruitment.domain.repository.ApplicationRepository;
import com.x5.recruitment.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Hiring Manager operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HmService {

    private final ApplicationRepository applicationRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Make decision on application with structured feedback.
     */
    public ApplicationDto makeDecision(Long applicationId, HmDecisionRequest request, User hiringManager) {
        log.info("HM decision for application {}: approved={}", applicationId, request.getApproved());
        
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        // Create feedback
        Feedback feedback = Feedback.builder()
            .application(application)
            .author(hiringManager)
            .rating(request.getRating())
            .strengths(request.getStrengths())
            .weaknesses(request.getWeaknesses())
            .recommendation(request.getRecommendation())
            .generalComments(request.getGeneralComments())
            .shareWithCandidate(request.getShareWithCandidate())
            .build();

        application.addFeedback(feedback);

        // Change status based on decision
        ApplicationStatus newStatus = request.getApproved() ? 
            ApplicationStatus.APPROVED : ApplicationStatus.REJECTED;
        
        application.changeStatus(newStatus, hiringManager, "HM decision");

        application = applicationRepository.save(application);

        // Send notification
        String notificationSubject = request.getApproved() ? 
            "Congratulations! Your application has been approved" :
            "Application Status Update";
        
        String notificationBody = request.getApproved() ?
            "Your application for " + application.getVacancy().getTitle() + " has been approved!" :
            "Thank you for your interest in " + application.getVacancy().getTitle() + ". " +
            "After careful consideration, we have decided to move forward with other candidates.";

        createNotification(application, 
            request.getApproved() ? NotificationType.APPROVED : NotificationType.REJECTED,
            notificationSubject, notificationBody);

        log.info("HM decision processed for application {}", applicationId);
        
        return mapToDto(application);
    }

    private void createNotification(Application application, NotificationType type, 
                                   String subject, String body) {
        Notification notification = Notification.builder()
            .candidate(application.getCandidate())
            .application(application)
            .type(type)
            .subject(subject)
            .body(body)
            .build();
        
        notificationRepository.save(notification);
    }

    private ApplicationDto mapToDto(Application application) {
        return ApplicationDto.builder()
            .id(application.getId())
            .candidateId(application.getCandidate().getId())
            .candidateName(application.getCandidate().getFullName())
            .candidateEmail(application.getCandidate().getEmail())
            .vacancyId(application.getVacancy().getId())
            .vacancyTitle(application.getVacancy().getTitle())
            .status(application.getStatus())
            .coverLetter(application.getCoverLetter())
            .notes(application.getNotes())
            .assignedRecruiterId(application.getAssignedRecruiter() != null ? 
                application.getAssignedRecruiter().getId() : null)
            .assignedRecruiterName(application.getAssignedRecruiter() != null ? 
                application.getAssignedRecruiter().getFullName() : null)
            .screeningScore(application.getScreeningScore())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .build();
    }
}
