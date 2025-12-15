package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Vacancy entity.
 */
@Repository
public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    
    Optional<Vacancy> findByCode(String code);
    
    Optional<Vacancy> findByTitleIgnoreCase(String title);
    
    List<Vacancy> findByActiveTrue();
    
    boolean existsByCode(String code);
}
