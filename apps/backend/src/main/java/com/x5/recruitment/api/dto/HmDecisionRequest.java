package com.x5.recruitment.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for HM decision with structured feedback.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HmDecisionRequest {
    
    @NotNull(message = "Decision is required")
    private Boolean approved;
    
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    
    private String strengths;
    
    private String weaknesses;
    
    private String recommendation;
    
    private String generalComments;
    
    @Builder.Default
    private Boolean shareWithCandidate = true;
}
