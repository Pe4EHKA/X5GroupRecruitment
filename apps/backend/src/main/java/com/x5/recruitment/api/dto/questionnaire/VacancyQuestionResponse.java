package com.x5.recruitment.api.dto.questionnaire;

import com.x5.recruitment.domain.model.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for a vacancy question.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyQuestionResponse {

    private Long id;
    private Long vacancyId;
    private String text;
    private QuestionType type;
    private Boolean mandatory;
    private Boolean randomPool;
    private Integer orderIndex;
    private Map<String, Object> validationRules;
    private List<String> options;
    private Integer weight;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
