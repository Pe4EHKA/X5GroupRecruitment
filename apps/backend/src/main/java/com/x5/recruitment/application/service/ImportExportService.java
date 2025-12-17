package com.x5.recruitment.application.service;

import com.x5.recruitment.api.dto.ApplicationDto;
import com.x5.recruitment.api.dto.ImportBatchDto;
import com.x5.recruitment.api.dto.ImportResultDto;
import com.x5.recruitment.api.dto.ImportRowErrorDto;
import com.x5.recruitment.domain.model.ApplicationStatus;
import com.x5.recruitment.domain.model.ImportBatch;
import com.x5.recruitment.domain.model.ImportRowError;
import com.x5.recruitment.domain.repository.ApplicationRepository;
import com.x5.recruitment.domain.repository.ImportBatchRepository;
import com.x5.recruitment.domain.repository.ImportRowErrorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for importing and exporting applications via CSV/Excel.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportExportService {

    private final XlsxImportService xlsxImportService;
    private final ApplicationRepository applicationRepository;
    private final ImportBatchRepository batchRepository;
    private final ImportRowErrorRepository errorRepository;

    /**
     * Import applications from Excel file.
     */
    @Transactional
    public ImportResultDto importFromExcel(MultipartFile file) throws IOException {
        log.info("Importing applications from Excel file: {}", file.getOriginalFilename());
        
        // Use new XLSX import service
        ImportBatch batch = xlsxImportService.importFromXlsx(file);
        
        // Get errors (limited to first 100 for response)
        List<ImportRowError> errors = errorRepository.findByBatchId(batch.getId());
        List<ImportRowErrorDto> errorDtos = errors.stream()
            .limit(100)
            .map(this::toErrorDto)
            .collect(Collectors.toList());
        
        ImportBatchDto batchDto = toBatchDto(batch);
        
        return ImportResultDto.builder()
            .batch(batchDto)
            .errors(errorDtos)
            .totalErrors(errors.size())
            .build();
    }

    /**
     * Get import batch by ID.
     */
    @Transactional(readOnly = true)
    public ImportBatchDto getImportBatch(Long batchId) {
        ImportBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new IllegalArgumentException("Import batch not found: " + batchId));
        return toBatchDto(batch);
    }

    /**
     * Get import batch errors (paginated).
     */
    @Transactional(readOnly = true)
    public Page<ImportRowErrorDto> getImportBatchErrors(Long batchId, Pageable pageable) {
        return errorRepository.findByBatchId(batchId, pageable)
            .map(this::toErrorDto);
    }

    /**
     * Export approved applications to Excel.
     */
    @Transactional(readOnly = true)
    public byte[] exportApprovedApplications() throws IOException {
        log.info("Exporting approved applications");
        
        List<com.x5.recruitment.domain.model.Application> applications = 
            applicationRepository.findByStatusIn(List.of(ApplicationStatus.APPROVED));
        
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Approved Applications");
            
            // Create header
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Candidate Name", "Email", "Phone", "Vacancy", "Applied Date", "Status"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }
            
            // Create data rows
            int rowNum = 1;
            for (com.x5.recruitment.domain.model.Application app : applications) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(app.getId());
                row.createCell(1).setCellValue(app.getCandidate().getFullName());
                row.createCell(2).setCellValue(app.getCandidate().getEmail());
                row.createCell(3).setCellValue(app.getCandidate().getPhone() != null ? 
                    app.getCandidate().getPhone() : "");
                row.createCell(4).setCellValue(app.getVacancy().getTitle());
                row.createCell(5).setCellValue(app.getCreatedAt().toString());
                row.createCell(6).setCellValue(app.getStatus().name());
            }
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            log.info("Exported {} approved applications", applications.size());
            return out.toByteArray();
        }
    }

    /**
     * Convert ImportBatch to DTO.
     */
    private ImportBatchDto toBatchDto(ImportBatch batch) {
        return ImportBatchDto.builder()
            .id(batch.getId())
            .fileName(batch.getFileName())
            .uploadedById(batch.getUploadedBy() != null ? batch.getUploadedBy().getId() : null)
            .uploadedByName(batch.getUploadedBy() != null ? batch.getUploadedBy().getUsername() : null)
            .uploadedAt(batch.getUploadedAt())
            .totalRows(batch.getTotalRows())
            .successRows(batch.getSuccessRows())
            .failedRows(batch.getFailedRows())
            .usersCreated(batch.getUsersCreated())
            .usersLinked(batch.getUsersLinked())
            .completed(batch.getCompleted())
            .createdAt(batch.getCreatedAt())
            .updatedAt(batch.getUpdatedAt())
            .build();
    }

    /**
     * Convert ImportRowError to DTO.
     */
    private ImportRowErrorDto toErrorDto(ImportRowError error) {
        return ImportRowErrorDto.builder()
            .id(error.getId())
            .batchId(error.getBatch().getId())
            .rowNumber(error.getRowNumber())
            .errorCode(error.getErrorCode())
            .errorMessage(error.getErrorMessage())
            .rawSnapshot(error.getRawSnapshot())
            .createdAt(error.getCreatedAt())
            .build();
    }
}
