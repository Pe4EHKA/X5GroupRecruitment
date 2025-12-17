package com.x5.recruitment.api.dto.questionnaire;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for the applicant's questionnaire.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireResponse {

    private Long applicationId;
    private Long vacancyId;
    private String vacancyTitle;
    private List<QuestionnaireQuestionDto> questions;
    private Integer totalQuestions;
    private Integer mandatoryQuestions;
    private Integer optionalQuestions;
}
