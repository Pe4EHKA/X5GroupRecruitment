package com.x5.recruitment.api.dto.questionnaire;

import com.x5.recruitment.domain.model.TranscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for video transcription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResponse {

    private Long id;
    private TranscriptionStatus status;
    private String text;
    private String language;
    private String errorMessage;
    private Integer attempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
