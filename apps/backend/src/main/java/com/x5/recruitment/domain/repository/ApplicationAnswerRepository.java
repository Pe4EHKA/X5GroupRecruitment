package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.ApplicationAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ApplicationAnswer entity.
 */
@Repository
public interface ApplicationAnswerRepository extends JpaRepository<ApplicationAnswer, Long> {

    /**
     * Find answer for a specific application question
     */
    Optional<ApplicationAnswer> findByApplicationQuestionId(Long applicationQuestionId);

    /**
     * Find all answers for an application
     */
    @Query("SELECT a FROM ApplicationAnswer a JOIN a.applicationQuestion aq " +
           "WHERE aq.application.id = :applicationId ORDER BY aq.orderSnapshot ASC")
    List<ApplicationAnswer> findByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * Count answered questions for an application
     */
    @Query("SELECT COUNT(a) FROM ApplicationAnswer a JOIN a.applicationQuestion aq " +
           "WHERE aq.application.id = :applicationId")
    long countByApplicationId(@Param("applicationId") Long applicationId);

    /**
     * Check if answer exists for a question
     */
    boolean existsByApplicationQuestionId(Long applicationQuestionId);

    /**
     * Delete answer for a question
     */
    void deleteByApplicationQuestionId(Long applicationQuestionId);
}
