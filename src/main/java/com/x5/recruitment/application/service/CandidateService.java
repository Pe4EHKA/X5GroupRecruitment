package com.x5.recruitment.application.service;

import com.x5.recruitment.api.dto.CandidateStatusDto;
import com.x5.recruitment.domain.model.Application;
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

    private String getStatusDescription(com.x5.recruitment.domain.model.ApplicationStatus status) {
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
        };
    }
}
