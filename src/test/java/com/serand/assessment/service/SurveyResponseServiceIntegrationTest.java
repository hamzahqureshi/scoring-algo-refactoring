package com.serand.assessment.service;

import com.serand.assessment.dto.SurveyProcessingResponse;
import com.serand.assessment.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyResponseServiceIntegrationTest {

    @Mock
    private SurveyService surveyService;
    @Mock
    private CandidateService candidateService;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private GeminiService geminiService;
    @Mock
    private ApplicationTrackingService applicationTrackingService;
    @Mock
    private CentralScoringEngine centralScoringEngine;
    @Mock
    private ScoreCompositionService scoreCompositionService;

    private SurveyResponseService surveyResponseService;

    @BeforeEach
    void setUp() {
        surveyResponseService = new SurveyResponseService(
                surveyService, candidateService, applicationService, 
                geminiService, applicationTrackingService,
                centralScoringEngine, scoreCompositionService
        );
    }

    @Test
    void processSurveyResponse_CompleteFlow_ReturnsSuccessResponse() throws Exception {
        // Arrange
        SurveyResponse surveyResponse = createSampleSurveyResponse();
        Survey survey = surveyResponse.getSurvey();
        Company company = survey.getCompany();
        Map<String, SurveyResponseAnswer> answersMap = surveyResponse.getAnswerMap();

        // Mock the scoring services
        when(centralScoringEngine.calculateAllQuestionScores(surveyResponse, survey, company))
                .thenReturn(new HashMap<>());
        
        Scores mockScores = new Scores();
        mockScores.setValues(80.0);
        mockScores.setCulture(85.0);
        mockScores.setMindset(90.0);
        WorkLife workLife = new WorkLife();
        workLife.setWorkLife(75.0);
        mockScores.setWorkLife(workLife);
        
        when(scoreCompositionService.aggregatePillarScores(answersMap, company, company.getPillars()))
                .thenReturn(mockScores);
        
        when(scoreCompositionService.updateApplicationScores(any(Application.class), eq(mockScores), eq(85.0)))
                .thenAnswer(invocation -> {
                    Application app = invocation.getArgument(0);
                    app.setApplicationOverallScore(82.5);
                    return app;
                });

        // Act
        CompletableFuture<SurveyProcessingResponse> future = 
                surveyResponseService.processSurveyResponse(surveyResponse, new byte[0]);
        SurveyProcessingResponse response = future.get();

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(82.5, response.getOverallScore(), 0.01);
        assertEquals("Success", response.getMessage());
        
        // Verify all services were called
        verify(applicationService, times(2)).saveApplication(any(Application.class));
        verify(candidateService).saveCandidate(any(Candidate.class));
        verify(centralScoringEngine).calculateAllQuestionScores(surveyResponse, survey, company);
        verify(scoreCompositionService).aggregatePillarScores(answersMap, company, company.getPillars());
        verify(scoreCompositionService).updateApplicationScores(any(Application.class), eq(mockScores), eq(85.0));
        verify(geminiService).generateCandidateFeedback(any(Candidate.class), eq(survey));
        verify(applicationTrackingService).pushingScoreToAts(any(Application.class));
    }

    @Test
    void processSurveyResponse_NullApplication_ThrowsException() {
        // Arrange
        SurveyResponse surveyResponse = createSampleSurveyResponse();
        surveyResponse.setApplication(null);

        // Act & Assert
        CompletableFuture<SurveyProcessingResponse> future = 
                surveyResponseService.processSurveyResponse(surveyResponse, new byte[0]);
        
        SurveyProcessingResponse response = future.join();
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Application must not be null"));
    }

    @Test
    void processSurveyResponse_ScoringServiceException_ReturnsErrorResponse() throws Exception {
        // Arrange
        SurveyResponse surveyResponse = createSampleSurveyResponse();
        Survey survey = surveyResponse.getSurvey();
        Company company = survey.getCompany();

        when(centralScoringEngine.calculateAllQuestionScores(surveyResponse, survey, company))
                .thenThrow(new RuntimeException("Scoring failed"));

        // Act
        CompletableFuture<SurveyProcessingResponse> future = 
                surveyResponseService.processSurveyResponse(surveyResponse, new byte[0]);
        SurveyProcessingResponse response = future.get();

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Failed: Scoring failed"));
    }

    @Test
    void processSurveyResponse_WithDynamicPillars_ProcessesCorrectly() throws Exception {
        // Arrange
        SurveyResponse surveyResponse = createSampleSurveyResponse();
        Survey survey = surveyResponse.getSurvey();
        Company company = survey.getCompany();
        Map<String, SurveyResponseAnswer> answersMap = surveyResponse.getAnswerMap();

        // Set up dynamic pillars
        Pillars dynamicPillars = new Pillars();
        Map<String, Double> pillarWeights = new HashMap<>();
        pillarWeights.put("technology", 0.6);
        pillarWeights.put("leadership", 0.4);
        dynamicPillars.setPillars(pillarWeights);
        company.setPillars(dynamicPillars);

        // Mock services
        when(centralScoringEngine.calculateAllQuestionScores(surveyResponse, survey, company))
                .thenReturn(new HashMap<>());
        
        Scores mockScores = new Scores();
        mockScores.addCustomPillarScore("technology", 85.0);
        mockScores.addCustomPillarScore("leadership", 80.0);
        
        when(scoreCompositionService.aggregatePillarScores(answersMap, company, dynamicPillars))
                .thenReturn(mockScores);
        
        when(scoreCompositionService.updateApplicationScores(any(Application.class), eq(mockScores), eq(85.0)))
                .thenAnswer(invocation -> {
                    Application app = invocation.getArgument(0);
                    app.setApplicationOverallScore(82.5);
                    return app;
                });

        // Act
        CompletableFuture<SurveyProcessingResponse> future = 
                surveyResponseService.processSurveyResponse(surveyResponse, new byte[0]);
        SurveyProcessingResponse response = future.get();

        // Assert
        assertTrue(response.isSuccess());
        verify(scoreCompositionService).aggregatePillarScores(answersMap, company, dynamicPillars);
    }

    @Test
    void processSurveyResponse_ZeroCvScore_ProcessesCorrectly() throws Exception {
        // Arrange
        SurveyResponse surveyResponse = createSampleSurveyResponse();
        Candidate candidate = surveyResponse.getCandidate();
        candidate.getCvScoreMap().clear(); // No CV score
        
        Survey survey = surveyResponse.getSurvey();
        Company company = survey.getCompany();
        Map<String, SurveyResponseAnswer> answersMap = surveyResponse.getAnswerMap();

        // Mock services
        when(centralScoringEngine.calculateAllQuestionScores(surveyResponse, survey, company))
                .thenReturn(new HashMap<>());
        
        Scores mockScores = new Scores();
        mockScores.setValues(80.0);
        mockScores.setCulture(85.0);
        mockScores.setMindset(90.0);
        WorkLife workLife = new WorkLife();
        workLife.setWorkLife(75.0);
        mockScores.setWorkLife(workLife);
        
        when(scoreCompositionService.aggregatePillarScores(answersMap, company, company.getPillars()))
                .thenReturn(mockScores);
        
        when(scoreCompositionService.updateApplicationScores(any(Application.class), eq(mockScores), eq(0.0)))
                .thenAnswer(invocation -> {
                    Application app = invocation.getArgument(0);
                    app.setApplicationOverallScore(82.5);
                    return app;
                });

        // Act
        CompletableFuture<SurveyProcessingResponse> future = 
                surveyResponseService.processSurveyResponse(surveyResponse, new byte[0]);
        SurveyProcessingResponse response = future.get();

        // Assert
        assertTrue(response.isSuccess());
        verify(scoreCompositionService).updateApplicationScores(any(Application.class), eq(mockScores), eq(0.0));
    }

    private SurveyResponse createSampleSurveyResponse() {
        // Create Survey
        Survey survey = new Survey();
        survey.setId("survey1");
        survey.setName("Test Survey");
        
        // Create Company
        Company company = new Company();
        company.setId("company1");
        company.setWeightings(createWeightings());
        survey.setCompany(company);
        
        // Create Questions
        Question question1 = new Question();
        question1.setId("q1");
        question1.setReference("values");
        question1.setQuestion("What are your core values?");
        question1.setCorrectAnswers(new String[]{"always", "often"});
        
        Question question2 = new Question();
        question2.setId("q2");
        question2.setReference("culture");
        question2.setQuestion("How do you prefer to work?");
        question2.setCorrectAnswers(new String[]{"team", "individual"});
        
        survey.setQuestions(java.util.Arrays.asList(question1, question2));
        
        // Create Candidate
        Candidate candidate = new Candidate();
        candidate.setId("candidate1");
        candidate.setEmail("test@example.com");
        candidate.setFirstName("John");
        candidate.setLastName("Doe");
        candidate.getCvScoreMap().put("survey1", 85.0);
        
        // Create Application
        Application application = new Application();
        application.setId("app1");
        application.setCandidate(candidate);
        application.setSurvey(survey);
        application.setCompany(company);
        
        // Create Survey Response Answers
        Map<String, SurveyResponseAnswer> answersMap = new HashMap<>();
        
        SurveyResponseAnswer answer1 = new SurveyResponseAnswer();
        answer1.setQuestionId("q1");
        answer1.setReference("values");
        answer1.setType("multipleChoice");
        answer1.setArrayAnswer(new String[]{"always"});
        answersMap.put("q1", answer1);
        
        SurveyResponseAnswer answer2 = new SurveyResponseAnswer();
        answer2.setQuestionId("q2");
        answer2.setReference("culture");
        answer2.setType("multipleChoice");
        answer2.setArrayAnswer(new String[]{"team"});
        answersMap.put("q2", answer2);
        
        // Create Survey Response
        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setId("response1");
        surveyResponse.setSurvey(survey);
        surveyResponse.setCandidate(candidate);
        surveyResponse.setApplication(application);
        surveyResponse.setAnswerMap(answersMap);
        
        return surveyResponse;
    }

    private Weightings createWeightings() {
        Weightings weightings = new Weightings();
        weightings.setValues(0.25);
        weightings.setCulture(0.25);
        weightings.setMindset(0.25);
        weightings.setWorkLife(0.25);
        return weightings;
    }
} 