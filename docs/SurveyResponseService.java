// File: com/serand/assignment/SurveyResponseService.java
package com.serand.assignment;

import com.serand.assignment.dto.*;
import com.serand.assignment.model.*;
import com.serand.assignment.service.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * THIS IS A SIMPLIFIED, INTENTIONALLY "MESSY" VERSION FOR THE REFACTORING ASSIGNMENT.
 * The candidate's task is to refactor this into a CentralScoringEngine and ScoreCompositionService.
 */
public class SurveyResponseService {

    // --- Dependencies ---
    private final SurveyService surveyService;
    private final CandidateService candidateService;
    private final ApplicationService applicationService;
    private final GeminiService geminiService;
    private final ApplicationTrackingService applicationTrackingService;

    // --- Internal state (should be refactored) ---
    private Map<String, String> scoreExplain = new HashMap<>();

    // Using constructor injection to make the class runnable without a Spring context.
    public SurveyResponseService(SurveyService surveyService, CandidateService candidateService, ApplicationService applicationService, GeminiService geminiService, ApplicationTrackingService applicationTrackingService) {
        this.surveyService = surveyService;
        this.candidateService = candidateService;
        this.applicationService = applicationService;
        this.geminiService = geminiService;
        this.applicationTrackingService = applicationTrackingService;
    }

    /**
     * Processes a complete survey response, including question scoring,
     * pillar aggregation, and final score saving.
     */
    public CompletableFuture<SurveyProcessingResponse> processSurveyResponse(SurveyResponse surveyResponse, byte[] resumeFile) {
        try {
            Survey survey = surveyResponse.getSurvey();
            Candidate candidate = surveyResponse.getCandidate();
            Map<String, SurveyResponseAnswer> answersMap = surveyResponse.getAnswerMap();
            Application application = surveyResponse.getApplication();
            List<Question> questionList = survey.getQuestions();
            Company company = survey.getCompany();

            // --- Part 1: Ensure Application is Linked ---
            if(application == null){
                // In a real app, we'd find or create one. Here we assume it exists on surveyResponse.
                throw new IllegalStateException("Application must not be null.");
            }
            application.setComplete(true);
            application.setCandidateResponse(surveyResponse);
            applicationService.saveApplication(application); // Mock save
            
            // --- Part 2: Calculate Individual Question Scores (MOVE THIS LOGIC) ---
            for (Map.Entry<String, SurveyResponseAnswer> entry : answersMap.entrySet()) {
                SurveyResponseAnswer answer = entry.getValue();
                String questionId = answer.getQuestionId();

                if (questionId == null || questionId.isEmpty() || entry.getKey().equals("personalInfo")) continue; // Skip non-question answers

                Question question = questionList.stream().filter(q -> q.getId().equals(questionId)).findFirst().orElse(null);
                double currentQuestionScore = 0;

                if(question != null) {
                    // --- SCORING LOGIC TO BE EXTRACTED ---
                    if(answer.getType().equals("multipleChoice") || answer.getType().equals("coding")){
                        if(isTypeOneQuestion(question.getCorrectAnswers())){
                            currentQuestionScore = getFixedMcqQuestionScore(answer.getArrayAnswer(), question.getCorrectAnswers());
                        } else {
                            currentQuestionScore = getVariableMcqQuestionScore(answer.getArrayAnswer(), question.getCorrectAnswers(), question.getReference(), company);
                        }
                    } else { // Text-based answers
                        String scoreExplanationAndScore = geminiService.getSurveyAnswerRelevanceScore(answer.getStringAnswer(), question.getQuestion(), survey.getName());
                        currentQuestionScore = Double.parseDouble(scoreExplanationAndScore.split("-")[0]);
                        if(question.isGemini()){
                            scoreExplain.put(question.getId(), scoreExplanationAndScore.split("-")[1]);
                        }
                        currentQuestionScore *= 100; // Convert 0-1 to 0-100
                    }
                    // --- END OF SCORING LOGIC TO EXTRACT ---
                }
                
                if(scoreExplain.containsKey(question.getId())){
                    answer.setScoreExplanation(scoreExplain.get(question.getId()));
                }
                answer.setQuestionScore(currentQuestionScore);
            }
            // surveyResponseRepository.save(surveyResponse); // Mock save

            // --- Part 3: Pillar Aggregation and Overall Score Calculation (MOVE THIS LOGIC) ---
            Pillars pillars = company.getPillars();
            Scores scores = application.getScores();
            if(scores == null) scores = new Scores();
            scores.setWeightings(company.getWeightings());

            if(pillars == null || pillars.getPillars().isEmpty()){
                // Standard Pillars Aggregation
                double valuesScore = 0, cultureScore = 0, workLifeScore = 0;
                int valuesCount = 0, cultureCount = 0, workLifeCount = 0;
                for (SurveyResponseAnswer answer : answersMap.values()) {
                    if (answer.getReference().equals("values")) { valuesScore += answer.getQuestionScore(); valuesCount++; }
                    if (answer.getReference().equals("culture")) { cultureScore += answer.getQuestionScore(); cultureCount++; }
                    if (answer.getReference().equals("workplace")) { workLifeScore += answer.getQuestionScore(); workLifeCount++; }
                }
                scores.setValues(valuesCount > 0 ? valuesScore / valuesCount : 0);
                scores.setCulture(cultureCount > 0 ? cultureScore / cultureCount : 0);
                WorkLife workLifeObj = new WorkLife();
                workLifeObj.setWorkLife(workLifeCount > 0 ? workLifeScore / workLifeCount : 0);
                scores.setWorkLife(workLifeObj);
                // (Simplified mindset logic for brevity)
                scores.setMindset(50.0); // Assume a mock value
                Mindset zIndexedMindset = surveyService.calculateZindexScore(new Mindset(), new PersonalityProfile());
                scores.setGrowthMindset(calculateMatchScore(zIndexedMindset, scores.getWeightings().getMindset()));

            } else {
                // Dynamic Pillars Aggregation
                Map<String, Double> customPillarScores = new HashMap<>();
                Map<String, Integer> pillarCounts = new HashMap<>();
                for (String pillarName : pillars.getPillars().keySet()) { customPillarScores.put(pillarName, 0.0); pillarCounts.put(pillarName, 0); }
                for (SurveyResponseAnswer answer : answersMap.values()) {
                    String pillarName = answer.getReference();
                    if(pillars.getPillars().containsKey(pillarName)) {
                        customPillarScores.put(pillarName, customPillarScores.get(pillarName) + answer.getQuestionScore());
                        pillarCounts.put(pillarName, pillarCounts.get(pillarName) + 1);
                    }
                }
                for (String pillarName : pillars.getPillars().keySet()) {
                    double score = (pillarCounts.get(pillarName) > 0) ? customPillarScores.get(pillarName) / pillarCounts.get(pillarName) : 0.0;
                    scores.addCustomPillarScore(pillarName, score);
                }
            }
            
            // --- Part 4: Final Overall Score Composition (MOVE THIS LOGIC) ---
            scores.recalculateOverallScore(); // Assumes Scores DTO has this logic
            application.setScores(scores);
            double cvScore = candidate.getCvScoreMap().getOrDefault(survey.getId(), 0.0);
            double finalScore = cvScore > 0 ? (scores.getOverallScore() + cvScore) / 2 : scores.getOverallScore();
            application.setApplicationOverallScore(finalScore);

            applicationService.saveApplication(application); // Mock save
            candidateService.saveCandidate(candidate);       // Mock save
            
            // --- Part 5: Post-Processing & External Calls ---
            geminiService.generateCandidateFeedback(candidate, survey);
            applicationTrackingService.pushingScoreToAts(application);

            return CompletableFuture.completedFuture(SurveyProcessingResponse.success(finalScore, scores, "Success"));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(SurveyProcessingResponse.error("Failed: " + e.getMessage()));
        }
    }


