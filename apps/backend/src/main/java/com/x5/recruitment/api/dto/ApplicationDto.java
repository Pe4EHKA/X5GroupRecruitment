package com.x5.recruitment.api.dto;

import com.x5.recruitment.domain.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * DTO for Application response.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {
    private Long id;
    
    // Nested candidate object (new structure)
    private CandidateDto candidate;
    
    // Deprecated: kept for backward compatibility
    @Deprecated
    private Long candidateId;
    @Deprecated
    private String candidateName;
    @Deprecated
    private String candidateEmail;
    
    private Long vacancyId;
    private String vacancyTitle;
    private ApplicationStatus status;
    private String coverLetter;
    private String notes;
    
    private Long recruiterId;
    private String recruiterName;
    private Long hmId;
    private String hmName;
    
    @Deprecated
    private Long assignedRecruiterId;
    @Deprecated
    private String assignedRecruiterName;
    
    private Integer screeningScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime statusChangedAt;
    private String currentComment;
}
