package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Application entity representing a candidate's application to a vacancy.
 * Main aggregate root for the recruitment workflow.
 */
@Entity
@Table(name = "applications", indexes = {
    @Index(name = "idx_applications_status", columnList = "status"),
    @Index(name = "idx_applications_candidate", columnList = "candidate_id"),
    @Index(name = "idx_applications_vacancy", columnList = "vacancy_id"),
    @Index(name = "idx_applications_created", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.NEW;

    @Column(length = 500)
    private String coverLetter;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Recruiter assigned to this application
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_recruiter_id")
    private User assignedRecruiter;

    /**
     * Score from screening (0-100)
     */
    @Column
    private Integer screeningScore;

    /**
     * Actual submission date from Excel import (Дата заявки)
     */
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApplicationPreference> preferences = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Interview> interviews = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Feedback> feedbacks = new ArrayList<>();

    /**
     * Candidate's video presentation attached to the application
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_presentation_id")
    private Media videoPresentation;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Change application status and record history
     */
    public void changeStatus(ApplicationStatus newStatus, User changedBy, String comment) {
        ApplicationStatus oldStatus = this.status;
        this.status = newStatus;
        
        StatusHistory history = StatusHistory.builder()
            .application(this)
            .fromStatus(oldStatus)
            .toStatus(newStatus)
            .changedBy(changedBy)
            .comment(comment)
            .build();
        
        statusHistory.add(history);
    }

    /**
     * Add interview to application
     */
    public void addInterview(Interview interview) {
        interviews.add(interview);
        interview.setApplication(this);
    }

    /**
     * Add feedback to application
     */
    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
        feedback.setApplication(this);
    }

    /**
     * Add preference to application
     */
    public void addPreference(ApplicationPreference preference) {
        preferences.add(preference);
        preference.setApplication(this);
    }
}
