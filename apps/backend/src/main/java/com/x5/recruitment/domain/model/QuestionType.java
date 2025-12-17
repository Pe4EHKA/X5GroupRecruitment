package com.x5.recruitment.domain.model;

/**
 * Types of questions that can be asked in a vacancy questionnaire.
 */
public enum QuestionType {
    /**
     * Free text answer
     */
    TEXT,
    
    /**
     * Single choice from predefined options
     */
    SINGLE_CHOICE,
    
    /**
     * Multiple choices from predefined options
     */
    MULTI_CHOICE,
    
    /**
     * Numeric answer
     */
    NUMBER,
    
    /**
     * Date answer
     */
    DATE,
    
    /**
     * Video recording answer
     */
    VIDEO
}
