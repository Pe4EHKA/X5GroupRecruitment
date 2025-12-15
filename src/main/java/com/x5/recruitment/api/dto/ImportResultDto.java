package com.x5.recruitment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for import result summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDto {

    private ImportBatchDto batch;
    private List<ImportRowErrorDto> errors;
    private Integer totalErrors;
    
    /**
     * Check if import was fully successful.
     */
    public boolean isFullySuccessful() {
        return batch.getFailedRows() == 0;
    }
    
    /**
     * Check if import had partial success.
     */
    public boolean isPartialSuccess() {
        return batch.getSuccessRows() > 0 && batch.getFailedRows() > 0;
    }
    
    /**
     * Check if import completely failed.
     */
    public boolean isCompleteFailure() {
        return batch.getSuccessRows() == 0 && batch.getTotalRows() > 0;
    }
}
