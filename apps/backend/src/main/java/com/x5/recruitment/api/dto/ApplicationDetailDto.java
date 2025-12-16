package com.x5.recruitment.api.dto;

import com.x5.recruitment.domain.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * DTO for detailed Application response with related entities.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApplicationDetailDto extends ApplicationDto {
    private List<ApplicationPreferenceDto> preferences;
    private List<StatusHistoryDto> statusHistory;
    private List<InterviewDto> interviews;
    private List<FeedbackDto> feedbacks;
}
