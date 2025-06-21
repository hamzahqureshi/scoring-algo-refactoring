package com.serand.assessment.service;

import com.serand.assessment.model.Candidate;
import com.serand.assessment.model.Survey;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {
    
    public String getSurveyAnswerRelevanceScore(String answer, String question, String surveyName) {
        // Mock implementation - returns score and explanation
        double score = Math.random(); // 0-1
        return score + "-AI generated explanation for the score";
    }
    
    public void generateCandidateFeedback(Candidate candidate, Survey survey) {
        // Mock implementation
        System.out.println("Generating AI feedback for candidate: " + candidate.getId());
    }
} 