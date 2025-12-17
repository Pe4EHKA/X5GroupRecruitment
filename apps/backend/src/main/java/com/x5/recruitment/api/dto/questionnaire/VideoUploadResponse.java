package com.x5.recruitment.api.dto.questionnaire;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for video upload operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoUploadResponse {

    private Long mediaId;
    private String storageKey;
    private String mimeType;
    private Long fileSize;
    private Integer duration;
    private String message;
    private Boolean transcriptionInitiated;
}
