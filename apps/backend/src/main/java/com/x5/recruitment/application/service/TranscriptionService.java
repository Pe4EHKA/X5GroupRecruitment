package com.x5.recruitment.application.service;

import com.x5.recruitment.domain.model.Transcription;
import com.x5.recruitment.domain.model.TranscriptionStatus;
import com.x5.recruitment.domain.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for handling video transcription.
 * In MVP, this is a stub that simulates transcription.
 * In production, this would integrate with a real transcription API (e.g., Google Speech-to-Text, AWS Transcribe).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TranscriptionService {

    private final TranscriptionRepository transcriptionRepository;

    /**
     * Process pending transcriptions (scheduled task)
     * Runs every 60 seconds
     */
    @Scheduled(fixedDelay = 60000)
    public void processPendingTranscriptions() {
        log.debug("Checking for pending transcriptions");

        List<Transcription> pending = transcriptionRepository.findByStatus(TranscriptionStatus.PENDING);
        
        if (pending.isEmpty()) {
            log.debug("No pending transcriptions found");
            return;
        }

        log.info("Found {} pending transcriptions to process", pending.size());

        for (Transcription transcription : pending) {
            try {
                processTranscription(transcription);
            } catch (Exception e) {
                log.error("Error processing transcription {}", transcription.getId(), e);
                handleTranscriptionError(transcription, e.getMessage());
            }
        }
    }

    /**
     * Process a single transcription
     */
    private void processTranscription(Transcription transcription) {
        log.info("Processing transcription {} for media {}", 
            transcription.getId(), transcription.getMedia().getId());

        // Update status to PROCESSING
        transcription.incrementAttempts();
        transcriptionRepository.save(transcription);

        try {
            // In MVP: simulate transcription with a placeholder text
            // In production: call real transcription API here
            String transcribedText = performTranscription(transcription);

            // Mark as completed
            transcription.markCompleted(transcribedText);
            transcriptionRepository.save(transcription);

            log.info("Completed transcription {} for media {}", 
                transcription.getId(), transcription.getMedia().getId());

        } catch (Exception e) {
            log.error("Transcription failed for {}", transcription.getId(), e);
            throw e;
        }
    }

    /**
     * Perform actual transcription (stub in MVP)
     */
    private String performTranscription(Transcription transcription) {
        // MVP: Return placeholder text
        // In production, this would:
        // 1. Get media file from storage
        // 2. Send to transcription API (Google Cloud Speech-to-Text, AWS Transcribe, etc.)
        // 3. Parse and return result
        
        log.info("Simulating transcription for media {}", transcription.getMedia().getId());
        
        // Simulate processing time
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "[MVP Placeholder] This is a simulated transcription of the video content. " +
               "In production, this would contain the actual transcribed text from the video. " +
               "Media ID: " + transcription.getMedia().getId();
    }

    /**
     * Handle transcription error
     */
    private void handleTranscriptionError(Transcription transcription, String errorMessage) {
        final int MAX_ATTEMPTS = 3;

        if (transcription.getAttempts() >= MAX_ATTEMPTS) {
            log.error("Transcription {} failed after {} attempts", 
                transcription.getId(), MAX_ATTEMPTS);
            transcription.markFailed("Failed after " + MAX_ATTEMPTS + " attempts: " + errorMessage);
        } else {
            log.warn("Transcription {} failed, will retry (attempt {}/{})", 
                transcription.getId(), transcription.getAttempts(), MAX_ATTEMPTS);
            // Set back to PENDING for retry
            transcription.setStatus(TranscriptionStatus.PENDING);
        }

        transcriptionRepository.save(transcription);
    }

    /**
     * Retry failed transcription (manually triggered by HR)
     */
    public void retryTranscription(Long transcriptionId) {
        log.info("Retrying transcription {}", transcriptionId);

        Transcription transcription = transcriptionRepository.findById(transcriptionId)
            .orElseThrow(() -> new IllegalArgumentException("Transcription not found: " + transcriptionId));

        if (transcription.getStatus() != TranscriptionStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed transcriptions");
        }

        // Reset status to PENDING
        transcription.setStatus(TranscriptionStatus.PENDING);
        transcription.setErrorMessage(null);
        transcriptionRepository.save(transcription);

        log.info("Transcription {} reset to PENDING for retry", transcriptionId);
    }

    /**
     * Get transcription by media ID
     */
    @Transactional(readOnly = true)
    public Transcription getTranscriptionByMediaId(Long mediaId) {
        return transcriptionRepository.findByMediaId(mediaId)
            .orElseThrow(() -> new IllegalArgumentException("Transcription not found for media: " + mediaId));
    }
}
