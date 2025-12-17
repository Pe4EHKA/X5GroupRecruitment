package com.x5.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Stager/Candidate profile information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagerProfileDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String city;
    private String university;
    private String course;
    private String telegram;
    private Integer birthYear;
    private String citizenship;
    private String speciality;
    private String schedule;
    private String source;
    private java.util.List<String> languages;
    private String additionalInfo;
}
