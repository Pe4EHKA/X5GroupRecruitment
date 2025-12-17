package com.x5.recruitment.api.dto;

import com.x5.recruitment.domain.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Status History information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryDto {
    private Long id;
    private ApplicationStatus status;
    private String comment;
    private String changedBy;
    private LocalDateTime changedAt;
}
