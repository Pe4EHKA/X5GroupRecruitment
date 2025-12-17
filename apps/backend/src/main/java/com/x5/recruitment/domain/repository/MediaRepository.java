package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Media entity.
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, Long> {

    /**
     * Find media by storage key
     */
    Optional<Media> findByStorageKey(String storageKey);

    /**
     * Check if media exists by storage key
     */
    boolean existsByStorageKey(String storageKey);
}
