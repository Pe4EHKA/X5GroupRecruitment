package com.x5.recruitment.domain.model;

/**
 * Application status in the recruitment pipeline.
 * Represents the workflow stages from initial application to final decision.
 */
public enum ApplicationStatus {
    /**
     * Application submitted but not yet reviewed
     */
    NEW,
    
    /**
     * Application is being screened by recruiter
     */
    SCREENING,
    
    /**
     * Scheduled for interview
     */
    INTERVIEW_SCHEDULED,
    
    /**
     * Interview completed, awaiting decision
     */
    INTERVIEW_COMPLETED,
    
    /**
     * Application approved, candidate accepted
     */
    APPROVED,
    
    /**
     * Application rejected
     */
    REJECTED,
    
    /**
     * Application withdrawn by candidate
     */
    WITHDRAWN,
    
    /**
     * On hold for future consideration
     */
    ON_HOLD
}
