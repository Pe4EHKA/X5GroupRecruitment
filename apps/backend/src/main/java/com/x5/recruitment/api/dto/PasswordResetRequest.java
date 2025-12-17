package com.x5.recruitment.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for password reset operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {

    @Schema(description = "New password to set. If empty, the system generates a temporary one")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
}
