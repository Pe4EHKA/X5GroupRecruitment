package com.x5.recruitment.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Audit event for tracking user management actions.
 * Immutable record of all administrative actions.
 */
@Entity
@Table(name = "audit_events", indexes = {
    @Index(name = "idx_audit_events_actor", columnList = "actor_user_id"),
    @Index(name = "idx_audit_events_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_events_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_events_action", columnList = "action")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who performed the action (nullable for system actions)
     */
    @Column(name = "actor_user_id")
    private Long actorUserId;

    /**
     * Action performed (e.g., CREATE_USER, UPDATE_USER, DISABLE_USER, etc.)
     */
    @Column(nullable = false, length = 100)
    private String action;

    /**
     * Entity type being acted upon (e.g., USER, APPLICATION, etc.)
     */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /**
     * ID of the entity being acted upon
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * Additional metadata in JSON format
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
