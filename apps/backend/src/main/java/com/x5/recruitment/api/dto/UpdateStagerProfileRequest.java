package com.x5.recruitment.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating Stager/Candidate profile
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStagerProfileRequest {
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phone;
    
    private String city;
    
    private String university;
    
    private String course;
    
    private String telegram;
    
    private Integer birthYear;
}
