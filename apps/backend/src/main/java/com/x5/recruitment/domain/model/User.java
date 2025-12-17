package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing system users (recruiters, hiring managers, admins).
 * Candidates are tracked separately in the Candidate entity.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_username", columnList = "username")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Normalized email for case-insensitive lookups
     */
    @Column(name = "email_normalized", unique = true, length = 255)
    private String emailNormalized;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    /**
     * Optional phone number
     */
    @Column(length = 20)
    private String phone;

    /**
     * Optional department/team
     */
    @Column(length = 100)
    private String department;

    /**
     * Optional admin comment
     */
    @Column(columnDefinition = "TEXT")
    private String comment;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    /**
     * User status (ACTIVE, DISABLED, INVITED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Deprecated: use status field instead
     * Kept for backward compatibility
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Last login timestamp (nullable)
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * User who created this user (nullable for system/initial users)
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * User who last updated this user (nullable)
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if user is truly active (status = ACTIVE and active flag = true)
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE && Boolean.TRUE.equals(active);
    }

    /**
     * Normalize email before persisting
     */
    @PrePersist
    @PreUpdate
    public void normalizeEmail() {
        if (email != null) {
            this.emailNormalized = email.toLowerCase().trim();
        }
    }
}
