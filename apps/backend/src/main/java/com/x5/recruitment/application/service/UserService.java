package com.x5.recruitment.application.service;

import com.x5.recruitment.api.dto.admin.*;
import com.x5.recruitment.domain.model.*;
import com.x5.recruitment.domain.repository.AuditEventRepository;
import com.x5.recruitment.domain.repository.UserRepository;
import com.x5.recruitment.infrastructure.exception.ConflictException;
import com.x5.recruitment.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Service for user management operations.
 * Handles CRUD operations and business logic for user administration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuditEventRepository auditEventRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users with optional filters
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String query, UserStatus status, UserRole role, Pageable pageable) {
        Page<User> users;
        
        if (query != null && !query.isBlank()) {
            users = userRepository.searchUsers(query.trim(), pageable);
        } else if (status != null && role != null) {
            users = userRepository.findByRoleAndStatus(role, status, pageable);
        } else if (status != null) {
            users = userRepository.findByStatus(status, pageable);
        } else if (role != null) {
            users = userRepository.findByRole(role, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        return users.map(this::toUserResponse);
    }

    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toUserResponse(user);
    }

    /**
     * Create new user
     */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // Validate unique constraints
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists: " + request.getUsername());
        }
        if (userRepository.existsByEmailNormalized(request.getEmail().toLowerCase().trim())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        // Create user entity
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phone(request.getPhone())
            .department(request.getDepartment())
            .comment(request.getComment())
            .roles(request.getRoles())
            .status(request.getStatus() != null ? request.getStatus() : UserStatus.ACTIVE)
            .active(true)
            .passwordHash(generatePasswordHash(request.getPassword()))
            .createdBy(getCurrentUserId())
            .build();

        user = userRepository.save(user);
        
        // Audit
        createAuditEvent("CREATE_USER", "USER", user.getId(), 
            String.format("Created user: %s with roles: %s", user.getUsername(), user.getRoles()));
        
        log.info("Created user: {} (id: {}) by user: {}", user.getUsername(), user.getId(), getCurrentUserId());
        
        return toUserResponse(user);
    }

    /**
     * Update user profile
     */
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Check email uniqueness if changed
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            String normalizedEmail = request.getEmail().toLowerCase().trim();
            if (userRepository.existsByEmailNormalized(normalizedEmail)) {
                throw new ConflictException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Update fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            user.setDepartment(request.getDepartment());
        }
        if (request.getComment() != null) {
            user.setComment(request.getComment());
        }

        user.setUpdatedBy(getCurrentUserId());
        user = userRepository.save(user);

        // Audit
        createAuditEvent("UPDATE_USER", "USER", user.getId(), 
            String.format("Updated user: %s", user.getUsername()));

        log.info("Updated user: {} (id: {}) by user: {}", user.getUsername(), user.getId(), getCurrentUserId());

        return toUserResponse(user);
    }

    /**
     * Update user roles
     */
    @Transactional
    public UserResponse updateUserRoles(Long id, UpdateRolesRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Set<UserRole> oldRoles = user.getRoles();
        Set<UserRole> newRoles = request.getRoles();

        // Check if removing ADMIN role from last active admin
        if (oldRoles.contains(UserRole.ADMIN) && !newRoles.contains(UserRole.ADMIN)) {
            checkLastActiveAdmin(user);
        }

        user.setRoles(newRoles);
        user.setUpdatedBy(getCurrentUserId());
        user = userRepository.save(user);

        // Audit
        createAuditEvent("UPDATE_ROLES", "USER", user.getId(), 
            String.format("Updated roles from %s to %s for user: %s", oldRoles, newRoles, user.getUsername()));

        log.info("Updated roles for user: {} (id: {}) from {} to {} by user: {}", 
            user.getUsername(), user.getId(), oldRoles, newRoles, getCurrentUserId());

        return toUserResponse(user);
    }

    /**
     * Update user status (ACTIVE/DISABLED/INVITED)
     */
    @Transactional
    public UserResponse updateUserStatus(Long id, UpdateStatusRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        UserStatus oldStatus = user.getStatus();
        UserStatus newStatus = request.getStatus();

        // Check if disabling last active admin
        if (oldStatus == UserStatus.ACTIVE && newStatus == UserStatus.DISABLED && 
            user.getRoles().contains(UserRole.ADMIN)) {
            checkLastActiveAdmin(user);
        }

        user.setStatus(newStatus);
        user.setActive(newStatus == UserStatus.ACTIVE);
        user.setUpdatedBy(getCurrentUserId());
        user = userRepository.save(user);

        // Audit
        String action = newStatus == UserStatus.DISABLED ? "DISABLE_USER" : "ENABLE_USER";
        createAuditEvent(action, "USER", user.getId(), 
            String.format("Changed status from %s to %s for user: %s", oldStatus, newStatus, user.getUsername()));

        log.info("Updated status for user: {} (id: {}) from {} to {} by user: {}", 
            user.getUsername(), user.getId(), oldStatus, newStatus, getCurrentUserId());

        return toUserResponse(user);
    }

    /**
     * Check if user is the last active admin
     * Throws ConflictException if true
     */
    private void checkLastActiveAdmin(User user) {
        if (user.getRoles().contains(UserRole.ADMIN) && user.getStatus() == UserStatus.ACTIVE) {
            long activeAdminCount = userRepository.countActiveAdmins();
            if (activeAdminCount <= 1) {
                throw new ConflictException("Cannot disable or remove ADMIN role from the last active administrator");
            }
        }
    }

    /**
     * Generate password hash
     */
    private String generatePasswordHash(String password) {
        if (password != null && !password.isBlank()) {
            return passwordEncoder.encode(password);
        }
        // Generate random password if not provided
        String randomPassword = UUID.randomUUID().toString();
        return passwordEncoder.encode(randomPassword);
    }

    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                return userRepository.findByUsername(username)
                    .map(User::getId)
                    .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not get current user ID", e);
        }
        return null;
    }

    /**
     * Create audit event
     */
    private void createAuditEvent(String action, String entityType, Long entityId, String metadata) {
        try {
            AuditEvent event = AuditEvent.builder()
                .actorUserId(getCurrentUserId())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
            auditEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to create audit event", e);
        }
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .department(user.getDepartment())
            .comment(user.getComment())
            .roles(user.getRoles())
            .status(user.getStatus())
            .lastLoginAt(user.getLastLoginAt())
            .createdBy(user.getCreatedBy())
            .updatedBy(user.getUpdatedBy())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
