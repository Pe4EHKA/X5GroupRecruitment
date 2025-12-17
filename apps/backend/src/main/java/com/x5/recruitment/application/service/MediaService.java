package com.x5.recruitment.application.service;

import com.x5.recruitment.api.dto.questionnaire.MediaResponse;
import com.x5.recruitment.api.dto.questionnaire.TranscriptionResponse;
import com.x5.recruitment.api.dto.questionnaire.VideoUploadResponse;
import com.x5.recruitment.domain.model.Media;
import com.x5.recruitment.domain.model.Transcription;
import com.x5.recruitment.domain.model.TranscriptionStatus;
import com.x5.recruitment.domain.model.User;
import com.x5.recruitment.domain.repository.MediaRepository;
import com.x5.recruitment.domain.repository.TranscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service for managing media files (videos, documents, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MediaService {

    private final MediaRepository mediaRepository;
    private final TranscriptionRepository transcriptionRepository;

    @Value("${app.media.storage-path:./media-storage}")
    private String storagePath;

    @Value("${app.media.max-video-size:104857600}") // 100MB default
    private long maxVideoSize;

    @Value("${app.media.max-video-duration:300}") // 5 minutes default
    private int maxVideoDuration;

    /**
     * Upload a video file
     */
    public VideoUploadResponse uploadVideo(MultipartFile file, User uploadedBy) {
        log.info("Uploading video file: {} (size: {} bytes)", file.getOriginalFilename(), file.getSize());

        // Validate file
        validateVideoFile(file);

        try {
            // Generate unique storage key
            String fileName = generateFileName(file.getOriginalFilename());
            String storageKey = "videos/" + fileName;

            // Save file to storage
            Path filePath = saveFile(file, storageKey);

            // Create media record
            Media media = Media.builder()
                .storageKey(storageKey)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .createdBy(uploadedBy)
                .build();

            media = mediaRepository.save(media);
            log.info("Saved media record with ID: {}", media.getId());

            // Create transcription record (PENDING status)
            Transcription transcription = Transcription.builder()
                .media(media)
                .status(TranscriptionStatus.PENDING)
                .build();
            transcriptionRepository.save(transcription);
            log.info("Created transcription record for media {}", media.getId());

            return VideoUploadResponse.builder()
                .mediaId(media.getId())
                .storageKey(storageKey)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .message("Video uploaded successfully")
                .transcriptionInitiated(true)
                .build();

        } catch (IOException e) {
            log.error("Error saving video file", e);
            throw new RuntimeException("Failed to save video file", e);
        }
    }

    /**
     * Get media by ID
     */
    @Transactional(readOnly = true)
    public MediaResponse getMedia(Long mediaId) {
        log.info("Getting media {}", mediaId);

        Media media = mediaRepository.findById(mediaId)
            .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));

        return toResponse(media);
    }

    /**
     * Stream media file
     */
    @Transactional(readOnly = true)
    public Resource streamMedia(Long mediaId) {
        log.info("Streaming media {}", mediaId);

        Media media = mediaRepository.findById(mediaId)
            .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));

        try {
            Path filePath = getFilePath(media.getStorageKey());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Media file not found or not readable: " + media.getStorageKey());
            }
        } catch (Exception e) {
            log.error("Error streaming media", e);
            throw new RuntimeException("Error streaming media", e);
        }
    }

    /**
     * Get MIME type for media
     */
    @Transactional(readOnly = true)
    public String getMediaMimeType(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
            .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));
        return media.getMimeType();
    }

    /**
     * Validate video file
     */
    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxVideoSize) {
            throw new IllegalArgumentException(
                "File size exceeds maximum allowed: " + maxVideoSize + " bytes");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("video/"))) {
            throw new IllegalArgumentException("File must be a video");
        }

        // TODO: Production enhancement - Add server-side content verification
        // using file magic numbers or Apache Tika to prevent MIME type spoofing
    }

    /**
     * Save file to storage
     */
    private Path saveFile(MultipartFile file, String storageKey) throws IOException {
        Path targetPath = getFilePath(storageKey);
        
        // Create directories if they don't exist
        Files.createDirectories(targetPath.getParent());
        
        // Copy file to target location
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("Saved file to: {}", targetPath);
        return targetPath;
    }

    /**
     * Get file path for storage key
     */
    private Path getFilePath(String storageKey) {
        return Paths.get(storagePath).resolve(storageKey).normalize();
    }

    /**
     * Generate unique file name
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Convert Media to response DTO
     */
    private MediaResponse toResponse(Media media) {
        MediaResponse.MediaResponseBuilder builder = MediaResponse.builder()
            .id(media.getId())
            .storageKey(media.getStorageKey())
            .mimeType(media.getMimeType())
            .fileSize(media.getFileSize())
            .duration(media.getDuration())
            .streamUrl("/api/media/" + media.getId() + "/stream")
            .createdAt(media.getCreatedAt());

        // Add transcription if exists
        transcriptionRepository.findByMediaId(media.getId())
            .ifPresent(t -> builder.transcription(toTranscriptionResponse(t)));

        return builder.build();
    }

    /**
     * Convert Transcription to response DTO
     */
    private TranscriptionResponse toTranscriptionResponse(Transcription transcription) {
        return TranscriptionResponse.builder()
            .id(transcription.getId())
            .status(transcription.getStatus())
            .text(transcription.getText())
            .language(transcription.getLanguage())
            .errorMessage(transcription.getErrorMessage())
            .attempts(transcription.getAttempts())
            .createdAt(transcription.getCreatedAt())
            .updatedAt(transcription.getUpdatedAt())
            .build();
    }
}
