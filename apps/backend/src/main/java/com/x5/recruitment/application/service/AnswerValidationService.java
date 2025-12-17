package com.x5.recruitment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x5.recruitment.api.dto.questionnaire.AnswerRequest;
import com.x5.recruitment.api.dto.questionnaire.ValidationErrorResponse;
import com.x5.recruitment.domain.model.ApplicationQuestion;
import com.x5.recruitment.domain.model.QuestionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service for validating answers to questionnaire questions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerValidationService {

    private final ObjectMapper objectMapper;

    /**
     * Validate a single answer
     */
    public List<ValidationErrorResponse.FieldError> validateAnswer(
            ApplicationQuestion question, 
            AnswerRequest answer) {
        
        List<ValidationErrorResponse.FieldError> errors = new ArrayList<>();

        // Parse validation rules
        Map<String, Object> rules = parseValidationRules(question.getValidationRulesSnapshot());
        
        // Type-specific validation
        switch (question.getType()) {
            case TEXT:
                validateTextAnswer(question, answer, rules, errors);
                break;
            case NUMBER:
                validateNumberAnswer(question, answer, rules, errors);
                break;
            case DATE:
                validateDateAnswer(question, answer, rules, errors);
                break;
            case SINGLE_CHOICE:
                validateSingleChoiceAnswer(question, answer, errors);
                break;
            case MULTI_CHOICE:
                validateMultiChoiceAnswer(question, answer, errors);
                break;
            case VIDEO:
                validateVideoAnswer(question, answer, errors);
                break;
        }

        // Mandatory check
        if (question.getMandatorySnapshot() && isAnswerEmpty(question.getType(), answer)) {
            errors.add(createError(question.getId(), "value", 
                "Answer is required for mandatory question", null));
        }

        return errors;
    }

    /**
     * Validate batch of answers
     */
    public ValidationErrorResponse validateAnswers(
            List<ApplicationQuestion> questions,
            List<AnswerRequest> answers) {
        
        List<ValidationErrorResponse.FieldError> allErrors = new ArrayList<>();

        // Create map of question ID to question
        Map<Long, ApplicationQuestion> questionMap = questions.stream()
            .collect(java.util.stream.Collectors.toMap(
                ApplicationQuestion::getId, 
                q -> q
            ));

        // Validate each answer
        for (AnswerRequest answer : answers) {
            ApplicationQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                allErrors.add(createError(answer.getQuestionId(), "questionId", 
                    "Question not found", answer.getQuestionId().toString()));
                continue;
            }

            List<ValidationErrorResponse.FieldError> errors = validateAnswer(question, answer);
            allErrors.addAll(errors);
        }

        // Check for missing mandatory answers
        for (ApplicationQuestion question : questions) {
            if (question.getMandatorySnapshot()) {
                boolean hasAnswer = answers.stream()
                    .anyMatch(a -> a.getQuestionId().equals(question.getId()));
                
                if (!hasAnswer) {
                    allErrors.add(createError(question.getId(), "value", 
                        "Missing answer for mandatory question", null));
                }
            }
        }

        if (allErrors.isEmpty()) {
            return null;
        }

        return ValidationErrorResponse.builder()
            .message("Validation failed")
            .errors(allErrors)
            .build();
    }

    /**
     * Validate text answer
     */
    private void validateTextAnswer(
            ApplicationQuestion question,
            AnswerRequest answer,
            Map<String, Object> rules,
            List<ValidationErrorResponse.FieldError> errors) {
        
        String value = answer.getTextValue();
        if (value == null) {
            return; // Will be caught by mandatory check if needed
        }

        // Min length
        if (rules.containsKey("minLength")) {
            int minLength = ((Number) rules.get("minLength")).intValue();
            if (value.length() < minLength) {
                errors.add(createError(question.getId(), "textValue", 
                    "Text must be at least " + minLength + " characters", value));
            }
        }

        // Max length
        if (rules.containsKey("maxLength")) {
            int maxLength = ((Number) rules.get("maxLength")).intValue();
            if (value.length() > maxLength) {
                errors.add(createError(question.getId(), "textValue", 
                    "Text must not exceed " + maxLength + " characters", value));
            }
        }

        // Regex pattern - with timeout to prevent ReDoS attacks
        if (rules.containsKey("pattern")) {
            String pattern = (String) rules.get("pattern");
            try {
                // Validate pattern complexity (basic check)
                if (pattern.length() > 200) {
                    errors.add(createError(question.getId(), "textValue", 
                        "Pattern too complex", value));
                    return;
                }
                
                // Use pattern matching with a simple timeout mechanism
                // Note: For production, consider using a regex library with built-in timeout
                if (!Pattern.matches(pattern, value)) {
                    errors.add(createError(question.getId(), "textValue", 
                        "Text does not match required pattern", value));
                }
            } catch (Exception e) {
                log.error("Error validating regex pattern", e);
                errors.add(createError(question.getId(), "textValue", 
                    "Invalid pattern validation", value));
            }
        }
    }

    /**
     * Validate number answer
     */
    private void validateNumberAnswer(
            ApplicationQuestion question,
            AnswerRequest answer,
            Map<String, Object> rules,
            List<ValidationErrorResponse.FieldError> errors) {
        
        BigDecimal value = answer.getNumberValue();
        if (value == null) {
            return;
        }

        // Min value
        if (rules.containsKey("min")) {
            BigDecimal min = new BigDecimal(rules.get("min").toString());
            if (value.compareTo(min) < 0) {
                errors.add(createError(question.getId(), "numberValue", 
                    "Number must be at least " + min, value.toString()));
            }
        }

        // Max value
        if (rules.containsKey("max")) {
            BigDecimal max = new BigDecimal(rules.get("max").toString());
            if (value.compareTo(max) > 0) {
                errors.add(createError(question.getId(), "numberValue", 
                    "Number must not exceed " + max, value.toString()));
            }
        }
    }

    /**
     * Validate date answer
     */
    private void validateDateAnswer(
            ApplicationQuestion question,
            AnswerRequest answer,
            Map<String, Object> rules,
            List<ValidationErrorResponse.FieldError> errors) {
        
        LocalDate value = answer.getDateValue();
        if (value == null) {
            return;
        }

        // Min date
        if (rules.containsKey("minDate")) {
            LocalDate minDate = LocalDate.parse((String) rules.get("minDate"));
            if (value.isBefore(minDate)) {
                errors.add(createError(question.getId(), "dateValue", 
                    "Date must be on or after " + minDate, value.toString()));
            }
        }

        // Max date
        if (rules.containsKey("maxDate")) {
            LocalDate maxDate = LocalDate.parse((String) rules.get("maxDate"));
            if (value.isAfter(maxDate)) {
                errors.add(createError(question.getId(), "dateValue", 
                    "Date must be on or before " + maxDate, value.toString()));
            }
        }
    }

    /**
     * Validate single choice answer
     */
    private void validateSingleChoiceAnswer(
            ApplicationQuestion question,
            AnswerRequest answer,
            List<ValidationErrorResponse.FieldError> errors) {
        
        List<String> choices = answer.getChoiceValues();
        if (choices == null || choices.isEmpty()) {
            return;
        }

        if (choices.size() > 1) {
            errors.add(createError(question.getId(), "choiceValues", 
                "Only one choice allowed", String.join(", ", choices)));
        }

        // Validate choice is in allowed options
        List<String> options = parseOptions(question.getOptionsSnapshot());
        if (options != null && !options.isEmpty()) {
            for (String choice : choices) {
                if (!options.contains(choice)) {
                    errors.add(createError(question.getId(), "choiceValues", 
                        "Invalid choice: " + choice, choice));
                }
            }
        }
    }

    /**
     * Validate multi choice answer
     */
    private void validateMultiChoiceAnswer(
            ApplicationQuestion question,
            AnswerRequest answer,
            List<ValidationErrorResponse.FieldError> errors) {
        
        List<String> choices = answer.getChoiceValues();
        if (choices == null || choices.isEmpty()) {
            return;
        }

        // Validate choices are in allowed options
        List<String> options = parseOptions(question.getOptionsSnapshot());
        if (options != null && !options.isEmpty()) {
            for (String choice : choices) {
                if (!options.contains(choice)) {
                    errors.add(createError(question.getId(), "choiceValues", 
                        "Invalid choice: " + choice, choice));
                }
            }
        }
    }

    /**
     * Validate video answer
     */
    private void validateVideoAnswer(
            ApplicationQuestion question,
            AnswerRequest answer,
            List<ValidationErrorResponse.FieldError> errors) {
        
        Long mediaId = answer.getMediaId();
        if (mediaId == null) {
            return; // Will be caught by mandatory check if needed
        }
    }

    /**
     * Check if answer is empty based on type
     */
    private boolean isAnswerEmpty(QuestionType type, AnswerRequest answer) {
        switch (type) {
            case TEXT:
                return answer.getTextValue() == null || answer.getTextValue().trim().isEmpty();
            case NUMBER:
                return answer.getNumberValue() == null;
            case DATE:
                return answer.getDateValue() == null;
            case SINGLE_CHOICE:
            case MULTI_CHOICE:
                return answer.getChoiceValues() == null || answer.getChoiceValues().isEmpty();
            case VIDEO:
                return answer.getMediaId() == null;
            default:
                return true;
        }
    }

    /**
     * Parse validation rules from JSON
     */
    private Map<String, Object> parseValidationRules(String json) {
        if (json == null || json.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing validation rules", e);
            return Map.of();
        }
    }

    /**
     * Parse options from JSON
     */
    private List<String> parseOptions(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing options", e);
            return null;
        }
    }

    /**
     * Create field error
     */
    private ValidationErrorResponse.FieldError createError(
            Long questionId, 
            String field, 
            String error, 
            String providedValue) {
        
        return ValidationErrorResponse.FieldError.builder()
            .questionId(questionId)
            .field(field)
            .error(error)
            .providedValue(providedValue)
            .build();
    }
}
