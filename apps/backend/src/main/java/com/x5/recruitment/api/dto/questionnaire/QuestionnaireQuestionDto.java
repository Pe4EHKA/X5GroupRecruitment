package com.x5.recruitment.api.dto.questionnaire;

import com.x5.recruitment.domain.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for a question in the applicant's questionnaire.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireQuestionDto {

    private Long questionId; // ApplicationQuestion ID
    private Long originalQuestionId; // VacancyQuestion ID (may be null if deleted)
    private String text;
    private QuestionType type;
    private Boolean mandatory;
    private Integer orderIndex;
    private Map<String, Object> validationRules;
    private List<String> options; // For CHOICE types
}
