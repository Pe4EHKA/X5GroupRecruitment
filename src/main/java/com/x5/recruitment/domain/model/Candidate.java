package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Candidate entity representing a person applying for internship positions.
 * Aggregates all applications and interactions with the recruitment system.
 */
@Entity
@Table(name = "candidates", indexes = {
    @Index(name = "idx_candidates_email", columnList = "email"),
    @Index(name = "idx_candidates_phone", columnList = "phone"),
    @Index(name = "idx_candidates_access_token", columnList = "accessToken")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 500)
    private String resumePath;

    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    /**
     * Unique token for candidate self-service status page
     */
    @Column(nullable = false, unique = true, length = 100)
    @Builder.Default
    private String accessToken = UUID.randomUUID().toString();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Application> applications = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Add application to candidate
     */
    public void addApplication(Application application) {
        applications.add(application);
        application.setCandidate(this);
    }

    /**
     * Remove application from candidate
     */
    public void removeApplication(Application application) {
        applications.remove(application);
        application.setCandidate(null);
    }
}
