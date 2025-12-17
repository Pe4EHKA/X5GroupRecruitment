package com.x5.recruitment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.recruitment.api.dto.questionnaire.QuestionnaireQuestionDto;
import com.x5.recruitment.api.dto.questionnaire.QuestionnaireResponse;
import com.x5.recruitment.domain.model.*;
import com.x5.recruitment.domain.repository.ApplicationQuestionRepository;
import com.x5.recruitment.domain.repository.ApplicationRepository;
import com.x5.recruitment.domain.repository.VacancyQuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing questionnaires and question selection for applications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class QuestionnaireService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationQuestionRepository applicationQuestionRepository;
    private final VacancyQuestionRepository vacancyQuestionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Generate and save questionnaire for an application.
     * Includes all mandatory questions + random selection of optional questions.
     * This is called once when an application is created and the questionnaire is fixed.
     */
    public QuestionnaireResponse generateQuestionnaire(Long applicationId) {
        log.info("Generating questionnaire for application {}", applicationId);

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        // Check if questionnaire already exists
        if (applicationQuestionRepository.existsByApplicationId(applicationId)) {
            log.info("Questionnaire already exists for application {}, returning existing", applicationId);
            return getQuestionnaire(applicationId);
        }

        Vacancy vacancy = application.getVacancy();
        
        // Get all mandatory questions
        List<VacancyQuestion> mandatoryQuestions = 
            vacancyQuestionRepository.findByVacancyIdAndMandatoryTrueOrderByOrderIndexAsc(vacancy.getId());

        // Get optional questions eligible for random selection
        List<VacancyQuestion> optionalQuestions = 
            vacancyQuestionRepository.findOptionalQuestionsForRandomSelection(vacancy.getId());

        // Determine how many optional questions to select
        int optionalToSelect = vacancy.getOptionalQuestionsToAsk() != null 
            ? vacancy.getOptionalQuestionsToAsk() : 3;
        
        // Randomly select optional questions
        List<VacancyQuestion> selectedOptional = selectRandomQuestions(optionalQuestions, optionalToSelect);

        // Combine and save as ApplicationQuestions
        List<ApplicationQuestion> applicationQuestions = new ArrayList<>();
        int order = 0;

        // Add mandatory questions
        for (VacancyQuestion vq : mandatoryQuestions) {
            ApplicationQuestion aq = createApplicationQuestion(application, vq, true, order++);
            applicationQuestions.add(aq);
        }

        // Add selected optional questions
        for (VacancyQuestion vq : selectedOptional) {
            ApplicationQuestion aq = createApplicationQuestion(application, vq, false, order++);
            applicationQuestions.add(aq);
        }

        applicationQuestionRepository.saveAll(applicationQuestions);
        log.info("Generated questionnaire with {} questions ({} mandatory, {} optional) for application {}", 
            applicationQuestions.size(), mandatoryQuestions.size(), selectedOptional.size(), applicationId);

        return buildQuestionnaireResponse(application, applicationQuestions);
    }

    /**
     * Get existing questionnaire for an application
     */
    @Transactional(readOnly = true)
    public QuestionnaireResponse getQuestionnaire(Long applicationId) {
        log.info("Getting questionnaire for application {}", applicationId);

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        List<ApplicationQuestion> questions = 
            applicationQuestionRepository.findByApplicationIdOrderByOrderSnapshotAsc(applicationId);

        if (questions.isEmpty()) {
            log.warn("No questionnaire found for application {}, generating new", applicationId);
            return generateQuestionnaire(applicationId);
        }

        return buildQuestionnaireResponse(application, questions);
    }

    /**
     * Select random questions from a list
     */
    private List<VacancyQuestion> selectRandomQuestions(List<VacancyQuestion> questions, int count) {
        if (questions.size() <= count) {
            return new ArrayList<>(questions);
        }

        // Use a seeded random for reproducibility within a session (optional)
        List<VacancyQuestion> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled, new Random());
        
        return shuffled.subList(0, count);
    }

    /**
     * Create an ApplicationQuestion from a VacancyQuestion
     */
    private ApplicationQuestion createApplicationQuestion(
            Application application, 
            VacancyQuestion vacancyQuestion, 
            boolean mandatory, 
            int order) {
        
        return ApplicationQuestion.builder()
            .application(application)
            .question(vacancyQuestion)
            .text(vacancyQuestion.getText())
            .type(vacancyQuestion.getType())
            .mandatorySnapshot(mandatory)
            .orderSnapshot(order)
            .validationRulesSnapshot(vacancyQuestion.getValidationRules())
            .optionsSnapshot(vacancyQuestion.getOptions())
            .build();
    }

    /**
     * Build questionnaire response DTO
     */
    private QuestionnaireResponse buildQuestionnaireResponse(
            Application application, 
            List<ApplicationQuestion> questions) {
        
        List<QuestionnaireQuestionDto> questionDtos = questions.stream()
            .map(this::toQuestionDto)
            .collect(Collectors.toList());

        long mandatoryCount = questions.stream()
            .filter(ApplicationQuestion::getMandatorySnapshot)
            .count();

        return QuestionnaireResponse.builder()
            .applicationId(application.getId())
            .vacancyId(application.getVacancy().getId())
            .vacancyTitle(application.getVacancy().getTitle())
            .questions(questionDtos)
            .totalQuestions(questions.size())
            .mandatoryQuestions((int) mandatoryCount)
            .optionalQuestions(questions.size() - (int) mandatoryCount)
            .build();
    }

    /**
     * Convert ApplicationQuestion to DTO
     */
    private QuestionnaireQuestionDto toQuestionDto(ApplicationQuestion aq) {
        return QuestionnaireQuestionDto.builder()
            .questionId(aq.getId())
            .originalQuestionId(aq.getQuestion() != null ? aq.getQuestion().getId() : null)
            .text(aq.getText())
            .type(aq.getType())
            .mandatory(aq.getMandatorySnapshot())
            .orderIndex(aq.getOrderSnapshot())
            .validationRules(fromJson(aq.getValidationRulesSnapshot(), Map.class))
            .options(fromJson(aq.getOptionsSnapshot(), List.class))
            .build();
    }

    /**
     * Parse JSON string to object
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
