package com.x5.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for import batch summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportBatchDto {

    private Long id;
    private String fileName;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
    private Integer totalRows;
    private Integer successRows;
    private Integer failedRows;
    private Boolean completed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
