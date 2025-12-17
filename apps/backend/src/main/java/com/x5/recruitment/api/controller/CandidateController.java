package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.ApplicationDetailDto;
import com.x5.recruitment.api.dto.CandidateStatusDto;
import com.x5.recruitment.api.dto.StatusHistoryDto;
import com.x5.recruitment.application.service.CandidateService;
import com.x5.recruitment.application.service.UserService;
import com.x5.recruitment.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Candidate self-service status checking.
 * Supports both token-based (no auth) and authenticated access.
 */
@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
@Tag(name = "Candidate API", description = "API for candidates to check their application status")
public class CandidateController {

    private final CandidateService candidateService;
    private final UserService userService;

    @Operation(summary = "Get application status", description = "Get status of all applications using access token (no authentication required)")
    @GetMapping("/status")
    public ResponseEntity<List<CandidateStatusDto>> getStatus(
            @RequestParam(required = true) String token) {
        
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        List<CandidateStatusDto> statuses = candidateService.getCandidateStatus(token);
        return ResponseEntity.ok(statuses);
    }

    @Operation(summary = "Get my applications", description = "Get all applications for authenticated candidate")
    @GetMapping("/me/applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<ApplicationDetailDto>> getMyApplications(
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        List<ApplicationDetailDto> applications = candidateService.getApplicationsForCandidate(user.getEmail());
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "Get my application by ID", description = "Get specific application details for authenticated candidate")
    @GetMapping("/me/applications/{id}")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ApplicationDetailDto> getMyApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        ApplicationDetailDto application = candidateService.getApplicationForCandidate(id, user.getEmail());
        return ResponseEntity.ok(application);
    }

    @Operation(summary = "Get my status history", description = "Get status history for a specific application")
    @GetMapping("/me/applications/{id}/history")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<List<StatusHistoryDto>> getMyStatusHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        List<StatusHistoryDto> history = candidateService.getStatusHistoryForCandidate(id, user.getEmail());
        return ResponseEntity.ok(history);
    }
}
