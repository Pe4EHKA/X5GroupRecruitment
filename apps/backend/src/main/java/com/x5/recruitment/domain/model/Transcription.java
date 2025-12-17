package com.x5.recruitment.domain.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Video transcription data and processing status.
 */
@Entity
@Table(name = "transcription", indexes = {
    @Index(name = "idx_transcription_media", columnList = "media_id"),
    @Index(name = "idx_transcription_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transcription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false, unique = true)
    private Media media;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private TranscriptionStatus status = TranscriptionStatus.PENDING;

    /**
     * Transcribed text
     */
    @Column(columnDefinition = "TEXT")
    private String text;

    /**
     * Language code (ru, en, etc.)
     */
    @Column(length = 10)
    @Builder.Default
    private String language = "ru";

    /**
     * Timestamped segments as JSON (optional)
     */
    @Column(columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode segments;

    /**
     * Error message if transcription failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Number of processing attempts
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Increment attempt counter and update status
     */
    public void incrementAttempts() {
        this.attempts++;
        this.status = TranscriptionStatus.PROCESSING;
    }

    /**
     * Mark transcription as completed
     */
    public void markCompleted(String transcribedText) {
        this.text = transcribedText;
        this.status = TranscriptionStatus.DONE;
    }

    /**
     * Mark transcription as failed
     */
    public void markFailed(String error) {
        this.errorMessage = error;
        this.status = TranscriptionStatus.FAILED;
    }
}
