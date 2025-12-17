package com.x5.recruitment.api.controller;

import com.x5.recruitment.api.dto.questionnaire.MediaResponse;
import com.x5.recruitment.api.dto.questionnaire.VideoUploadResponse;
import com.x5.recruitment.application.service.MediaService;
import com.x5.recruitment.application.service.UserService;
import com.x5.recruitment.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST API for media operations (video upload, streaming).
 */
@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "Media API", description = "Operations for media file management")
public class MediaController {

    private final MediaService mediaService;
    private final UserService userService;

    @Operation(
        summary = "Upload video",
        description = "Upload a video file for a questionnaire answer. " +
                      "Supported formats: webm, mp4. Max size and duration are configured in application properties."
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STAGER', 'CANDIDATE', 'ADMIN')")
    public ResponseEntity<VideoUploadResponse> uploadVideo(
            @Parameter(description = "Video file to upload")
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails principal) {
        
        User user = userService.getUserEntityByUsername(principal.getUsername());
        VideoUploadResponse response = mediaService.uploadVideo(file, user);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get media metadata",
        description = "Get metadata for a media file including transcription status"
    )
    @GetMapping("/{mediaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MediaResponse> getMedia(@PathVariable Long mediaId) {
        MediaResponse media = mediaService.getMedia(mediaId);
        return ResponseEntity.ok(media);
    }

    @Operation(
        summary = "Stream media file",
        description = "Stream a media file (video/audio). Requires authentication."
    )
    @GetMapping("/{mediaId}/stream")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> streamMedia(@PathVariable Long mediaId) {
        Resource resource = mediaService.streamMedia(mediaId);
        String mimeType = mediaService.getMediaMimeType(mediaId);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(mimeType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"video\"")
            .body(resource);
    }
}
