package com.x5.recruitment.api.dto;

import com.x5.recruitment.domain.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Application response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {
    private Long id;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    private Long vacancyId;
    private String vacancyTitle;
    private ApplicationStatus status;
    private String coverLetter;
    private String notes;
    private Long assignedRecruiterId;
    private String assignedRecruiterName;
    private Integer screeningScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
