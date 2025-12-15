package com.x5.recruitment.domain.model;

/**
 * Notification type for candidate communications.
 */
public enum NotificationType {
    /**
     * Application received confirmation
     */
    APPLICATION_RECEIVED,
    
    /**
     * Status changed notification
     */
    STATUS_CHANGED,
    
    /**
     * Interview scheduled
     */
    INTERVIEW_SCHEDULED,
    
    /**
     * Interview reminder
     */
    INTERVIEW_REMINDER,
    
    /**
     * Application approved
     */
    APPROVED,
    
    /**
     * Application rejected with feedback
     */
    REJECTED,
    
    /**
     * General information
     */
    GENERAL
}
