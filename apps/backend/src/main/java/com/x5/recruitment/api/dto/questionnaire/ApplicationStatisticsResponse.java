package com.x5.recruitment.api.dto.questionnaire;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for application statistics (HR view).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatisticsResponse {

    private Long applicationId;
    private Long candidateId;
    private String candidateName;
    private String candidateEmail;
    
    // Answer statistics
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private Integer mandatoryQuestions;
    private Integer mandatoryAnswered;
    private Integer optionalQuestions;
    private Integer optionalAnswered;
    private Double completionPercentage;
    
    // Video statistics
    private Boolean hasVideoAnswers;
    private Integer videoQuestionsTotal;
    private Integer videoQuestionsAnswered;
    
    // Answers with details
    private List<AnswerResponse> answers;
}
