package com.x5.recruitment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.recruitment.api.dto.questionnaire.AnswerResponse;
import com.x5.recruitment.api.dto.questionnaire.ApplicationStatisticsResponse;
import com.x5.recruitment.domain.model.*;
import com.x5.recruitment.domain.repository.ApplicationAnswerRepository;
import com.x5.recruitment.domain.repository.ApplicationQuestionRepository;
import com.x5.recruitment.domain.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating statistics about application questionnaires.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationQuestionRepository questionRepository;
    private final ApplicationAnswerRepository answerRepository;
    private final AnswerService answerService;
    private final ObjectMapper objectMapper;

    /**
     * Get statistics for a single application
     */
    @Transactional(readOnly = true)
    public ApplicationStatisticsResponse getApplicationStatistics(Long applicationId) {
        log.info("Getting statistics for application {}", applicationId);

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        List<ApplicationQuestion> questions = 
            questionRepository.findByApplicationIdOrderByOrderSnapshotAsc(applicationId);

        List<ApplicationAnswer> answers = answerRepository.findByApplicationId(applicationId);

        return buildStatistics(application, questions, answers);
    }

    /**
     * Get statistics for all applications of a vacancy
     */
    @Transactional(readOnly = true)
    public List<ApplicationStatisticsResponse> getVacancyStatistics(Long vacancyId) {
        log.info("Getting statistics for vacancy {}", vacancyId);

        List<Application> applications = applicationRepository.findByVacancyId(vacancyId);

        return applications.stream()
            .map(app -> {
                List<ApplicationQuestion> questions = 
                    questionRepository.findByApplicationIdOrderByOrderSnapshotAsc(app.getId());
                List<ApplicationAnswer> answers = answerRepository.findByApplicationId(app.getId());
                return buildStatistics(app, questions, answers);
            })
            .collect(Collectors.toList());
    }

    /**
     * Build statistics response
     */
    private ApplicationStatisticsResponse buildStatistics(
            Application application,
            List<ApplicationQuestion> questions,
            List<ApplicationAnswer> answers) {

        Candidate candidate = application.getCandidate();

        // Count mandatory and optional questions
        long mandatoryCount = questions.stream()
            .filter(ApplicationQuestion::getMandatorySnapshot)
            .count();

        long optionalCount = questions.size() - mandatoryCount;

        // Count answered questions
        long answeredCount = answers.size();

        // Count mandatory answered
        long mandatoryAnswered = answers.stream()
            .filter(a -> a.getApplicationQuestion().getMandatorySnapshot())
            .count();

        long optionalAnswered = answeredCount - mandatoryAnswered;

        // Calculate completion percentage
        double completionPercentage = questions.isEmpty() ? 0.0 : 
            (double) answeredCount / questions.size() * 100.0;

        // Count video questions
        long videoQuestionsTotal = questions.stream()
            .filter(q -> q.getType() == QuestionType.VIDEO)
            .count();

        long videoQuestionsAnswered = answers.stream()
            .filter(a -> a.getApplicationQuestion().getType() == QuestionType.VIDEO)
            .filter(a -> a.getMedia() != null)
            .count();

        boolean hasVideoAnswers = videoQuestionsAnswered > 0;

        // Get all answers as response DTOs
        List<AnswerResponse> answerResponses = answerService.getAnswers(application.getId());

        return ApplicationStatisticsResponse.builder()
            .applicationId(application.getId())
            .candidateId(candidate.getId())
            .candidateName(candidate.getFirstName() + " " + candidate.getLastName())
            .candidateEmail(candidate.getEmail())
            .totalQuestions(questions.size())
            .answeredQuestions((int) answeredCount)
            .mandatoryQuestions((int) mandatoryCount)
            .mandatoryAnswered((int) mandatoryAnswered)
            .optionalQuestions((int) optionalCount)
            .optionalAnswered((int) optionalAnswered)
            .completionPercentage(Math.round(completionPercentage * 100.0) / 100.0)
            .hasVideoAnswers(hasVideoAnswers)
            .videoQuestionsTotal((int) videoQuestionsTotal)
            .videoQuestionsAnswered((int) videoQuestionsAnswered)
            .answers(answerResponses)
            .build();
    }
}
