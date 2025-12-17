package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.ApplicationDetailDto;
import com.x5.recruitment.api.dto.ApplicationDto;
import com.x5.recruitment.api.dto.ChangeStatusRequest;
import com.x5.recruitment.api.dto.PageResponseDto;
import com.x5.recruitment.api.dto.questionnaire.*;
import com.x5.recruitment.application.service.ApplicationService;
import com.x5.recruitment.application.service.StatisticsService;
import com.x5.recruitment.application.service.UserService;
import com.x5.recruitment.application.service.VacancyQuestionService;
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
    private final VacancyQuestionService vacancyQuestionService;
    private final StatisticsService statisticsService;

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

    // ========== Vacancy Question Management ==========

    @Operation(
        summary = "Create question for vacancy",
        description = "Add a new question to a vacancy questionnaire"
    )
    @PostMapping("/vacancies/{vacancyId}/questions")
    public ResponseEntity<VacancyQuestionResponse> createQuestion(
            @PathVariable Long vacancyId,
            @Valid @RequestBody VacancyQuestionRequest request) {
        
        VacancyQuestionResponse question = vacancyQuestionService.createQuestion(vacancyId, request);
        return ResponseEntity.ok(question);
    }

    @Operation(
        summary = "Update vacancy question",
        description = "Update an existing vacancy question"
    )
    @PutMapping("/vacancies/questions/{questionId}")
    public ResponseEntity<VacancyQuestionResponse> updateQuestion(
            @PathVariable Long questionId,
            @Valid @RequestBody VacancyQuestionRequest request) {
        
        VacancyQuestionResponse question = vacancyQuestionService.updateQuestion(questionId, request);
        return ResponseEntity.ok(question);
    }

    @Operation(
        summary = "Get all questions for vacancy",
        description = "Get all questions configured for a vacancy"
    )
    @GetMapping("/vacancies/{vacancyId}/questions")
    public ResponseEntity<List<VacancyQuestionResponse>> getQuestions(
            @PathVariable Long vacancyId) {
        
        List<VacancyQuestionResponse> questions = vacancyQuestionService.getQuestions(vacancyId);
        return ResponseEntity.ok(questions);
    }

    @Operation(
        summary = "Delete vacancy question",
        description = "Delete a question from vacancy"
    )
    @DeleteMapping("/vacancies/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        vacancyQuestionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Reorder vacancy questions",
        description = "Update the order of questions in a vacancy"
    )
    @PutMapping("/vacancies/{vacancyId}/questions/reorder")
    public ResponseEntity<Void> reorderQuestions(
            @PathVariable Long vacancyId,
            @RequestBody List<Long> questionIds) {
        
        vacancyQuestionService.reorderQuestions(vacancyId, questionIds);
        return ResponseEntity.ok().build();
    }

    // ========== Application Statistics ==========

    @Operation(
        summary = "Get application statistics",
        description = "Get detailed statistics for a specific application including answers and completion"
    )
    @GetMapping("/applications/{applicationId}/statistics")
    public ResponseEntity<ApplicationStatisticsResponse> getApplicationStatistics(
            @PathVariable Long applicationId) {
        
        ApplicationStatisticsResponse statistics = statisticsService.getApplicationStatistics(applicationId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(
        summary = "Get vacancy statistics",
        description = "Get statistics for all applications in a vacancy"
    )
    @GetMapping("/vacancies/{vacancyId}/statistics")
    public ResponseEntity<List<ApplicationStatisticsResponse>> getVacancyStatistics(
            @PathVariable Long vacancyId) {
        
        List<ApplicationStatisticsResponse> statistics = statisticsService.getVacancyStatistics(vacancyId);
        return ResponseEntity.ok(statistics);
    }
}
