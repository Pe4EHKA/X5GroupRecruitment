package com.x5.recruitment.api.dto.questionnaire;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for batch submitting answers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAnswerRequest {

    @NotEmpty(message = "At least one answer is required")
    @Valid
    private List<AnswerRequest> answers;
}
