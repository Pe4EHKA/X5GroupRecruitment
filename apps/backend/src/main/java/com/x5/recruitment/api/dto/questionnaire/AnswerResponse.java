package com.x5.recruitment.api.dto.questionnaire;

import com.x5.recruitment.domain.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for an answer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {

    private Long id;
    private Long questionId;
    private String questionText;
    private QuestionType questionType;
    
    // Answer values
    private String textValue;
    private BigDecimal numberValue;
    private LocalDate dateValue;
    private List<String> choiceValues;
    private MediaResponse media; // For VIDEO type
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
