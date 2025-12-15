package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.ImportRowError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for ImportRowError entity.
 */
@Repository
public interface ImportRowErrorRepository extends JpaRepository<ImportRowError, Long> {

    /**
     * Find all errors for a batch.
     */
    Page<ImportRowError> findByBatchId(Long batchId, Pageable pageable);

    /**
     * Find all errors for a batch.
     */
    List<ImportRowError> findByBatchId(Long batchId);

    /**
     * Count errors by batch.
     */
    long countByBatchId(Long batchId);

    /**
     * Find errors by error code.
     */
    List<ImportRowError> findByErrorCode(String errorCode);
}
