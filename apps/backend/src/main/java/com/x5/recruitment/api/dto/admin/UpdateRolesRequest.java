package com.x5.recruitment.api.dto.admin;

import com.x5.recruitment.domain.model.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for updating user roles.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRolesRequest {
    
    @NotNull(message = "Roles cannot be null")
    private Set<UserRole> roles;
}
