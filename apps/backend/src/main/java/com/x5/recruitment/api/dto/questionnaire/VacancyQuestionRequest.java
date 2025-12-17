package com.x5.recruitment.api.dto.questionnaire;

import com.x5.recruitment.domain.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating/updating a vacancy question.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyQuestionRequest {

    @NotBlank(message = "Question text is required")
    private String text;

    @NotNull(message = "Question type is required")
    private QuestionType type;

    private Boolean mandatory;

    private Boolean randomPool;

    private Integer orderIndex;

    /**
     * Validation rules as key-value pairs (e.g., minLength: 10, maxLength: 500)
     */
    private Map<String, Object> validationRules;

    /**
     * Options for CHOICE type questions
     */
    private List<String> options;

    private Integer weight;
}
