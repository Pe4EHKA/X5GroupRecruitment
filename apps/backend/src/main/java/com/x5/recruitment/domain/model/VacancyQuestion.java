package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Question configured by HR for a vacancy.
 * Can be mandatory (always shown) or optional (shown randomly).
 */
@Entity
@Table(name = "vacancy_question", indexes = {
    @Index(name = "idx_vacancy_question_vacancy", columnList = "vacancy_id"),
    @Index(name = "idx_vacancy_question_mandatory", columnList = "mandatory"),
    @Index(name = "idx_vacancy_question_order", columnList = "vacancy_id,order_index")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacancyQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestionType type;

    /**
     * If true, this question is always shown to applicants
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean mandatory = false;

    /**
     * If true (and not mandatory), this question participates in random selection
     */
    @Column(name = "random_pool", nullable = false)
    @Builder.Default
    private Boolean randomPool = true;

    /**
     * Display order of the question
     */
    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    /**
     * Validation rules as JSON (min/max length, regex, range, etc.)
     */
    @Column(name = "validation_rules", columnDefinition = "JSONB")
    private String validationRules;

    /**
     * Options for CHOICE type questions as JSON array
     */
    @Column(columnDefinition = "JSONB")
    private String options;

    /**
     * Weight/importance for future scoring
     */
    @Column
    @Builder.Default
    private Integer weight = 1;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
