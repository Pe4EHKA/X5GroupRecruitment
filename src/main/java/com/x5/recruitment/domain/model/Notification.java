package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Notification entity for tracking communications with candidates.
 * Implements outbox pattern for reliable async notifications.
 */
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notifications_candidate", columnList = "candidate_id"),
    @Index(name = "idx_notifications_sent", columnList = "sent"),
    @Index(name = "idx_notifications_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false)
    @Builder.Default
    private Boolean sent = false;

    @Column
    private LocalDateTime sentAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Number of send attempts
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void markAsSent() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
    }

    public void recordError(String error) {
        this.errorMessage = error;
        this.attempts++;
    }
}
