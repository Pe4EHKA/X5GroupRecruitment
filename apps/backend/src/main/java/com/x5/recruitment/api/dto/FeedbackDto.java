package com.x5.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Feedback information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDto {
    private Long id;
    private String hmName;
    private String decision;
    private String overallAssessment;
    private String strengths;
    private String areasForGrowth;
    private String recommendations;
    private Boolean talentPool;
    private LocalDateTime createdAt;
}
