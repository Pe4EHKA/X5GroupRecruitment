package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Media file metadata (videos, documents, etc.)
 */
@Entity
@Table(name = "media", indexes = {
    @Index(name = "idx_media_storage_key", columnList = "storage_key"),
    @Index(name = "idx_media_created_by", columnList = "created_by")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * File path or cloud storage key (S3/MinIO)
     */
    @Column(name = "storage_key", nullable = false, unique = true, length = 500)
    private String storageKey;

    /**
     * MIME type (e.g., video/webm, video/mp4)
     */
    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    /**
     * File size in bytes
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * Duration in seconds (for video/audio)
     */
    @Column
    private Integer duration;

    /**
     * User who uploaded the file
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
