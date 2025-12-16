package com.x5.recruitment.domain.model;

/**
 * User account status.
 * Manages user lifecycle and access control.
 */
public enum UserStatus {
    /**
     * User is active and can access the system
     */
    ACTIVE,
    
    /**
     * User is disabled and cannot access the system
     */
    DISABLED,
    
    /**
     * User has been invited but hasn't activated account yet
     */
    INVITED
}