    // --- Private Helper Methods (THESE SHOULD BE MOVED TO A SCORING ENGINE) ---
    private double calculateMatchScore(Mindset candidateMindset, double weight) {
        return 50.0 * weight; // Simplified logic
    }
    
    private double getVariableMcqQuestionScore(String[] candidateAnswer, String[] correctAnswers , String questionReference, Company company){
        long matches = Arrays.stream(candidateAnswer)
                .filter(ca -> Arrays.stream(correctAnswers).anyMatch(cca -> ca.equalsIgnoreCase(cca)))
                .count();
        return correctAnswers.length == 0 ? 0 : (double)matches / correctAnswers.length * 100;
    }
    
    private double getFixedMcqQuestionScore(String[] candidateAnswer, String[] correctAnswers){
        double scorePoints = 0;
        for (String ans : candidateAnswer) {
            if ("always".equalsIgnoreCase(ans)) scorePoints += 1.0;
            if ("often".equalsIgnoreCase(ans)) scorePoints += 0.75;
            if ("sometimes".equalsIgnoreCase(ans)) scorePoints += 0.5;
        }
        return (scorePoints / correctAnswers.length) * 100;
    }
    
    private boolean isTypeOneQuestion(String[] correctAnswers) {
        return Arrays.stream(correctAnswers).anyMatch(ans -> "always".equalsIgnoreCase(ans) || "often".equalsIgnoreCase(ans));
    }
}