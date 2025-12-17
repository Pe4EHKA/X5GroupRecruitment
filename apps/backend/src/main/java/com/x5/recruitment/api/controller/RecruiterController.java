package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.ApplicationDetailDto;
import com.x5.recruitment.api.dto.ApplicationDto;
import com.x5.recruitment.api.dto.ChangeStatusRequest;
import com.x5.recruitment.api.dto.CreateApplicationRequest;
import com.x5.recruitment.application.service.ApplicationService;
import com.x5.recruitment.application.service.UserService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    private final UserService userService;

    @Operation(summary = "Get dashboard metrics", description = "Get metrics for recruiter dashboard")
    @GetMapping("/dashboard/metrics")
    public ResponseEntity<Map<String, Long>> getDashboardMetrics() {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("newCount", applicationService.countByStatus(ApplicationStatus.NEW));
        metrics.put("screeningCount", applicationService.countByStatus(ApplicationStatus.SCREENING));
        metrics.put("interviewCount", applicationService.countByStatus(ApplicationStatus.INTERVIEW_SCHEDULED) 
            + applicationService.countByStatus(ApplicationStatus.INTERVIEW_COMPLETED));
        metrics.put("approvedCount", applicationService.countByStatus(ApplicationStatus.APPROVED));
        metrics.put("rejectedCount", applicationService.countByStatus(ApplicationStatus.REJECTED));
        metrics.put("slaBreachCount", 0L); // Placeholder for SLA calculation
        metrics.put("totalCount", applicationService.countAll());
        
        return ResponseEntity.ok(metrics);
    }

    @Operation(summary = "Get all applications", description = "Get paginated list of applications with optional status filter")
    @GetMapping("/applications")
    public ResponseEntity<Page<ApplicationDto>> getApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Long vacancyId,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        Page<ApplicationDto> applications = applicationService.getApplicationsForRecruiter(status, vacancyId, search, pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get application by ID", description = "Get detailed information about a specific application")
    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationDetailDto> getApplication(@PathVariable Long id) {
        ApplicationDetailDto application = applicationService.getApplication(id);
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
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        ApplicationDto application = applicationService.changeStatus(id, request, user);
        return ResponseEntity.ok(application);
    }
}
