package com.x5.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Interview information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewDto {
    private Long id;
    private String interviewType;
    private LocalDateTime scheduledAt;
    private LocalDateTime completedAt;
    private String interviewerName;
    private String notes;
}
