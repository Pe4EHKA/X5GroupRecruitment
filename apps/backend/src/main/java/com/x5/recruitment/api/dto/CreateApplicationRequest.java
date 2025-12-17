package com.x5.recruitment.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating new application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationRequest {
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String phone;
    private Long vacancyId;
    private String vacancyTitle;
    
    private String coverLetter;

    private String additionalInfo;

    @AssertTrue(message = "Vacancy ID or title is required")
    public boolean hasVacancyReference() {
        return (vacancyId != null) || (vacancyTitle != null && !vacancyTitle.isBlank());
    }
}
