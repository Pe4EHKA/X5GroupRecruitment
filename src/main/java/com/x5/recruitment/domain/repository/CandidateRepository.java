package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Candidate entity.
 */
@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    Optional<Candidate> findByEmail(String email);
    
    Optional<Candidate> findByAccessToken(String accessToken);
    
    boolean existsByEmail(String email);
}
