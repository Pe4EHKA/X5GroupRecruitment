package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.admin.*;
import com.x5.recruitment.application.service.UserService;
import com.x5.recruitment.domain.model.UserRole;
import com.x5.recruitment.domain.model.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller for user management.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - User Management", description = "User management endpoints for administrators")
@SecurityRequirement(name = "basicAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users", description = "Get paginated list of users with optional filters")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
        @Parameter(description = "Search query (email, username, or full name)")
        @RequestParam(required = false) String q,
        
        @Parameter(description = "Filter by status")
        @RequestParam(required = false) UserStatus status,
        
        @Parameter(description = "Filter by role")
        @RequestParam(required = false) UserRole role,
        
        @Parameter(description = "Page number (0-indexed)")
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") int size,
        
        @Parameter(description = "Sort field (createdAt, updatedAt, email, username)")
        @RequestParam(defaultValue = "updatedAt") String sort,
        
        @Parameter(description = "Sort direction (asc, desc)")
        @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<UserResponse> users = userService.getAllUsers(q, status, role, pageable);
        
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get detailed information about a specific user")
    public ResponseEntity<UserResponse> getUserById(
        @Parameter(description = "User ID")
        @PathVariable Long id
    ) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user with specified roles and profile")
    public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Update user profile information")
    public ResponseEntity<UserResponse> updateUser(
        @Parameter(description = "User ID")
        @PathVariable Long id,
        
        @Valid @RequestBody UpdateUserRequest request
    ) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Update user roles", description = "Replace user roles (full replacement, not merge)")
    public ResponseEntity<UserResponse> updateUserRoles(
        @Parameter(description = "User ID")
        @PathVariable Long id,
        
        @Valid @RequestBody UpdateRolesRequest request
    ) {
        UserResponse user = userService.updateUserRoles(id, request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update user status", description = "Change user status (ACTIVE/DISABLED/INVITED)")
    public ResponseEntity<UserResponse> updateUserStatus(
        @Parameter(description = "User ID")
        @PathVariable Long id,
        
        @Valid @RequestBody UpdateStatusRequest request
    ) {
        UserResponse user = userService.updateUserStatus(id, request);
        return ResponseEntity.ok(user);
    }
}
