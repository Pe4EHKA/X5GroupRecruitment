package com.x5.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for import row error.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportRowErrorDto {

    private Long id;
    private Long batchId;
    private Integer rowNumber;
    private String errorCode;
    private String errorMessage;
    private Map<String, Object> rawSnapshot;
    private LocalDateTime createdAt;
}
