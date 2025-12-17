package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.VacancyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for VacancyQuestion entity.
 */
@Repository
public interface VacancyQuestionRepository extends JpaRepository<VacancyQuestion, Long> {

    /**
     * Find all questions for a vacancy, ordered by order_index
     */
    List<VacancyQuestion> findByVacancyIdOrderByOrderIndexAsc(Long vacancyId);

    /**
     * Find mandatory questions for a vacancy
     */
    List<VacancyQuestion> findByVacancyIdAndMandatoryTrueOrderByOrderIndexAsc(Long vacancyId);

    /**
     * Find optional questions eligible for random selection
     */
    @Query("SELECT q FROM VacancyQuestion q WHERE q.vacancy.id = :vacancyId " +
           "AND q.mandatory = false AND q.randomPool = true ORDER BY q.orderIndex ASC")
    List<VacancyQuestion> findOptionalQuestionsForRandomSelection(@Param("vacancyId") Long vacancyId);

    /**
     * Count questions by vacancy
     */
    long countByVacancyId(Long vacancyId);

    /**
     * Delete all questions for a vacancy
     */
    void deleteByVacancyId(Long vacancyId);
}
