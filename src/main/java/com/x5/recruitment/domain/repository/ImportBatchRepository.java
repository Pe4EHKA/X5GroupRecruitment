package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.ImportBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ImportBatch entity.
 */
@Repository
public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {

    /**
     * Find all batches uploaded by a user.
     */
    List<ImportBatch> findByUploadedById(Long uploadedById);

    /**
     * Find batches uploaded after a certain date.
     */
    Page<ImportBatch> findByUploadedAtAfter(LocalDateTime uploadedAt, Pageable pageable);

    /**
     * Find incomplete batches.
     */
    List<ImportBatch> findByCompletedFalse();
}
