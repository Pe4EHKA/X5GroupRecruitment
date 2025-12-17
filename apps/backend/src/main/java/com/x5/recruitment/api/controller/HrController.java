package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.ApplicationDetailDto;
import com.x5.recruitment.api.dto.ApplicationDto;
import com.x5.recruitment.api.dto.ChangeStatusRequest;
import com.x5.recruitment.api.dto.PageResponseDto;
import com.x5.recruitment.application.service.ApplicationService;
import com.x5.recruitment.application.service.UserService;
import com.x5.recruitment.domain.model.ApplicationStatus;
import com.x5.recruitment.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST API for HR (Recruiter) operations in MVP.
 * Simplified interface for managing all applications.
 */
@RestController
@RequestMapping("/api/hr")
@RequiredArgsConstructor
@Tag(name = "HR API", description = "Operations for HR managing applications")
@PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
public class HrController {

    private final ApplicationService applicationService;
    private final UserService userService;

    @Operation(
        summary = "Get all applications with filters",
        description = "Get paginated list of applications with optional filters for status, vacancy, date range, and search"
    )
    @GetMapping("/applications")
    public ResponseEntity<PageResponseDto<ApplicationDto>> getApplications(
            @Parameter(description = "Filter by status (comma-separated for multiple)")
            @RequestParam(required = false) List<ApplicationStatus> statuses,
            
            @Parameter(description = "Filter by vacancy ID")
            @RequestParam(required = false) Long vacancyId,
            
            @Parameter(description = "Filter by date from (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            
            @Parameter(description = "Filter by date to (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            
            @Parameter(description = "Search by candidate name, email, or phone")
            @RequestParam(required = false) String search,
            
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort field and direction (e.g., 'createdAt,desc')")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        // Parse sort parameter
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        Page<ApplicationDto> applications = applicationService.getApplicationsWithFilters(
            statuses, vacancyId, dateFrom, dateTo, search, pageable);
        
        PageResponseDto<ApplicationDto> response = PageResponseDto.<ApplicationDto>builder()
            .content(applications.getContent())
            .totalElements(applications.getTotalElements())
            .totalPages(applications.getTotalPages())
            .size(applications.getSize())
            .number(applications.getNumber())
            .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get application by ID",
        description = "Get detailed information about a specific application"
    )
    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationDetailDto> getApplication(@PathVariable Long id) {
        ApplicationDetailDto application = applicationService.getApplication(id);
        return ResponseEntity.ok(application);
    }

    @Operation(
        summary = "Change application status",
        description = "Update the status of an application with optional comment"
    )
    @PostMapping("/applications/{id}/status")
    public ResponseEntity<ApplicationDto> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatusRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        ApplicationDto application = applicationService.changeStatus(id, request, user);
        return ResponseEntity.ok(application);
    }
}
