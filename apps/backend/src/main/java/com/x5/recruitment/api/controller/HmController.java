package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.ApplicationDetailDto;
import com.x5.recruitment.api.dto.ApplicationDto;
import com.x5.recruitment.api.dto.HmDecisionRequest;
import com.x5.recruitment.application.service.ApplicationService;
import com.x5.recruitment.application.service.HmService;
import com.x5.recruitment.application.service.UserService;
import com.x5.recruitment.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Hiring Manager operations.
 */
@RestController
@RequestMapping("/api/hm")
@RequiredArgsConstructor
@Tag(name = "Hiring Manager API", description = "Operations for hiring managers to review and decide on applications")
@PreAuthorize("hasAnyRole('HM', 'ADMIN')")
public class HmController {

    private final ApplicationService applicationService;
    private final HmService hmService;
    private final UserService userService;

    @Operation(summary = "Get pending applications", description = "Get applications pending HM review")
    @GetMapping("/pending")
    public ResponseEntity<Page<ApplicationDto>> getPendingApplications(
            @AuthenticationPrincipal UserDetails principal,
            Pageable pageable) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        Page<ApplicationDto> applications = applicationService.getApplicationsPendingHmReview(
            user.getId(), pageable);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get application details", description = "Get detailed information about a specific application")
    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationDetailDto> getApplication(@PathVariable Long id) {
        ApplicationDetailDto application = applicationService.getApplication(id);
        return ResponseEntity.ok(application);
    }

    @Operation(summary = "Make decision", description = "Approve or reject application with structured feedback")
    @PostMapping("/applications/{id}/decision")
    public ResponseEntity<ApplicationDto> makeDecision(
            @PathVariable Long id,
            @Valid @RequestBody HmDecisionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        ApplicationDto application = hmService.makeDecision(id, request, user);
        return ResponseEntity.ok(application);
    }
}
