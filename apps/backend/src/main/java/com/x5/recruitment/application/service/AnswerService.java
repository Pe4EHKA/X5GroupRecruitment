package com.x5.recruitment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.recruitment.api.dto.questionnaire.*;
import com.x5.recruitment.domain.model.*;
import com.x5.recruitment.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing answers to questionnaire questions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnswerService {

    private final ApplicationQuestionRepository questionRepository;
    private final ApplicationAnswerRepository answerRepository;
    private final MediaRepository mediaRepository;
    private final AnswerValidationService validationService;
    private final ObjectMapper objectMapper;

    /**
     * Submit batch of answers for an application
     */
    public List<AnswerResponse> submitAnswers(Long applicationId, BatchAnswerRequest request) {
        log.info("Submitting {} answers for application {}", request.getAnswers().size(), applicationId);

        // Get all questions for this application
        List<ApplicationQuestion> questions = 
            questionRepository.findByApplicationIdOrderByOrderSnapshotAsc(applicationId);

        if (questions.isEmpty()) {
            throw new IllegalArgumentException("No questionnaire found for application: " + applicationId);
        }

        // Validate all answers
        ValidationErrorResponse validationErrors = 
            validationService.validateAnswers(questions, request.getAnswers());
        
        if (validationErrors != null) {
            throw new IllegalArgumentException("Validation failed: " + validationErrors.getMessage());
        }

        // Save answers
        List<ApplicationAnswer> savedAnswers = new ArrayList<>();
        
        for (AnswerRequest answerRequest : request.getAnswers()) {
            ApplicationQuestion question = questions.stream()
                .filter(q -> q.getId().equals(answerRequest.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                    "Question not found: " + answerRequest.getQuestionId()));

            ApplicationAnswer answer = saveOrUpdateAnswer(question, answerRequest);
            savedAnswers.add(answer);
        }

        log.info("Saved {} answers for application {}", savedAnswers.size(), applicationId);

        return savedAnswers.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all answers for an application
     */
    @Transactional(readOnly = true)
    public List<AnswerResponse> getAnswers(Long applicationId) {
        log.info("Getting answers for application {}", applicationId);

        List<ApplicationAnswer> answers = answerRepository.findByApplicationId(applicationId);
        
        return answers.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get answer for a specific question
     */
    @Transactional(readOnly = true)
    public AnswerResponse getAnswer(Long questionId) {
        log.info("Getting answer for question {}", questionId);

        ApplicationAnswer answer = answerRepository.findByApplicationQuestionId(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Answer not found for question: " + questionId));

        return toResponse(answer);
    }

    /**
     * Save or update an answer
     */
    private ApplicationAnswer saveOrUpdateAnswer(
            ApplicationQuestion question, 
            AnswerRequest request) {

        // Find existing answer or create new
        ApplicationAnswer answer = answerRepository.findByApplicationQuestionId(question.getId())
            .orElse(ApplicationAnswer.builder()
                .applicationQuestion(question)
                .build());

        // Set answer values based on question type
        switch (question.getType()) {
            case TEXT:
                answer.setTextValue(request.getTextValue());
                break;
            case NUMBER:
                answer.setNumberValue(request.getNumberValue());
                break;
            case DATE:
                answer.setDateValue(request.getDateValue());
                break;
            case SINGLE_CHOICE:
            case MULTI_CHOICE:
                answer.setChoiceValues(toJson(request.getChoiceValues()));
                break;
            case VIDEO:
                if (request.getMediaId() != null) {
                    Media media = mediaRepository.findById(request.getMediaId())
                        .orElseThrow(() -> new IllegalArgumentException(
                            "Media not found: " + request.getMediaId()));
                    answer.setMedia(media);
                }
                break;
        }

        return answerRepository.save(answer);
    }

    /**
     * Convert answer to response DTO
     */
    private AnswerResponse toResponse(ApplicationAnswer answer) {
        ApplicationQuestion question = answer.getApplicationQuestion();

        AnswerResponse.AnswerResponseBuilder builder = AnswerResponse.builder()
            .id(answer.getId())
            .questionId(question.getId())
            .questionText(question.getText())
            .questionType(question.getType())
            .textValue(answer.getTextValue())
            .numberValue(answer.getNumberValue())
            .dateValue(answer.getDateValue())
            .createdAt(answer.getCreatedAt())
            .updatedAt(answer.getUpdatedAt());

        // Parse choice values
        if (answer.getChoiceValues() != null) {
            builder.choiceValues(fromJson(answer.getChoiceValues(), List.class));
        }

        // Add media if present
        if (answer.getMedia() != null) {
            builder.media(toMediaResponse(answer.getMedia()));
        }

        return builder.build();
    }

    /**
     * Convert Media to MediaResponse (simplified, will be enhanced by MediaService)
     */
    private MediaResponse toMediaResponse(Media media) {
        return MediaResponse.builder()
            .id(media.getId())
            .storageKey(media.getStorageKey())
            .mimeType(media.getMimeType())
            .fileSize(media.getFileSize())
            .duration(media.getDuration())
            .streamUrl("/api/media/" + media.getId() + "/stream")
            .createdAt(media.getCreatedAt())
            .build();
    }

    /**
     * Convert object to JSON string
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting to JSON", e);
            throw new RuntimeException("Error converting to JSON", e);
        }
    }

    /**
     * Convert JSON string to object
     */
    private <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON", e);
            return null;
        }
    }
}
