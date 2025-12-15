package com.x5.recruitment.domain.model;

/**
 * User roles in the recruitment system.
 * RBAC implementation for access control.
 */
public enum UserRole {
    /**
     * Administrator with full system access
     */
    ADMIN,
    
    /**
     * Recruiter managing candidate pipeline
     */
    RECRUITER,
    
    /**
     * Hiring Manager making final decisions
     */
    HM,
    
    /**
     * Candidate viewing their application status
     */
    CANDIDATE
}
