package com.x5.recruitment.api.dto;

import java.util.List;

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
    private String telegram;
    private String city;
    private String university;
    private String otherUniversity;
    private String speciality;
    private String otherSpeciality;
    private String course;
    private String schedule;
    private String source;
    private String citizenship;
    private Integer birthYear;
    private String otherCity;
    private List<String> languages;
    private String additionalInfo;
    private String resumePath;
    private String statusToken;
}

