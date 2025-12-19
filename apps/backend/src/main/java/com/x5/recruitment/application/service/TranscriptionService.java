package com.x5.recruitment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.recruitment.domain.model.Transcription;
import com.x5.recruitment.domain.model.TranscriptionStatus;
import com.x5.recruitment.domain.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for handling video transcription locally using a bundled speech-to-text model.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TranscriptionService {

    private final TranscriptionRepository transcriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.media.storage-path:./media-storage}")
    private String storagePath;

    @Value("${app.transcription.local.model-path:}")
    private String transcriptionModelPath;

    @Value("${app.transcription.language:ru}")
    private String transcriptionLanguage;

    @Value("${app.transcription.local.sample-rate:16000}")
    private int sampleRate;

    /**
     * Optional environment override for the model path. Allows configuring the
     * Vosk model location via container environment without changing
     * application properties (e.g. VOSK_MODEL_PATH=/opt/models/vosk).
     */
    @Value("${VOSK_MODEL_PATH:}")
    private String voskModelPathOverride;

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

    private String performTranscription(Transcription transcription) {
        transcriptionModelPath = resolveModelPath().toString();

        Path mediaPath = resolveMediaPath(transcription);
        Path wavPath = extractAudio(mediaPath);

        try (Model model = new Model(transcriptionModelPath);
             Recognizer recognizer = new Recognizer(model, sampleRate);
             InputStream audioStream = new BufferedInputStream(Files.newInputStream(wavPath))) {

            byte[] buffer = new byte[4096];
            StringJoiner transcriptJoiner = new StringJoiner(" ");
            int read;

            while ((read = audioStream.read(buffer)) != -1) {
                if (recognizer.acceptWaveForm(buffer, read)) {
                    transcriptJoiner.add(extractText(recognizer.getResult()));
                }
            }

            String finalText = extractText(recognizer.getFinalResult());
            if (!finalText.isBlank()) {
                transcriptJoiner.add(finalText);
            }

            String transcriptionText = transcriptJoiner.toString().replaceAll("\\s+", " ").trim();
            log.info("Received transcription for media {} ({} chars)",
                transcription.getMedia().getId(), transcriptionText.length());

            transcription.setLanguage(transcriptionLanguage);
            return transcriptionText;
        } catch (IOException e) {
            log.error("Failed to read audio for transcription {}", transcription.getId(), e);
            throw new IllegalStateException("Unable to read audio stream: " + e.getMessage(), e);
        } finally {
            deleteTempFile(wavPath);
        }
    }

    private String extractText(String recognizerJson) {
        if (recognizerJson == null || recognizerJson.isBlank()) {
            return "";
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(recognizerJson);
            return jsonNode.path("text").asText("").strip();
        } catch (JsonProcessingException e) {
            log.warn("Unable to parse recognizer result: {}", recognizerJson, e);
            return recognizerJson;
        }
    }

    private Path extractAudio(Path mediaPath) {
        Path wavPath;
        try {
            wavPath = Files.createTempFile("transcription-", ".wav");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create temporary audio file", e);
        }

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-i");
        command.add(mediaPath.toString());
        command.add("-ar");
        command.add(String.valueOf(sampleRate));
        command.add("-ac");
        command.add("1");
        command.add("-f");
        command.add("wav");
        command.add(wavPath.toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            List<String> outputLines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLines.add(line);
                }
            }

            boolean finished = process.waitFor(transcriptionTimeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Audio extraction timed out after " + transcriptionTimeoutMs + " ms");
            }

            if (process.exitValue() != 0) {
                throw new IllegalStateException("ffmpeg failed with code " + process.exitValue() + ": " + summarizeOutput(outputLines));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Audio extraction interrupted", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to execute ffmpeg for transcription: " + e.getMessage(), e);
        }

        if (!Files.exists(wavPath) || !Files.isReadable(wavPath)) {
            throw new IllegalStateException("Extracted audio file is not available: " + wavPath);
        }

        try {
            if (Files.size(wavPath) <= 0) {
                throw new IllegalStateException("Extracted audio file is empty: " + wavPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to validate extracted audio: " + wavPath, e);
        }

        return wavPath;
    }

    private String summarizeOutput(List<String> outputLines) {
        if (outputLines.isEmpty()) {
            return "";
        }

        List<String> preview = outputLines.size() > 8
            ? outputLines.subList(0, 8)
            : outputLines;
        return String.join(" | ", preview);
    }

    private void deleteTempFile(Path file) {
        if (file == null) {
            return;
        }

        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Failed to delete temporary file {}", file, e);
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

    private Path resolveModelPath() {
        List<Path> candidateRoots = new ArrayList<>();

        // Priority 1: explicit property
        if (transcriptionModelPath != null && !transcriptionModelPath.isBlank()) {
            candidateRoots.add(Paths.get(transcriptionModelPath));
        }

        // Priority 2: environment override used in container deployments
        if (voskModelPathOverride != null && !voskModelPathOverride.isBlank()) {
            candidateRoots.add(Paths.get(voskModelPathOverride));
        }

        // Priority 3: common repo locations (root-level and module-local)
        candidateRoots.add(Paths.get("./transcription-model"));
        candidateRoots.add(Paths.get("apps", "backend", "transcription-model"));

        // Priority 4: default next to media storage (mounted volume inside container)
        candidateRoots.add(Paths.get(storagePath).resolve("transcription-model"));

        Set<String> inspected = new HashSet<>();

        for (Path candidateRoot : candidateRoots) {
            Path modelRoot = detectModelRoot(candidateRoot);
            if (modelRoot != null) {
                return modelRoot;
            }

            inspected.add(candidateRoot.toAbsolutePath().normalize().toString());
        }

        String searchedLocations = inspected.stream()
            .sorted()
            .collect(Collectors.joining(", "));

        throw new IllegalStateException("Local transcription model not found. Searched: " + searchedLocations
            + ". Provide app.transcription.local.model-path or mount a model directory.");
    }

    private Path detectModelRoot(Path candidateRoot) {
        if (candidateRoot == null) {
            return null;
        }

        Path normalized = candidateRoot.toAbsolutePath().normalize();
        if (!Files.exists(normalized)) {
            return null;
        }

        if (isValidModelRoot(normalized)) {
            return normalized;
        }

        if (!Files.isDirectory(normalized)) {
            return null;
        }

        try (Stream<Path> children = Files.list(normalized)) {
            Optional<Path> firstModelDir = children
                .filter(Files::isDirectory)
                .sorted()
                .filter(this::isValidModelRoot)
                .findFirst();

            if (firstModelDir.isPresent()) {
                return firstModelDir.get().toAbsolutePath().normalize();
            }
        } catch (IOException e) {
            log.warn("Unable to inspect model directory {}", normalized, e);
        }

        if (!Files.isDirectory(normalized)) {
            return null;
        }

        try (Stream<Path> children = Files.list(normalized)) {
            Optional<Path> firstModelDir = children
                .filter(Files::isDirectory)
                .sorted()
                .filter(this::isModelRoot)
                .findFirst();

            if (firstModelDir.isPresent()) {
                return firstModelDir.get().toAbsolutePath().normalize();
            }
        } catch (IOException e) {
            log.warn("Unable to inspect model directory {}", normalized, e);
        }

        if (isModelRoot(normalized)) {
            return normalized;
        }

        if (!Files.isDirectory(normalized)) {
            return null;
        }

        try (Stream<Path> children = Files.list(normalized)) {
            Optional<Path> firstModelDir = children
                .filter(Files::isDirectory)
                .sorted()
                .filter(this::isModelRoot)
                .findFirst();

            if (firstModelDir.isPresent()) {
                return firstModelDir.get().toAbsolutePath().normalize();
            }
        } catch (IOException e) {
            log.warn("Unable to inspect model directory {}", normalized, e);
        }

        if (!Files.isDirectory(normalized)) {
            return null;
        }

        try (Stream<Path> children = Files.list(normalized)) {
            List<Path> subdirectories = children
                .filter(Files::isDirectory)
                .limit(2)
                .toList();

            if (subdirectories.size() == 1 && isModelRoot(subdirectories.get(0))) {
                return subdirectories.get(0).toAbsolutePath().normalize();
            }
        } catch (IOException e) {
            log.warn("Unable to inspect model directory {}", normalized, e);
        }

        throw new IllegalStateException("Local transcription model not found: " + errorPath
            + ". Provide app.transcription.local.model-path or mount a model directory.");
    }

    private boolean isModelRoot(Path candidate) {
        if (candidate == null || !Files.isDirectory(candidate)) {
            return false;
        }

        Path confDir = candidate.resolve("conf");
        Path acousticDir = candidate.resolve("am");
        Path graphDir = candidate.resolve("graph");

        return Files.isDirectory(confDir)
            && Files.exists(confDir.resolve("model.conf"))
            && Files.isDirectory(acousticDir)
            && Files.isDirectory(graphDir);
    }

    private boolean isModelRoot(Path candidate) {
        if (candidate == null || !Files.isDirectory(candidate)) {
            return false;
        }

        Path confDir = candidate.resolve("conf");
        Path acousticDir = candidate.resolve("am");
        Path graphDir = candidate.resolve("graph");

        return Files.isDirectory(confDir)
            && Files.exists(confDir.resolve("model.conf"))
            && Files.isDirectory(acousticDir)
            && Files.isDirectory(graphDir);
    }

    private boolean isModelRoot(Path candidate) {
        if (candidate == null || !Files.isDirectory(candidate)) {
            return false;
        }

        Path confDir = candidate.resolve("conf");
        Path acousticDir = candidate.resolve("am");
        Path graphDir = candidate.resolve("graph");

        return Files.isDirectory(confDir)
            && Files.exists(confDir.resolve("model.conf"))
            && Files.isDirectory(acousticDir)
            && Files.isDirectory(graphDir);
    }

    private boolean isValidModelRoot(Path candidate) {
        if (candidate == null || !Files.isDirectory(candidate)) {
            return false;
        }

        Path confDir = candidate.resolve("conf");
        Path acousticDir = candidate.resolve("am");
        Path graphDir = candidate.resolve("graph");

        return Files.isDirectory(confDir)
            && Files.exists(confDir.resolve("model.conf"))
            && Files.isDirectory(acousticDir)
            && Files.isDirectory(graphDir);
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
