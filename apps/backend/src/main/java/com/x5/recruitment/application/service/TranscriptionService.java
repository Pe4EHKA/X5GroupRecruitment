package com.x5.recruitment.application.service;

import com.x5.recruitment.domain.model.Transcription;
import com.x5.recruitment.domain.model.TranscriptionStatus;
import com.x5.recruitment.domain.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Service for handling video transcription using a real external provider (OpenAI Whisper-compatible).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TranscriptionService {

    private final TranscriptionRepository transcriptionRepository;
    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${app.media.storage-path:./media-storage}")
    private String storagePath;

    @Value("${app.transcription.api-url:https://api.openai.com/v1/audio/transcriptions}")
    private String transcriptionApiUrl;

    @Value("${app.transcription.api-key:}")
    private String transcriptionApiKey;

    @Value("${app.transcription.model:whisper-1}")
    private String transcriptionModel;

    @Value("${app.transcription.language:ru}")
    private String transcriptionLanguage;

    @Value("${app.transcription.timeout-ms:60000}")
    private long transcriptionTimeoutMs;

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
     * Perform actual transcription using OpenAI Whisper API
     */
    private String performTranscription(Transcription transcription) {
        validateConfiguration();

        Path mediaPath = resolveMediaPath(transcription);
        RestTemplate restTemplate = buildRestTemplate();

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(mediaPath));
        body.add("model", transcriptionModel);
        body.add("response_format", "text");
        body.add("language", transcriptionLanguage);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(List.of(MediaType.TEXT_PLAIN, MediaType.ALL));
        headers.setBearerAuth(transcriptionApiKey);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(transcriptionApiUrl, requestEntity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("Transcription API returned status " + response.getStatusCode());
            }

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isBlank()) {
                throw new IllegalStateException("Empty transcription response received");
            }

            log.info("Received transcription for media {} ({} bytes)",
                transcription.getMedia().getId(), responseBody.length());

            transcription.setLanguage(transcriptionLanguage);
            return responseBody.strip();
        } catch (RestClientException e) {
            log.error("Failed to transcribe media {}", transcription.getMedia().getId(), e);
            throw new IllegalStateException("Transcription API request failed: " + e.getMessage(), e);
        }
    }

    private void validateConfiguration() {
        if (transcriptionApiKey == null || transcriptionApiKey.isBlank()) {
            throw new IllegalStateException("Transcription API key is not configured");
        }

        if (transcriptionApiUrl == null || transcriptionApiUrl.isBlank()) {
            throw new IllegalStateException("Transcription API URL is not configured");
        }
    }

    private Path resolveMediaPath(Transcription transcription) {
        Objects.requireNonNull(transcription.getMedia(), "Transcription is not linked to media");

        Path storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
        Path mediaPath = storageRoot
            .resolve(transcription.getMedia().getStorageKey())
            .normalize();

        if (!mediaPath.startsWith(storageRoot)) {
            throw new IllegalStateException("Resolved media path escapes storage root: " + mediaPath);
        }

        if (!Files.exists(mediaPath)) {
            throw new IllegalStateException("Media file not found for transcription: " + mediaPath);
        }

        if (!Files.isReadable(mediaPath)) {
            throw new IllegalStateException("Media file is not readable: " + mediaPath);
        }

        try {
            if (Files.size(mediaPath) <= 0) {
                throw new IllegalStateException("Media file is empty: " + mediaPath);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to inspect media file: " + mediaPath, e);
        }

        return mediaPath;
    }

    private RestTemplate buildRestTemplate() {
        return restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(transcriptionTimeoutMs))
            .setReadTimeout(Duration.ofMillis(transcriptionTimeoutMs))
            .build();
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
