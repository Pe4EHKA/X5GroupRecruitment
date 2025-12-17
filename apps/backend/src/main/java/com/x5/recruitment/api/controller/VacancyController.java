package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.VacancyRequest;
import com.x5.recruitment.api.dto.VacancyResponse;
import com.x5.recruitment.application.service.VacancyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacancies")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('RECRUITER','ADMIN')")
@Tag(name = "Vacancies", description = "CRUD operations for internship programs")
public class VacancyController {

    private final VacancyService vacancyService;

    @GetMapping
    @Operation(summary = "List vacancies")
    public ResponseEntity<List<VacancyResponse>> getVacancies() {
        return ResponseEntity.ok(vacancyService.getAllVacancies());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vacancy by id")
    public ResponseEntity<VacancyResponse> getVacancy(@PathVariable Long id) {
        return ResponseEntity.ok(vacancyService.getVacancy(id));
    }

    @PostMapping
    @Operation(summary = "Create vacancy")
    public ResponseEntity<VacancyResponse> createVacancy(@Valid @RequestBody VacancyRequest request) {
        VacancyResponse response = vacancyService.createVacancy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vacancy")
    public ResponseEntity<VacancyResponse> updateVacancy(
        @PathVariable Long id,
        @Valid @RequestBody VacancyRequest request
    ) {
        VacancyResponse response = vacancyService.updateVacancy(id, request);
        return ResponseEntity.ok(response);
    }
}
