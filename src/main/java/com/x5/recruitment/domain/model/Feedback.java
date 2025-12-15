package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Feedback entity for structured feedback on candidates.
 * Used by recruiters and hiring managers to provide decision rationale.
 */
@Entity
@Table(name = "feedbacks", indexes = {
    @Index(name = "idx_feedbacks_application", columnList = "application_id"),
    @Index(name = "idx_feedbacks_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Overall rating (1-5)
     */
    @Column
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(columnDefinition = "TEXT")
    private String generalComments;

    /**
     * Whether this feedback should be shared with candidate
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean shareWithCandidate = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
