package com.x5.recruitment.application.service;

import com.x5.recruitment.api.dto.ApplicationDto;
import com.x5.recruitment.api.dto.CreateApplicationRequest;
import com.x5.recruitment.domain.model.ApplicationStatus;
import com.x5.recruitment.domain.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing and exporting applications via CSV/Excel.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportExportService {

    private final ApplicationService applicationService;
    private final ApplicationRepository applicationRepository;

    /**
     * Import applications from Excel file.
     */
    @Transactional
    public List<ApplicationDto> importFromExcel(MultipartFile file) throws IOException {
        log.info("Importing applications from Excel file: {}", file.getOriginalFilename());
        
        List<ApplicationDto> imported = new ArrayList<>();
        
        try (InputStream is = file.getInputStream(); 
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    CreateApplicationRequest request = parseRow(row);
                    ApplicationDto dto = applicationService.createApplication(request);
                    imported.add(dto);
                    log.debug("Imported application for: {}", request.getEmail());
                } catch (Exception e) {
                    log.error("Error importing row {}: {}", i, e.getMessage());
                }
            }
        }
        
        log.info("Imported {} applications", imported.size());
        return imported;
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
     * Parse Excel row to CreateApplicationRequest.
     */
    private CreateApplicationRequest parseRow(Row row) {
        return CreateApplicationRequest.builder()
            .firstName(getCellValueAsString(row.getCell(0)))
            .lastName(getCellValueAsString(row.getCell(1)))
            .email(getCellValueAsString(row.getCell(2)))
            .phone(getCellValueAsString(row.getCell(3)))
            .vacancyId(getCellValueAsLong(row.getCell(4)))
            .coverLetter(getCellValueAsString(row.getCell(5)))
            .additionalInfo(getCellValueAsString(row.getCell(6)))
            .build();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Long getCellValueAsLong(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> Long.parseLong(cell.getStringCellValue());
            default -> null;
        };
    }
}
