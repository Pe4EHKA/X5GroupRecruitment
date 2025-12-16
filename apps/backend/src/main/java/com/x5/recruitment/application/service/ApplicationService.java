package com.x5.recruitment.application.service;

import com.x5.recruitment.api.dto.*;
import com.x5.recruitment.domain.model.*;
import com.x5.recruitment.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing applications and candidate workflow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final VacancyRepository vacancyRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Create new application (with candidate deduplication).
     */
    public ApplicationDto createApplication(CreateApplicationRequest request) {
        log.info("Creating application for email: {}", request.getEmail());
        
        // Find or create candidate
        Candidate candidate = candidateRepository.findByEmail(request.getEmail())
            .orElseGet(() -> {
                Candidate newCandidate = Candidate.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .additionalInfo(request.getAdditionalInfo())
                    .accessToken(UUID.randomUUID().toString())
                    .build();
                return candidateRepository.save(newCandidate);
            });

        // Find vacancy
        Vacancy vacancy = vacancyRepository.findById(request.getVacancyId())
            .orElseThrow(() -> new IllegalArgumentException("Vacancy not found: " + request.getVacancyId()));

        // Check for duplicate application
        if (applicationRepository.findByCandidateIdAndVacancyId(candidate.getId(), vacancy.getId()).isPresent()) {
            throw new IllegalStateException("Application already exists for this candidate and vacancy");
        }

        // Create application
        Application application = Application.builder()
            .candidate(candidate)
            .vacancy(vacancy)
            .status(ApplicationStatus.NEW)
            .coverLetter(request.getCoverLetter())
            .build();

        application = applicationRepository.save(application);

        // Create notification
        createNotification(candidate, application, NotificationType.APPLICATION_RECEIVED,
            "Application Received",
            "Your application for " + vacancy.getTitle() + " has been received and is under review.");

        log.info("Created application ID: {} for candidate: {}", application.getId(), candidate.getEmail());
        
        return mapToDto(application);
    }

    /**
     * Get applications for recruiter dashboard.
     */
    @Transactional(readOnly = true)
    public Page<ApplicationDto> getApplicationsForRecruiter(ApplicationStatus status, Pageable pageable) {
        Page<Application> applications = status != null 
            ? applicationRepository.findByStatus(status, pageable)
            : applicationRepository.findAll(pageable);
        
        return applications.map(this::mapToDto);
    }

    /**
     * Change application status.
     */
    public ApplicationDto changeStatus(Long applicationId, ChangeStatusRequest request, User user) {
        log.info("Changing status for application {} to {}", applicationId, request.getNewStatus());
        
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        ApplicationStatus oldStatus = application.getStatus();
        application.changeStatus(request.getNewStatus(), user, request.getComment());
        
        application = applicationRepository.save(application);

        // Create notification for status change
        createNotification(application.getCandidate(), application, NotificationType.STATUS_CHANGED,
            "Application Status Updated",
            "Your application status has been updated to: " + request.getNewStatus());

        log.info("Changed application {} status from {} to {}", applicationId, oldStatus, request.getNewStatus());
        
        return mapToDto(application);
    }

    /**
     * Get applications pending HM review.
     */
    @Transactional(readOnly = true)
    public Page<ApplicationDto> getApplicationsPendingHmReview(Long hmId, Pageable pageable) {
        return applicationRepository.findByStatusAndHiringManagerId(
            ApplicationStatus.PENDING_HM_REVIEW, hmId, pageable)
            .map(this::mapToDto);
    }

    /**
     * Get application by ID with full details.
     */
    @Transactional(readOnly = true)
    public ApplicationDetailDto getApplication(Long id) {
        Application application = applicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
        return mapToDetailDto(application);
    }

    /**
     * Count applications by status.
     */
    public long countByStatus(ApplicationStatus status) {
        return applicationRepository.countByStatus(status);
    }

    /**
     * Count all applications.
     */
    public long countAll() {
        return applicationRepository.count();
    }

    /**
     * Create notification for candidate.
     */
    private void createNotification(Candidate candidate, Application application, 
                                   NotificationType type, String subject, String body) {
        Notification notification = Notification.builder()
            .candidate(candidate)
            .application(application)
            .type(type)
            .subject(subject)
            .body(body)
            .build();
        
        notificationRepository.save(notification);
        log.debug("Created notification for candidate: {}", candidate.getEmail());
    }

    /**
     * Map Application entity to DTO.
     */
    private ApplicationDto mapToDto(Application application) {
        Candidate candidate = application.getCandidate();
        User assignedRecruiter = application.getAssignedRecruiter();
        
        // Create nested CandidateDto
        CandidateDto candidateDto = CandidateDto.builder()
            .id(candidate.getId())
            .fullName(candidate.getFullName())
            .email(candidate.getEmail())
            .phone(candidate.getPhone())
            .university(candidate.getUniversity())
            .course(candidate.getCourse())
            .statusToken(candidate.getAccessToken())
            .build();
        
        return ApplicationDto.builder()
            .id(application.getId())
            // New nested structure
            .candidate(candidateDto)
            // Backward compatibility
            .candidateId(candidate.getId())
            .candidateName(candidate.getFullName())
            .candidateEmail(candidate.getEmail())
            .vacancyId(application.getVacancy().getId())
            .vacancyTitle(application.getVacancy().getTitle())
            .status(application.getStatus())
            .coverLetter(application.getCoverLetter())
            .notes(application.getNotes())
            .recruiterId(assignedRecruiter != null ? assignedRecruiter.getId() : null)
            .recruiterName(assignedRecruiter != null ? assignedRecruiter.getFullName() : null)
            .assignedRecruiterId(assignedRecruiter != null ? assignedRecruiter.getId() : null)
            .assignedRecruiterName(assignedRecruiter != null ? assignedRecruiter.getFullName() : null)
            .screeningScore(application.getScreeningScore())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .statusChangedAt(application.getUpdatedAt())
            .build();
    }
    
    /**
     * Map Application entity to DetailDto with related entities.
     */
    private ApplicationDetailDto mapToDetailDto(Application application) {
        Candidate candidate = application.getCandidate();
        User assignedRecruiter = application.getAssignedRecruiter();
        
        // Create nested CandidateDto
        CandidateDto candidateDto = CandidateDto.builder()
            .id(candidate.getId())
            .fullName(candidate.getFullName())
            .email(candidate.getEmail())
            .phone(candidate.getPhone())
            .university(candidate.getUniversity())
            .course(candidate.getCourse())
            .statusToken(candidate.getAccessToken())
            .build();
        
        // Map status history
        List<StatusHistoryDto> statusHistoryDtos = application.getStatusHistory().stream()
            .map(sh -> StatusHistoryDto.builder()
                .id(sh.getId())
                .status(sh.getToStatus())
                .comment(sh.getComment())
                .changedBy(sh.getChangedBy() != null ? sh.getChangedBy().getFullName() : "System")
                .changedAt(sh.getChangedAt())
                .build())
            .toList();
        
        // Map feedbacks
        List<FeedbackDto> feedbackDtos = application.getFeedbacks().stream()
            .map(fb -> FeedbackDto.builder()
                .id(fb.getId())
                .hmName(fb.getAuthor().getFullName())
                .decision("APPROVE") // TODO: add decision field to Feedback entity
                .overallAssessment(fb.getGeneralComments())
                .strengths(fb.getStrengths())
                .areasForGrowth(fb.getWeaknesses())
                .recommendations(fb.getRecommendation())
                .talentPool(false) // TODO: add talentPool field if needed
                .createdAt(fb.getCreatedAt())
                .build())
            .toList();
        
        // Map preferences
        List<ApplicationPreferenceDto> preferenceDtos = application.getPreferences().stream()
            .map(pref -> ApplicationPreferenceDto.builder()
                .preferenceOrder(pref.getRank())
                .preferredPosition(pref.getRawValue())
                .preferredLocation(pref.getVacancy() != null ? pref.getVacancy().getLocation() : null)
                .build())
            .toList();
        
        // Map interviews (if any)
        List<InterviewDto> interviewDtos = application.getInterviews().stream()
            .map(iv -> InterviewDto.builder()
                .id(iv.getId())
                .interviewType(iv.getCompleted() ? "Completed" : "Scheduled")
                .scheduledAt(iv.getScheduledAt())
                .completedAt(iv.getCompletedAt())
                .interviewerName(iv.getInterviewer() != null ? iv.getInterviewer().getFullName() : null)
                .notes(iv.getNotes())
                .build())
            .toList();
        
        return ApplicationDetailDto.builder()
            .id(application.getId())
            // New nested structure
            .candidate(candidateDto)
            // Backward compatibility
            .candidateId(candidate.getId())
            .candidateName(candidate.getFullName())
            .candidateEmail(candidate.getEmail())
            .vacancyId(application.getVacancy().getId())
            .vacancyTitle(application.getVacancy().getTitle())
            .status(application.getStatus())
            .coverLetter(application.getCoverLetter())
            .notes(application.getNotes())
            .recruiterId(assignedRecruiter != null ? assignedRecruiter.getId() : null)
            .recruiterName(assignedRecruiter != null ? assignedRecruiter.getFullName() : null)
            .assignedRecruiterId(assignedRecruiter != null ? assignedRecruiter.getId() : null)
            .assignedRecruiterName(assignedRecruiter != null ? assignedRecruiter.getFullName() : null)
            .screeningScore(application.getScreeningScore())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .statusChangedAt(application.getUpdatedAt())
            // Detail information
            .preferences(preferenceDtos)
            .statusHistory(statusHistoryDtos)
            .feedbacks(feedbackDtos)
            .interviews(interviewDtos)
            .build();
    }
}
