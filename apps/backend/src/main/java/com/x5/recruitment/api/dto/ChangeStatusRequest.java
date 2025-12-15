package com.x5.recruitment.api.dto;

import com.x5.recruitment.domain.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for changing application status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusRequest {
    
    @NotNull(message = "New status is required")
    private ApplicationStatus newStatus;
    
    private String comment;
}
