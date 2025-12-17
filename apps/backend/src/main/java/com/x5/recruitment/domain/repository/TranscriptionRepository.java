package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.Transcription;
import com.x5.recruitment.domain.model.TranscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Transcription entity.
 */
@Repository
public interface TranscriptionRepository extends JpaRepository<Transcription, Long> {

    /**
     * Find transcription by media ID
     */
    Optional<Transcription> findByMediaId(Long mediaId);

    /**
     * Find all transcriptions with a specific status
     */
    List<Transcription> findByStatus(TranscriptionStatus status);

    /**
     * Find pending transcriptions (for processing)
     */
    List<Transcription> findByStatusInOrderByCreatedAtAsc(List<TranscriptionStatus> statuses);

    /**
     * Check if transcription exists for media
     */
    boolean existsByMediaId(Long mediaId);
}
