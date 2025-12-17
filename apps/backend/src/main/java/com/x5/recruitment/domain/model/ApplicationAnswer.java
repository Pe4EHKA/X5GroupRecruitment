package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Answer provided by an applicant to a question.
 * Supports multiple answer types (text, number, date, choices, video).
 */
@Entity
@Table(name = "application_answer", indexes = {
    @Index(name = "idx_application_answer_question", columnList = "application_question_id"),
    @Index(name = "idx_application_answer_media", columnList = "media_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_question_id", nullable = false)
    private ApplicationQuestion applicationQuestion;

    /**
     * Text answer (for TEXT type)
     */
    @Column(name = "text_value", columnDefinition = "TEXT")
    private String textValue;

    /**
     * Numeric answer (for NUMBER type)
     */
    @Column(name = "number_value", precision = 15, scale = 2)
    private BigDecimal numberValue;

    /**
     * Date answer (for DATE type)
     */
    @Column(name = "date_value")
    private LocalDate dateValue;

    /**
     * Selected choices as JSON array (for SINGLE_CHOICE and MULTI_CHOICE)
     */
    @Column(name = "choice_values", columnDefinition = "JSONB")
    private String choiceValues;

    /**
     * Video/media answer (for VIDEO type)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id")
    private Media media;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
