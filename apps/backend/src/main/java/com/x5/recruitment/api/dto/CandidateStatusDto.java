package com.x5.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for candidate status view (read-only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateStatusDto {
    private String candidateName;
    private String vacancyTitle;
    private String status;
    private String statusDescription;
    private LocalDateTime lastUpdated;
    private LocalDateTime appliedAt;
}
