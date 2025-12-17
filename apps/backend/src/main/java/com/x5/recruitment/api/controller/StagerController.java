package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.ApplicationDetailDto;
import com.x5.recruitment.api.dto.StagerProfileDto;
import com.x5.recruitment.api.dto.UpdateStagerProfileRequest;
import com.x5.recruitment.api.dto.questionnaire.*;
import com.x5.recruitment.application.service.AnswerService;
import com.x5.recruitment.application.service.CandidateService;
import com.x5.recruitment.application.service.QuestionnaireService;
import com.x5.recruitment.application.service.UserService;
import com.x5.recruitment.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Stager (Intern/Candidate) operations.
 * Provides access to own application and profile.
 */
@RestController
@RequestMapping("/api/stager")
@RequiredArgsConstructor
@Tag(name = "Stager API", description = "Operations for stagers to view their applications")
@PreAuthorize("hasAnyRole('STAGER', 'CANDIDATE', 'ADMIN')")
public class StagerController {

    private final CandidateService candidateService;
    private final UserService userService;
    private final QuestionnaireService questionnaireService;
    private final AnswerService answerService;

    @Operation(
        summary = "Get my applications",
        description = "Get all applications for the authenticated stager/candidate"
    )
    @GetMapping("/application")
    public ResponseEntity<List<ApplicationDetailDto>> getMyApplications(
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        List<ApplicationDetailDto> applications = candidateService.getApplicationsForCandidate(user.getEmail());
        return ResponseEntity.ok(applications);
    }

    @Operation(
        summary = "Get my application by ID",
        description = "Get detailed information about a specific application (must be owned by current user)"
    )
    @GetMapping("/application/{id}")
    public ResponseEntity<ApplicationDetailDto> getMyApplication(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        ApplicationDetailDto application = candidateService.getApplicationForCandidate(id, user.getEmail());
        return ResponseEntity.ok(application);
    }

    @Operation(
        summary = "Update my profile",
        description = "Update own profile information (phone, city, university, etc.)"
    )
    @PutMapping("/profile")
    public ResponseEntity<StagerProfileDto> updateProfile(
            @Valid @RequestBody UpdateStagerProfileRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        StagerProfileDto profile = candidateService.updateCandidateProfile(user.getEmail(), request);
        return ResponseEntity.ok(profile);
    }

    @Operation(
        summary = "Get my profile",
        description = "Get own profile information"
    )
    @GetMapping("/profile")
    public ResponseEntity<StagerProfileDto> getProfile(
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        StagerProfileDto profile = candidateService.getCandidateProfile(user.getEmail());
        return ResponseEntity.ok(profile);
    }

    // ========== Questionnaire Management ==========

    @Operation(
        summary = "Get questionnaire for application",
        description = "Get the questionnaire (questions) for a specific application. " +
                      "Questions are generated once and remain stable for the application."
    )
    @GetMapping("/application/{applicationId}/questionnaire")
    public ResponseEntity<QuestionnaireResponse> getQuestionnaire(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserDetails principal) {
        
        // Verify the application belongs to the current user (throws exception if not)
        User user = userService.getUserEntityByUsername(principal.getUsername());
        candidateService.getApplicationForCandidate(applicationId, user.getEmail());
        
        QuestionnaireResponse questionnaire = questionnaireService.getQuestionnaire(applicationId);
        return ResponseEntity.ok(questionnaire);
    }

    @Operation(
        summary = "Submit answers",
        description = "Submit batch of answers for an application"
    )
    @PostMapping("/application/{applicationId}/answers")
    public ResponseEntity<List<AnswerResponse>> submitAnswers(
            @PathVariable Long applicationId,
            @Valid @RequestBody BatchAnswerRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        // Verify the application belongs to the current user (throws exception if not)
        User user = userService.getUserEntityByUsername(principal.getUsername());
        candidateService.getApplicationForCandidate(applicationId, user.getEmail());
        
        List<AnswerResponse> answers = answerService.submitAnswers(applicationId, request);
        return ResponseEntity.ok(answers);
    }

    @Operation(
        summary = "Get my answers",
        description = "Get all answers submitted for an application"
    )
    @GetMapping("/application/{applicationId}/answers")
    public ResponseEntity<List<AnswerResponse>> getAnswers(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal UserDetails principal) {
        
        // Verify the application belongs to the current user (throws exception if not)
        User user = userService.getUserEntityByUsername(principal.getUsername());
        candidateService.getApplicationForCandidate(applicationId, user.getEmail());
        
        List<AnswerResponse> answers = answerService.getAnswers(applicationId);
        return ResponseEntity.ok(answers);
    }
}
