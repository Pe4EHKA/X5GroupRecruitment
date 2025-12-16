package com.x5.recruitment.api.dto.admin;

import com.x5.recruitment.domain.model.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    
    @NotNull(message = "Status is required")
    private UserStatus status;
}
