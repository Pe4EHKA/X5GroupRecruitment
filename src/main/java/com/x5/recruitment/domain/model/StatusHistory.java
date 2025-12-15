package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Status history entity tracking all status transitions of an application.
 * Provides audit trail for the recruitment process.
 */
@Entity
@Table(name = "status_history", indexes = {
    @Index(name = "idx_status_history_application", columnList = "application_id"),
    @Index(name = "idx_status_history_changed_at", columnList = "changedAt")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ApplicationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_id")
    private User changedBy;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;
}
