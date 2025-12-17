package com.x5.recruitment.api.dto.questionnaire;

import com.x5.recruitment.domain.model.TranscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for media (video) metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {

    private Long id;
    private String storageKey;
    private String mimeType;
    private Long fileSize;
    private Integer duration;
    private String streamUrl; // URL for streaming the video
    private TranscriptionResponse transcription;
    private LocalDateTime createdAt;
}
