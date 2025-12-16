package com.x5.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Candidate information in Application responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateDto {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String university;
    private String course;
    private String statusToken;
}
