package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.ImportBatchDto;
import com.x5.recruitment.api.dto.ImportResultDto;
import com.x5.recruitment.api.dto.ImportRowErrorDto;
import com.x5.recruitment.api.exception.GlobalExceptionHandler;
import com.x5.recruitment.application.service.ImportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * REST API for Import/Export operations.
 */
@RestController
@RequestMapping("/api/import-export")
@RequiredArgsConstructor
@Tag(name = "Import/Export API", description = "Import applications from Excel and export approved candidates")
@PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
public class ImportExportController {

    private final ImportExportService importExportService;

    @Operation(summary = "Import applications",
               description = "Import applications from XLSX file with detailed validation and error reporting")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importApplications(
            @RequestParam("file") MultipartFile file) throws IOException {

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            GlobalExceptionHandler.ErrorResponse error = new GlobalExceptionHandler.ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                "Имя файла обязательно", LocalDateTime.now());
            return ResponseEntity.badRequest().body(error);
        }

        if (!originalName.endsWith(".xlsx") && !originalName.endsWith(".xls")) {
            throw new IllegalArgumentException("Допустим только импорт файлов .xlsx или .xls");
        }

        ImportResultDto result = importExportService.importFromExcel(file);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get import batch status",
               description = "Get status and summary of an import batch")
    @GetMapping("/batches/{batchId}")
    public ResponseEntity<ImportBatchDto> getImportBatch(@PathVariable Long batchId) {
        ImportBatchDto batch = importExportService.getImportBatch(batchId);
        return ResponseEntity.ok(batch);
    }

    @Operation(summary = "Get import batch errors",
               description = "Get paginated list of errors for an import batch")
    @GetMapping("/batches/{batchId}/errors")
    public ResponseEntity<Page<ImportRowErrorDto>> getImportBatchErrors(
            @PathVariable Long batchId,
            Pageable pageable) {
        Page<ImportRowErrorDto> errors = importExportService.getImportBatchErrors(batchId, pageable);
        return ResponseEntity.ok(errors);
    }

    @Operation(summary = "Export approved applications", 
               description = "Export approved applications to Excel for integration with internal ATS")
    @GetMapping("/export/approved")
    public ResponseEntity<byte[]> exportApprovedApplications() throws IOException {
        byte[] excelData = importExportService.exportApprovedApplications();
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=approved_applications.xlsx")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(excelData);
    }
}
