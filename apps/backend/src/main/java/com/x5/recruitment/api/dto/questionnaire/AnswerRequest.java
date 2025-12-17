package com.x5.recruitment.api.dto.questionnaire;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for submitting an answer to a question.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId; // ApplicationQuestion ID

    // Different value types - only one should be provided based on question type
    private String textValue;
    private BigDecimal numberValue;
    private LocalDate dateValue;
    private List<String> choiceValues; // For SINGLE_CHOICE and MULTI_CHOICE
    private Long mediaId; // For VIDEO type (uploaded separately)
}
