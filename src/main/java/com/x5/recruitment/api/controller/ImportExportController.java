package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.ApplicationDto;
import com.x5.recruitment.application.service.ImportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    @Operation(summary = "Import applications", description = "Import applications from Excel/CSV file")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ApplicationDto>> importApplications(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        if (!file.getOriginalFilename().endsWith(".xlsx") && 
            !file.getOriginalFilename().endsWith(".xls")) {
            return ResponseEntity.badRequest().build();
        }
        
        List<ApplicationDto> imported = importExportService.importFromExcel(file);
        return ResponseEntity.ok(imported);
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
