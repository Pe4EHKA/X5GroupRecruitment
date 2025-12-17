package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Import batch entity for tracking Excel/CSV import operations.
 * Tracks the overall import process and provides summary statistics.
 */
@Entity
@Table(name = "import_batches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private User uploadedBy;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Builder.Default
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows = 0;

    @Builder.Default
    @Column(name = "success_rows", nullable = false)
    private Integer successRows = 0;

    @Builder.Default
    @Column(name = "failed_rows", nullable = false)
    private Integer failedRows = 0;

    @Builder.Default
    @Column(name = "users_created", nullable = false)
    private Integer usersCreated = 0;

    @Builder.Default
    @Column(name = "users_linked", nullable = false)
    private Integer usersLinked = 0;

    @Builder.Default
    @Column(name = "completed", nullable = false)
    private Boolean completed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }

        // Defensive defaults for counters created via Lombok builder
        if (totalRows == null) {
            totalRows = 0;
        }
        if (successRows == null) {
            successRows = 0;
        }
        if (failedRows == null) {
            failedRows = 0;
        }
        if (usersCreated == null) {
            usersCreated = 0;
        }
        if (usersLinked == null) {
            usersLinked = 0;
        }
        if (completed == null) {
            completed = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increment success counter.
     */
    public void incrementSuccess() {
        this.successRows++;
    }

    /**
     * Increment failure counter.
     */
    public void incrementFailure() {
        this.failedRows++;
    }

    /**
     * Increment users created counter.
     */
    public void incrementUsersCreated() {
        this.usersCreated++;
    }

    /**
     * Increment users linked counter.
     */
    public void incrementUsersLinked() {
        this.usersLinked++;
    }

    /**
     * Mark batch as completed.
     */
    public void markCompleted() {
        this.completed = true;
    }
}
