package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Snapshot of a question shown to a specific applicant.
 * Preserves question state even if vacancy questions are modified later.
 */
@Entity
@Table(name = "application_question", indexes = {
    @Index(name = "idx_application_question_application", columnList = "application_id"),
    @Index(name = "idx_application_question_question", columnList = "question_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    /**
     * Reference to original question (nullable if question was deleted)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private VacancyQuestion question;

    /**
     * Snapshot of question text at the time of application
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestionType type;

    /**
     * Snapshot of mandatory flag
     */
    @Column(name = "mandatory_snapshot", nullable = false)
    @Builder.Default
    private Boolean mandatorySnapshot = false;

    /**
     * Snapshot of display order
     */
    @Column(name = "order_snapshot", nullable = false)
    @Builder.Default
    private Integer orderSnapshot = 0;

    /**
     * Snapshot of validation rules
     */
    @Column(name = "validation_rules_snapshot", columnDefinition = "JSONB")
    private String validationRulesSnapshot;

    /**
     * Snapshot of options for CHOICE types
     */
    @Column(name = "options_snapshot", columnDefinition = "JSONB")
    private String optionsSnapshot;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
