package com.serand.assessment.service;

import com.serand.assessment.model.*;

import java.util.Map;

/**
 * CentralScoringEngine is responsible for calculating individual question scores.
 * This service extracts all question scoring logic from the original SurveyResponseService.
 */
public interface CentralScoringEngine {
    
    /**
     * Calculate the score for a single question.
     * 
     * @param answer The candidate's answer to the question
     * @param question The question being scored
     * @param company The company context for company-specific scoring
     * @param surveyName The name of the survey for AI scoring context
     * @return The calculated score (0-100)
     */
    double calculateQuestionScore(SurveyResponseAnswer answer, Question question, 
                                Company company, String surveyName);
    
    /**
     * Calculate scores for all questions in a survey response.
     * 
     * @param response The complete survey response
     * @param survey The survey containing the questions
     * @param company The company context
     * @return Map of question ID to calculated score
     */
    Map<String, Double> calculateAllQuestionScores(SurveyResponse response, 
                                                  Survey survey, Company company);
} 