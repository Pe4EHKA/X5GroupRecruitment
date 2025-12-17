package com.x5.recruitment.domain.model;

/**
 * Status of video transcription processing.
 */
public enum TranscriptionStatus {
    /**
     * Transcription job is pending, waiting to be processed
     */
    PENDING,
    
    /**
     * Transcription is currently being processed
     */
    PROCESSING,
    
    /**
     * Transcription completed successfully
     */
    DONE,
    
    /**
     * Transcription failed
     */
    FAILED
}
