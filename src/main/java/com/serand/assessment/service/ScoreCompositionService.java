package com.serand.assessment.service;

import com.serand.assessment.model.*;

import java.util.Map;

/**
 * ScoreCompositionService is responsible for aggregating scores and calculating final results.
 * This service extracts all pillar aggregation and final score calculation logic from the original SurveyResponseService.
 */
public interface ScoreCompositionService {
    
    /**
     * Aggregate individual question scores into pillar scores.
     * 
     * @param answersMap Map of question answers with their scores
     * @param company The company context for pillar configuration
     * @param pillars The pillar configuration (can be null for standard pillars)
     * @return Scores object with aggregated pillar scores
     */
    Scores aggregatePillarScores(Map<String, SurveyResponseAnswer> answersMap,
                                Company company, Pillars pillars);
    
    /**
     * Calculate the overall score combining survey scores with CV score.
     * 
     * @param scores The aggregated survey scores
     * @param cvScore The candidate's CV score
     * @return The final overall score
     */
    double calculateOverallScore(Scores scores, double cvScore);
    
    /**
     * Update the application with final scores.
     * 
     * @param application The application to update
     * @param scores The calculated scores
     * @param cvScore The candidate's CV score
     * @return The updated application
     */
    Application updateApplicationScores(Application application, Scores scores, 
                                      double cvScore);
} 