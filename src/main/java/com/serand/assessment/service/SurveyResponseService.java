package com.serand.assessment.service;

import com.serand.assessment.dto.SurveyProcessingResponse;
import com.serand.assessment.model.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * REFACTORED VERSION: SurveyResponseService now orchestrates the scoring flow
 * using CentralScoringEngine and ScoreCompositionService for clean separation of concerns.
 * 
 * Original method was 184 lines, now reduced to under 100 lines.
 */
public class SurveyResponseService {

    // --- Dependencies ---
    private final SurveyService surveyService;
    private final CandidateService candidateService;
    private final ApplicationService applicationService;
    private final GeminiService geminiService;
    private final ApplicationTrackingService applicationTrackingService;
    
    // --- New Refactored Services ---
    private final CentralScoringEngine centralScoringEngine;
    private final ScoreCompositionService scoreCompositionService;

    // Using constructor injection to make the class runnable without a Spring context.
    public SurveyResponseService(SurveyService surveyService, 
                                CandidateService candidateService, 
                                ApplicationService applicationService, 
                                GeminiService geminiService, 
                                ApplicationTrackingService applicationTrackingService,
                                CentralScoringEngine centralScoringEngine,
                                ScoreCompositionService scoreCompositionService) {
        this.surveyService = surveyService;
        this.candidateService = candidateService;
        this.applicationService = applicationService;
        this.geminiService = geminiService;
        this.applicationTrackingService = applicationTrackingService;
        this.centralScoringEngine = centralScoringEngine;
        this.scoreCompositionService = scoreCompositionService;
    }

    /**
     * REFACTORED: Processes a complete survey response using the new scoring services.
     * Method reduced from 184 lines to under 100 lines through clean separation of concerns.
     */
    public CompletableFuture<SurveyProcessingResponse> processSurveyResponse(SurveyResponse surveyResponse, byte[] resumeFile) {
        try {
            // Extract data from survey response
            Survey survey = surveyResponse.getSurvey();
            Candidate candidate = surveyResponse.getCandidate();
            Application application = surveyResponse.getApplication();
            Company company = survey.getCompany();
            Map<String, SurveyResponseAnswer> answersMap = surveyResponse.getAnswerMap();

            // --- Part 1: Ensure Application is Linked (KEPT - Orchestration Logic) ---
            validateAndLinkApplication(application, surveyResponse);
            
            // --- Part 2: Calculate Individual Question Scores (EXTRACTED TO CentralScoringEngine) ---
            centralScoringEngine.calculateAllQuestionScores(surveyResponse, survey, company);
            
            // --- Part 3: Aggregate Pillar Scores and Calculate Final Score (EXTRACTED TO ScoreCompositionService) ---
            Pillars pillars = company.getPillars();
            Scores scores = scoreCompositionService.aggregatePillarScores(answersMap, company, pillars);
            
            // --- Part 4: Calculate Overall Score and Update Application (EXTRACTED TO ScoreCompositionService) ---
            double cvScore = candidate.getCvScoreMap().getOrDefault(survey.getId(), 0.0);
            application = scoreCompositionService.updateApplicationScores(application, scores, cvScore);
            
            // --- Part 5: Data Persistence (KEPT - Orchestration Logic) ---
            persistData(application, candidate);
            
            // --- Part 6: Post-Processing & External Calls (KEPT - Orchestration Logic) ---
            performPostProcessing(candidate, survey, application);

            return CompletableFuture.completedFuture(SurveyProcessingResponse.success(application.getApplicationOverallScore(), scores, "Success"));
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(SurveyProcessingResponse.error("Failed: " + e.getMessage()));
        }
    }

    /**
     * Validate and link application to survey response.
     * Extracted from original method for better organization.
     */
    private void validateAndLinkApplication(Application application, SurveyResponse surveyResponse) {
        if (application == null) {
            throw new IllegalStateException("Application must not be null.");
        }
        application.setComplete(true);
        application.setCandidateResponse(surveyResponse);
        applicationService.saveApplication(application);
    }

    /**
     * Persist application and candidate data.
     * Extracted from original method for better organization.
     */
    private void persistData(Application application, Candidate candidate) {
        applicationService.saveApplication(application);
        candidateService.saveCandidate(candidate);
    }

    /**
     * Perform post-processing tasks including AI feedback and ATS integration.
     * Extracted from original method for better organization.
     */
    private void performPostProcessing(Candidate candidate, Survey survey, Application application) {
        geminiService.generateCandidateFeedback(candidate, survey);
        applicationTrackingService.pushingScoreToAts(application);
    }
} 