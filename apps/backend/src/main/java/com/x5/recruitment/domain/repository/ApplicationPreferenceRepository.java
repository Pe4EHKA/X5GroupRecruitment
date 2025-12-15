package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.ApplicationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ApplicationPreference entity.
 */
@Repository
public interface ApplicationPreferenceRepository extends JpaRepository<ApplicationPreference, Long> {

    /**
     * Find all preferences for an application.
     */
    List<ApplicationPreference> findByApplicationId(Long applicationId);

    /**
     * Find preference by application and rank.
     */
    Optional<ApplicationPreference> findByApplicationIdAndRank(Long applicationId, Integer rank);

    /**
     * Find all unmapped preferences.
     */
    List<ApplicationPreference> findByIsMappedFalse();

    /**
     * Find preferences by vacancy.
     */
    List<ApplicationPreference> findByVacancyId(Long vacancyId);
}
