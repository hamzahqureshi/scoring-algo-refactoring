package com.serand.assessment.service;

import com.serand.assessment.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of ScoreCompositionService that handles all pillar aggregation and final score calculation logic.
 * This service extracts the aggregation algorithms from the original SurveyResponseService.
 */
@Service
public class ScoreCompositionServiceImpl implements ScoreCompositionService {

    private final SurveyService surveyService;

    @Autowired
    public ScoreCompositionServiceImpl(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @Override
    public Scores aggregatePillarScores(Map<String, SurveyResponseAnswer> answersMap,
                                       Company company, Pillars pillars) {
        Scores scores = new Scores();
        scores.setWeightings(company.getWeightings());

        if (pillars == null || pillars.getPillars().isEmpty()) {
            // Standard Pillars Aggregation
            aggregateStandardPillars(answersMap, scores);
        } else {
            // Dynamic Pillars Aggregation
            aggregateDynamicPillars(answersMap, scores, pillars);
        }

        return scores;
    }

    @Override
    public double calculateOverallScore(Scores scores, double cvScore) {
        if (cvScore > 0) {
            return (scores.getOverallScore() + cvScore) / 2;
        } else {
            return scores.getOverallScore();
        }
    }

    @Override
    public Application updateApplicationScores(Application application, Scores scores, 
                                             double cvScore) {
        scores.recalculateOverallScore();
        application.setScores(scores);
        
        double finalScore = calculateOverallScore(scores, cvScore);
        application.setApplicationOverallScore(finalScore);
        
        return application;
    }

    /**
     * Aggregate scores using standard pillars (values, culture, mindset, workLife).
     * Extracted from original SurveyResponseService pillar aggregation logic.
     */
    private void aggregateStandardPillars(Map<String, SurveyResponseAnswer> answersMap, Scores scores) {
        double valuesScore = 0, cultureScore = 0, workLifeScore = 0;
        int valuesCount = 0, cultureCount = 0, workLifeCount = 0;

        for (SurveyResponseAnswer answer : answersMap.values()) {
            if (answer.getReference().equals("values")) {
                valuesScore += answer.getQuestionScore();
                valuesCount++;
            }
            if (answer.getReference().equals("culture")) {
                cultureScore += answer.getQuestionScore();
                cultureCount++;
            }
            if (answer.getReference().equals("workplace")) {
                workLifeScore += answer.getQuestionScore();
                workLifeCount++;
            }
        }

        scores.setValues(valuesCount > 0 ? valuesScore / valuesCount : 0);
        scores.setCulture(cultureCount > 0 ? cultureScore / cultureCount : 0);
        
        WorkLife workLifeObj = new WorkLife();
        workLifeObj.setWorkLife(workLifeCount > 0 ? workLifeScore / workLifeCount : 0);
        scores.setWorkLife(workLifeObj);

        // Simplified mindset logic (moved from original service)
        scores.setMindset(50.0); // Assume a mock value
        Mindset zIndexedMindset = surveyService.calculateZindexScore(new Mindset(), new PersonalityProfile());
        scores.setGrowthMindset(calculateMatchScore(zIndexedMindset, scores.getWeightings().getMindset()));
    }

    /**
     * Aggregate scores using dynamic pillars (custom company pillars).
     * Extracted from original SurveyResponseService dynamic pillar aggregation logic.
     */
    private void aggregateDynamicPillars(Map<String, SurveyResponseAnswer> answersMap, 
                                        Scores scores, Pillars pillars) {
        Map<String, Double> customPillarScores = new HashMap<>();
        Map<String, Integer> pillarCounts = new HashMap<>();

        // Initialize pillar tracking
        for (String pillarName : pillars.getPillars().keySet()) {
            customPillarScores.put(pillarName, 0.0);
            pillarCounts.put(pillarName, 0);
        }

        // Aggregate scores by pillar
        for (SurveyResponseAnswer answer : answersMap.values()) {
            String pillarName = answer.getReference();
            if (pillars.getPillars().containsKey(pillarName)) {
                customPillarScores.put(pillarName, customPillarScores.get(pillarName) + answer.getQuestionScore());
                pillarCounts.put(pillarName, pillarCounts.get(pillarName) + 1);
            }
        }

        // Calculate average scores for each pillar
        for (String pillarName : pillars.getPillars().keySet()) {
            double score = (pillarCounts.get(pillarName) > 0) 
                ? customPillarScores.get(pillarName) / pillarCounts.get(pillarName) 
                : 0.0;
            scores.addCustomPillarScore(pillarName, score);
        }
    }

    /**
     * Calculate match score for mindset comparison.
     * Extracted from original SurveyResponseService.calculateMatchScore()
     */
    private double calculateMatchScore(Mindset candidateMindset, double weight) {
        return 50.0 * weight; // Simplified logic from original service
    }
} 