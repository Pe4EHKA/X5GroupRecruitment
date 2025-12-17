package com.x5.recruitment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.recruitment.api.dto.questionnaire.VacancyQuestionRequest;
import com.x5.recruitment.api.dto.questionnaire.VacancyQuestionResponse;
import com.x5.recruitment.domain.model.Vacancy;
import com.x5.recruitment.domain.model.VacancyQuestion;
import com.x5.recruitment.domain.repository.VacancyQuestionRepository;
import com.x5.recruitment.domain.repository.VacancyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing vacancy questions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VacancyQuestionService {

    private final VacancyQuestionRepository questionRepository;
    private final VacancyRepository vacancyRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create a new question for a vacancy
     */
    public VacancyQuestionResponse createQuestion(Long vacancyId, VacancyQuestionRequest request) {
        log.info("Creating question for vacancy {}", vacancyId);
        
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
            .orElseThrow(() -> new IllegalArgumentException("Vacancy not found: " + vacancyId));

        VacancyQuestion question = VacancyQuestion.builder()
            .vacancy(vacancy)
            .text(request.getText())
            .type(request.getType())
            .mandatory(request.getMandatory() != null ? request.getMandatory() : false)
            .randomPool(request.getRandomPool() != null ? request.getRandomPool() : true)
            .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
            .weight(request.getWeight() != null ? request.getWeight() : 1)
            .build();

        // Serialize validation rules and options to JSON
        if (request.getValidationRules() != null) {
            question.setValidationRules(toJson(request.getValidationRules()));
        }
        if (request.getOptions() != null) {
            question.setOptions(toJson(request.getOptions()));
        }

        question = questionRepository.save(question);
        log.info("Created question {} for vacancy {}", question.getId(), vacancyId);
        
        return toResponse(question);
    }

    /**
     * Update an existing question
     */
    public VacancyQuestionResponse updateQuestion(Long questionId, VacancyQuestionRequest request) {
        log.info("Updating question {}", questionId);
        
        VacancyQuestion question = questionRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));

        question.setText(request.getText());
        question.setType(request.getType());
        if (request.getMandatory() != null) {
            question.setMandatory(request.getMandatory());
        }
        if (request.getRandomPool() != null) {
            question.setRandomPool(request.getRandomPool());
        }
        if (request.getOrderIndex() != null) {
            question.setOrderIndex(request.getOrderIndex());
        }
        if (request.getWeight() != null) {
            question.setWeight(request.getWeight());
        }

        // Update validation rules and options
        if (request.getValidationRules() != null) {
            question.setValidationRules(toJson(request.getValidationRules()));
        } else {
            question.setValidationRules(null);
        }
        
        if (request.getOptions() != null) {
            question.setOptions(toJson(request.getOptions()));
        } else {
            question.setOptions(null);
        }

        question = questionRepository.save(question);
        log.info("Updated question {}", questionId);
        
        return toResponse(question);
    }

    /**
     * Get all questions for a vacancy
     */
    @Transactional(readOnly = true)
    public List<VacancyQuestionResponse> getQuestions(Long vacancyId) {
        log.info("Getting questions for vacancy {}", vacancyId);
        
        return questionRepository.findByVacancyIdOrderByOrderIndexAsc(vacancyId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get a single question
     */
    @Transactional(readOnly = true)
    public VacancyQuestionResponse getQuestion(Long questionId) {
        log.info("Getting question {}", questionId);
        
        VacancyQuestion question = questionRepository.findById(questionId)
            .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
        
        return toResponse(question);
    }

    /**
     * Delete a question
     */
    public void deleteQuestion(Long questionId) {
        log.info("Deleting question {}", questionId);
        
        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("Question not found: " + questionId);
        }
        
        questionRepository.deleteById(questionId);
        log.info("Deleted question {}", questionId);
    }

    /**
     * Reorder questions for a vacancy
     */
    public void reorderQuestions(Long vacancyId, List<Long> questionIds) {
        log.info("Reordering {} questions for vacancy {}", questionIds.size(), vacancyId);
        
        List<VacancyQuestion> questions = questionRepository.findByVacancyIdOrderByOrderIndexAsc(vacancyId);
        
        for (int i = 0; i < questionIds.size(); i++) {
            Long questionId = questionIds.get(i);
            VacancyQuestion question = questions.stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));
            
            question.setOrderIndex(i);
        }
        
        questionRepository.saveAll(questions);
        log.info("Reordered questions for vacancy {}", vacancyId);
    }

    /**
     * Convert question to response DTO
     */
    private VacancyQuestionResponse toResponse(VacancyQuestion question) {
        return VacancyQuestionResponse.builder()
            .id(question.getId())
            .vacancyId(question.getVacancy().getId())
            .text(question.getText())
            .type(question.getType())
            .mandatory(question.getMandatory())
            .randomPool(question.getRandomPool())
            .orderIndex(question.getOrderIndex())
            .validationRules(fromJson(question.getValidationRules(), Map.class))
            .options(fromJson(question.getOptions(), List.class))
            .weight(question.getWeight())
            .createdAt(question.getCreatedAt())
            .updatedAt(question.getUpdatedAt())
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
