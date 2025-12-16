package com.x5.recruitment.api.dto.admin;

import com.x5.recruitment.domain.model.UserRole;
import com.x5.recruitment.domain.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for creating a new user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;
    
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    private String comment;
    
    @NotEmpty(message = "At least one role is required")
    private Set<UserRole> roles;
    
    private UserStatus status;
    
    /**
     * Optional: initial password. If not provided, system will generate one.
     * For INVITED users, password can be set later via activation link.
     */
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
