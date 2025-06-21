package com.serand.assessment.service;

import com.serand.assessment.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

/**
 * Implementation of CentralScoringEngine that handles all individual question scoring logic.
 * This service extracts the scoring algorithms from the original SurveyResponseService.
 */
@Service
public class CentralScoringEngineImpl implements CentralScoringEngine {

    private final GeminiService geminiService;
    
    // Internal state for score explanations (moved from original service)
    private Map<String, String> scoreExplain = new HashMap<>();

    @Autowired
    public CentralScoringEngineImpl(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @Override
    public double calculateQuestionScore(SurveyResponseAnswer answer, Question question, 
                                       Company company, String surveyName) {
        if (answer == null || question == null) {
            return 0.0;
        }

        double currentQuestionScore = 0.0;

        // Handle different question types
        if (answer.getType().equals("multipleChoice") || answer.getType().equals("coding")) {
            if (isTypeOneQuestion(question.getCorrectAnswers())) {
                currentQuestionScore = getFixedMcqQuestionScore(answer.getArrayAnswer(), question.getCorrectAnswers());
            } else {
                currentQuestionScore = getVariableMcqQuestionScore(answer.getArrayAnswer(), question.getCorrectAnswers(), question.getReference(), company);
            }
        } else { // Text-based answers
            String scoreExplanationAndScore = geminiService.getSurveyAnswerRelevanceScore(answer.getStringAnswer(), question.getQuestion(), surveyName);
            currentQuestionScore = Double.parseDouble(scoreExplanationAndScore.split("-")[0]);
            if (question.isGemini()) {
                scoreExplain.put(question.getId(), scoreExplanationAndScore.split("-")[1]);
            }
            currentQuestionScore *= 100; // Convert 0-1 to 0-100
        }

        // Set score explanation if available
        if (scoreExplain.containsKey(question.getId())) {
            answer.setScoreExplanation(scoreExplain.get(question.getId()));
        }
        
        answer.setQuestionScore(currentQuestionScore);
        return currentQuestionScore;
    }

    @Override
    public Map<String, Double> calculateAllQuestionScores(SurveyResponse response, 
                                                         Survey survey, Company company) {
        Map<String, Double> questionScores = new HashMap<>();
        Map<String, SurveyResponseAnswer> answersMap = response.getAnswerMap();
        String surveyName = survey.getName();

        for (Map.Entry<String, SurveyResponseAnswer> entry : answersMap.entrySet()) {
            SurveyResponseAnswer answer = entry.getValue();
            String questionId = answer.getQuestionId();

            // Skip non-question answers
            if (questionId == null || questionId.isEmpty() || entry.getKey().equals("personalInfo")) {
                continue;
            }

            // Find the corresponding question
            Question question = survey.getQuestions().stream()
                    .filter(q -> q.getId().equals(questionId))
                    .findFirst()
                    .orElse(null);

            if (question != null) {
                double score = calculateQuestionScore(answer, question, company, surveyName);
                questionScores.put(questionId, score);
            }
        }

        return questionScores;
    }

    /**
     * Calculate score for fixed MCQ questions (Always/Often/Sometimes pattern).
     * Extracted from original SurveyResponseService.getFixedMcqQuestionScore()
     */
    private double getFixedMcqQuestionScore(String[] candidateAnswer, String[] correctAnswers) {
        if (candidateAnswer == null || correctAnswers == null || correctAnswers.length == 0) {
            return 0.0;
        }

        double scorePoints = 0;
        for (String ans : candidateAnswer) {
            if ("always".equalsIgnoreCase(ans)) {
                scorePoints += 1.0;
            } else if ("often".equalsIgnoreCase(ans)) {
                scorePoints += 0.75;
            } else if ("sometimes".equalsIgnoreCase(ans)) {
                scorePoints += 0.5;
            }
        }
        return (scorePoints / correctAnswers.length) * 100;
    }

    /**
     * Calculate score for variable MCQ questions (company-specific answers).
     * Extracted from original SurveyResponseService.getVariableMcqQuestionScore()
     */
    private double getVariableMcqQuestionScore(String[] candidateAnswer, String[] correctAnswers, 
                                             String questionReference, Company company) {
        if (candidateAnswer == null || correctAnswers == null || correctAnswers.length == 0) {
            return 0.0;
        }

        long matches = Arrays.stream(candidateAnswer)
                .filter(ca -> Arrays.stream(correctAnswers).anyMatch(cca -> ca.equalsIgnoreCase(cca)))
                .count();
        return (double) matches / correctAnswers.length * 100;
    }

    /**
     * Determine if a question is a Type One question (Always/Often pattern).
     * Extracted from original SurveyResponseService.isTypeOneQuestion()
     */
    private boolean isTypeOneQuestion(String[] correctAnswers) {
        if (correctAnswers == null) {
            return false;
        }
        return Arrays.stream(correctAnswers).anyMatch(ans -> 
            "always".equalsIgnoreCase(ans) || "often".equalsIgnoreCase(ans));
    }

    /**
     * Clear the score explanations map (useful for testing).
     */
    public void clearScoreExplanations() {
        scoreExplain.clear();
    }

    /**
     * Get the score explanations map (useful for testing).
     */
    public Map<String, String> getScoreExplanations() {
        return new HashMap<>(scoreExplain);
    }
} 