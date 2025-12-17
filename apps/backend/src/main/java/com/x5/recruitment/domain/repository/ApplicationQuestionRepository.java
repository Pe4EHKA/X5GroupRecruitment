package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.ApplicationQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ApplicationQuestion entity.
 */
@Repository
public interface ApplicationQuestionRepository extends JpaRepository<ApplicationQuestion, Long> {

    /**
     * Find all questions for an application, ordered by snapshot order
     */
    List<ApplicationQuestion> findByApplicationIdOrderByOrderSnapshotAsc(Long applicationId);

    /**
     * Find mandatory questions for an application
     */
    List<ApplicationQuestion> findByApplicationIdAndMandatorySnapshotTrueOrderByOrderSnapshotAsc(Long applicationId);

    /**
     * Count questions for an application
     */
    long countByApplicationId(Long applicationId);

    /**
     * Check if application has any questions
     */
    boolean existsByApplicationId(Long applicationId);

    /**
     * Delete all questions for an application
     */
    void deleteByApplicationId(Long applicationId);
}
