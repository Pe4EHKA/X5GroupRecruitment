package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.CandidateStatusDto;
import com.x5.recruitment.application.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Candidate self-service status checking.
 * No authentication required - uses access token.
 */
@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
@Tag(name = "Candidate API", description = "Public API for candidates to check their application status")
public class CandidateController {

    private final CandidateService candidateService;

    @Operation(summary = "Get application status", description = "Get status of all applications using access token")
    @GetMapping("/status")
    public ResponseEntity<List<CandidateStatusDto>> getStatus(
            @RequestParam String token) {
        
        List<CandidateStatusDto> statuses = candidateService.getCandidateStatus(token);
        return ResponseEntity.ok(statuses);
    }
}
