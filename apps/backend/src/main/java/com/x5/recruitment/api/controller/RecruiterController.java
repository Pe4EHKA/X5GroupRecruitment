package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.ApplicationDto;
import com.x5.recruitment.api.dto.ChangeStatusRequest;
import com.x5.recruitment.api.dto.CreateApplicationRequest;
import com.x5.recruitment.application.service.ApplicationService;
import com.x5.recruitment.domain.model.ApplicationStatus;
import com.x5.recruitment.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Recruiter operations.
 */
@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
@Tag(name = "Recruiter API", description = "Operations for recruiters managing applications")
@PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
public class RecruiterController {

    private final ApplicationService applicationService;

    @Operation(summary = "Get all applications", description = "Get paginated list of applications with optional status filter")
    @GetMapping("/applications")
    public ResponseEntity<Page<ApplicationDto>> getApplications(
            @RequestParam(required = false) ApplicationStatus status,
            Pageable pageable) {
        
        Page<ApplicationDto> applications = applicationService.getApplicationsForRecruiter(status, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get application by ID", description = "Get detailed information about a specific application")
    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationDto> getApplication(@PathVariable Long id) {
        ApplicationDto application = applicationService.getApplication(id);
        return ResponseEntity.ok(application);
    }

    @Operation(summary = "Create new application", description = "Create a new application (typically from CSV import)")
    @PostMapping("/applications")
    public ResponseEntity<ApplicationDto> createApplication(
            @Valid @RequestBody CreateApplicationRequest request) {
        
        ApplicationDto application = applicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(application);
    }

    @Operation(summary = "Change application status", description = "Update the status of an application")
    @PatchMapping("/applications/{id}/status")
    public ResponseEntity<ApplicationDto> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request,
            @AuthenticationPrincipal User user) {
        
        ApplicationDto application = applicationService.changeStatus(id, request, user);
        return ResponseEntity.ok(application);
    }

    @Operation(summary = "Send to HM review", description = "Send application to hiring manager for review")
    @PostMapping("/applications/{id}/send-to-hm")
    public ResponseEntity<ApplicationDto> sendToHmReview(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        
        ChangeStatusRequest request = ChangeStatusRequest.builder()
            .newStatus(ApplicationStatus.PENDING_HM_REVIEW)
            .comment("Sent to HM for review")
            .build();
        
        ApplicationDto application = applicationService.changeStatus(id, request, user);
        return ResponseEntity.ok(application);
    }
}
