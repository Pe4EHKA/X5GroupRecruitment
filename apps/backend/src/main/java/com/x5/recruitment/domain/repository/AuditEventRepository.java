package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditEvent entity.
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    
    /**
     * Find all audit events for a specific entity
     */
    Page<AuditEvent> findByEntityTypeAndEntityIdOrderByTimestampDesc(
        String entityType, 
        Long entityId, 
        Pageable pageable
    );
    
    /**
     * Find all audit events by actor
     */
    Page<AuditEvent> findByActorUserIdOrderByTimestampDesc(
        Long actorUserId, 
        Pageable pageable
    );
    
    /**
     * Find audit events by action type
     */
    List<AuditEvent> findByActionAndTimestampAfter(
        String action, 
        LocalDateTime after
    );
}
