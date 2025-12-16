package com.x5.recruitment.application.service;

import com.x5.recruitment.api.dto.*;
import com.x5.recruitment.domain.model.Application;
import com.x5.recruitment.domain.model.ApplicationStatus;
import com.x5.recruitment.domain.model.Candidate;
import com.x5.recruitment.domain.repository.ApplicationRepository;
import com.x5.recruitment.domain.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for candidate self-service operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CandidateService {

    private final CandidateRepository candidateRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * Get candidate applications by access token.
     */
    public List<CandidateStatusDto> getCandidateStatus(String accessToken) {
        log.debug("Fetching status for access token: {}", accessToken);
        
        Candidate candidate = candidateRepository.findByAccessToken(accessToken)
            .orElseThrow(() -> new IllegalArgumentException("Invalid access token"));

        List<Application> applications = applicationRepository.findByCandidateId(candidate.getId());

        return applications.stream()
            .map(this::mapToStatusDto)
            .collect(Collectors.toList());
    }

    /**
     * Get all applications for a candidate by email (for authenticated users).
     */
    public List<ApplicationDetailDto> getApplicationsForCandidate(String email) {
        log.debug("Fetching applications for candidate email: {}", email);
        
        Candidate candidate = candidateRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        List<Application> applications = applicationRepository.findByCandidateId(candidate.getId());

        return applications.stream()
            .map(this::mapToDetailDto)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific application for a candidate (with ownership check).
     */
    public ApplicationDetailDto getApplicationForCandidate(Long applicationId, String email) {
        log.debug("Fetching application {} for candidate email: {}", applicationId, email);
        
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Security check: ensure the application belongs to this candidate
        if (!application.getCandidate().getEmail().equals(email)) {
            throw new SecurityException("You do not have permission to view this application");
        }

        return mapToDetailDto(application);
    }

    /**
     * Get status history for a specific application (with ownership check).
     */
    public List<StatusHistoryDto> getStatusHistoryForCandidate(Long applicationId, String email) {
        log.debug("Fetching status history for application {} and candidate email: {}", applicationId, email);
        
        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Security check: ensure the application belongs to this candidate
        if (!application.getCandidate().getEmail().equals(email)) {
            throw new SecurityException("You do not have permission to view this application");
        }

        return application.getStatusHistory().stream()
            .map(sh -> StatusHistoryDto.builder()
                .id(sh.getId())
                .status(sh.getToStatus())
                .comment(sh.getComment())
                .changedBy(sh.getChangedBy() != null ? sh.getChangedBy().getFullName() : "System")
                .changedAt(sh.getChangedAt())
                .build())
            .collect(Collectors.toList());
    }

    private CandidateStatusDto mapToStatusDto(Application application) {
        return CandidateStatusDto.builder()
            .candidateName(application.getCandidate().getFullName())
            .vacancyTitle(application.getVacancy().getTitle())
            .status(application.getStatus().name())
            .statusDescription(getStatusDescription(application.getStatus()))
            .lastUpdated(application.getUpdatedAt())
            .appliedAt(application.getCreatedAt())
            .build();
    }

    private ApplicationDetailDto mapToDetailDto(Application application) {
        Candidate candidate = application.getCandidate();
        
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
            .collect(Collectors.toList());
        
        // Map preferences
        List<ApplicationPreferenceDto> preferenceDtos = application.getPreferences().stream()
            .map(pref -> ApplicationPreferenceDto.builder()
                .preferenceOrder(pref.getRank())
                .preferredPosition(pref.getRawValue())
                .preferredLocation(pref.getVacancy() != null ? pref.getVacancy().getLocation() : null)
                .build())
            .collect(Collectors.toList());
        
        return ApplicationDetailDto.builder()
            .id(application.getId())
            .candidate(candidateDto)
            .candidateId(candidate.getId())
            .candidateName(candidate.getFullName())
            .candidateEmail(candidate.getEmail())
            .vacancyId(application.getVacancy().getId())
            .vacancyTitle(application.getVacancy().getTitle())
            .status(application.getStatus())
            .coverLetter(application.getCoverLetter())
            .notes(application.getNotes())
            .createdAt(application.getCreatedAt())
            .updatedAt(application.getUpdatedAt())
            .statusChangedAt(application.getUpdatedAt())
            .currentComment(getStatusDescription(application.getStatus()))
            .preferences(preferenceDtos)
            .statusHistory(statusHistoryDtos)
            .build();
    }

    private String getStatusDescription(ApplicationStatus status) {
        return switch (status) {
            case NEW -> "Your application has been received and is awaiting review.";
            case SCREENING -> "Your application is currently being reviewed by our recruitment team.";
            case PENDING_HM_REVIEW -> "Your application has passed initial screening and is awaiting hiring manager review.";
            case INTERVIEW_SCHEDULED -> "An interview has been scheduled. Check your email for details.";
            case INTERVIEW_COMPLETED -> "Your interview has been completed. We will contact you soon with next steps.";
            case APPROVED -> "Congratulations! Your application has been approved.";
            case REJECTED -> "Thank you for your interest. Unfortunately, we have decided to move forward with other candidates.";
            case WITHDRAWN -> "Your application has been withdrawn.";
            case ON_HOLD -> "Your application is on hold for future consideration.";
            default -> "Status update in progress.";
        };
    }
}
