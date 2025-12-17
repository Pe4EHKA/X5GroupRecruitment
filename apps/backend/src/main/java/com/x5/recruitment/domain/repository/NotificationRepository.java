package com.x5.recruitment.domain.repository;

import com.x5.recruitment.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Notification entity (outbox pattern).
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findBySentFalseAndAttemptsLessThan(int maxAttempts);
    
    List<Notification> findByCandidateIdOrderByCreatedAtDesc(Long candidateId);
}
