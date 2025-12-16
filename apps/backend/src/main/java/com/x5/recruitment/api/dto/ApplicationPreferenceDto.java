package com.x5.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Application Preference information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPreferenceDto {
    private Integer preferenceOrder;
    private String preferredPosition;
    private String preferredLocation;
}
