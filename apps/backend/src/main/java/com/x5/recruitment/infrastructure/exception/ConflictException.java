package com.x5.recruitment.infrastructure.exception;

/**
 * Exception thrown when there is a conflict (e.g., unique constraint violation).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
