package com.serand.assessment.service;

import com.serand.assessment.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreCompositionServiceTest {

    @Mock
    private SurveyService surveyService;

    private ScoreCompositionServiceImpl scoreCompositionService;

    @BeforeEach
    void setUp() {
        scoreCompositionService = new ScoreCompositionServiceImpl(surveyService);
    }

    @Test
    void aggregatePillarScores_StandardPillars_ReturnsCorrectAggregation() {
        // Arrange
        Map<String, SurveyResponseAnswer> answersMap = new HashMap<>();
        
        // Values answers
        answersMap.put("v1", createAnswer("values", 80.0));
        answersMap.put("v2", createAnswer("values", 90.0));
        
        // Culture answers
        answersMap.put("c1", createAnswer("culture", 75.0));
        answersMap.put("c2", createAnswer("culture", 85.0));
        answersMap.put("c3", createAnswer("culture", 95.0));
        
        // Workplace answers
        answersMap.put("w1", createAnswer("workplace", 70.0));
        
        Company company = new Company();
        company.setWeightings(createWeightings());

        when(surveyService.calculateZindexScore(any(Mindset.class), any(PersonalityProfile.class)))
                .thenReturn(new Mindset());

        // Act
        Scores scores = scoreCompositionService.aggregatePillarScores(answersMap, company, null);

        // Assert
        assertEquals(85.0, scores.getValues(), 0.01); // (80 + 90) / 2 = 85
        assertEquals(85.0, scores.getCulture(), 0.01); // (75 + 85 + 95) / 3 = 85
        assertEquals(70.0, scores.getWorkLife().getWorkLife(), 0.01); // 70 / 1 = 70
        assertEquals(50.0, scores.getMindset(), 0.01); // Mock value
        assertEquals(12.5, scores.getGrowthMindset(), 0.01); // 50.0 * 0.25 = 12.5
    }

    @Test
    void aggregatePillarScores_DynamicPillars_ReturnsCorrectAggregation() {
        // Arrange
        Map<String, SurveyResponseAnswer> answersMap = new HashMap<>();
        answersMap.put("tech1", createAnswer("technology", 80.0));
        answersMap.put("tech2", createAnswer("technology", 90.0));
        answersMap.put("lead1", createAnswer("leadership", 75.0));
        answersMap.put("lead2", createAnswer("leadership", 85.0));

        Company company = new Company();
        company.setWeightings(createWeightings());

        Pillars pillars = new Pillars();
        Map<String, Double> pillarWeights = new HashMap<>();
        pillarWeights.put("technology", 0.6);
        pillarWeights.put("leadership", 0.4);
        pillars.setPillars(pillarWeights);

        // Act
        Scores scores = scoreCompositionService.aggregatePillarScores(answersMap, company, pillars);

        // Assert
        Map<String, Double> customScores = scores.getCustomPillarScores();
        assertEquals(85.0, customScores.get("technology"), 0.01); // (80 + 90) / 2 = 85
        assertEquals(80.0, customScores.get("leadership"), 0.01); // (75 + 85) / 2 = 80
    }

    @Test
    void aggregatePillarScores_EmptyAnswers_ReturnsZeroScores() {
        // Arrange
        Map<String, SurveyResponseAnswer> answersMap = new HashMap<>();
        Company company = new Company();
        company.setWeightings(createWeightings());

        when(surveyService.calculateZindexScore(any(Mindset.class), any(PersonalityProfile.class)))
                .thenReturn(new Mindset());

        // Act
        Scores scores = scoreCompositionService.aggregatePillarScores(answersMap, company, null);

        // Assert
        assertEquals(0.0, scores.getValues(), 0.01);
        assertEquals(0.0, scores.getCulture(), 0.01);
        assertEquals(0.0, scores.getWorkLife().getWorkLife(), 0.01);
        assertEquals(50.0, scores.getMindset(), 0.01); // Mock value
    }

    @Test
    void aggregatePillarScores_DynamicPillarsWithNoMatches_ReturnsZeroScores() {
        // Arrange
        Map<String, SurveyResponseAnswer> answersMap = new HashMap<>();
        answersMap.put("other1", createAnswer("other", 80.0)); // Not in pillars

        Company company = new Company();
        company.setWeightings(createWeightings());

        Pillars pillars = new Pillars();
        Map<String, Double> pillarWeights = new HashMap<>();
        pillarWeights.put("technology", 0.6);
        pillarWeights.put("leadership", 0.4);
        pillars.setPillars(pillarWeights);

        // Act
        Scores scores = scoreCompositionService.aggregatePillarScores(answersMap, company, pillars);

        // Assert
        Map<String, Double> customScores = scores.getCustomPillarScores();
        assertEquals(0.0, customScores.get("technology"), 0.01);
        assertEquals(0.0, customScores.get("leadership"), 0.01);
    }

    @Test
    void calculateOverallScore_WithCvScore_ReturnsAverage() {
        // Arrange
        Scores scores = new Scores();
        scores.setValues(80.0);
        scores.setCulture(85.0);
        scores.setMindset(90.0);
        WorkLife workLife = new WorkLife();
        workLife.setWorkLife(75.0);
        scores.setWorkLife(workLife);
        scores.setWeightings(createWeightings());

        double cvScore = 85.0;

        // Act
        double overallScore = scoreCompositionService.calculateOverallScore(scores, cvScore);

        // Assert
        // Expected: (surveyScore + cvScore) / 2
        // Survey score calculation: (80*0.25 + 85*0.25 + 90*0.25 + 75*0.25) = 82.5
        // Overall: (82.5 + 85) / 2 = 83.75
        assertEquals(83.75, overallScore, 0.01);
    }

    @Test
    void calculateOverallScore_WithoutCvScore_ReturnsSurveyScore() {
        // Arrange
        Scores scores = new Scores();
        scores.setValues(80.0);
        scores.setCulture(85.0);
        scores.setMindset(90.0);
        WorkLife workLife = new WorkLife();
        workLife.setWorkLife(75.0);
        scores.setWorkLife(workLife);
        scores.setWeightings(createWeightings());

        double cvScore = 0.0;

        // Act
        double overallScore = scoreCompositionService.calculateOverallScore(scores, cvScore);

        // Assert
        // Expected: surveyScore only
        // Survey score calculation: (80*0.25 + 85*0.25 + 90*0.25 + 75*0.25) = 82.5
        assertEquals(82.5, overallScore, 0.01);
    }

    @Test
    void updateApplicationScores_ValidInput_UpdatesApplicationCorrectly() {
        // Arrange
        Application application = new Application();
        Scores scores = new Scores();
        scores.setValues(80.0);
        scores.setCulture(85.0);
        scores.setMindset(90.0);
        WorkLife workLife = new WorkLife();
        workLife.setWorkLife(75.0);
        scores.setWorkLife(workLife);
        scores.setWeightings(createWeightings());

        double cvScore = 85.0;

        // Act
        Application updatedApplication = scoreCompositionService.updateApplicationScores(application, scores, cvScore);

        // Assert
        assertSame(application, updatedApplication);
        assertSame(scores, application.getScores());
        assertEquals(83.75, application.getApplicationOverallScore(), 0.01);
    }

    @Test
    void updateApplicationScores_NullScores_HandlesGracefully() {
        // Arrange
        Application application = new Application();
        Scores scores = null;
        double cvScore = 85.0;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            scoreCompositionService.updateApplicationScores(application, scores, cvScore);
        });
    }

    @Test
    void aggregatePillarScores_StandardPillarsWithMixedReferences_IgnoresUnmatchedReferences() {
        // Arrange
        Map<String, SurveyResponseAnswer> answersMap = new HashMap<>();
        answersMap.put("v1", createAnswer("values", 80.0));
        answersMap.put("c1", createAnswer("culture", 85.0));
        answersMap.put("w1", createAnswer("workplace", 75.0));
        answersMap.put("other1", createAnswer("other", 90.0)); // Should be ignored

        Company company = new Company();
        company.setWeightings(createWeightings());

        when(surveyService.calculateZindexScore(any(Mindset.class), any(PersonalityProfile.class)))
                .thenReturn(new Mindset());

        // Act
        Scores scores = scoreCompositionService.aggregatePillarScores(answersMap, company, null);

        // Assert
        assertEquals(80.0, scores.getValues(), 0.01); // Only values answer
        assertEquals(85.0, scores.getCulture(), 0.01); // Only culture answer
        assertEquals(75.0, scores.getWorkLife().getWorkLife(), 0.01); // Only workplace answer
    }

    @Test
    void calculateOverallScore_ZeroCvScore_ReturnsSurveyScoreOnly() {
        // Arrange
        Scores scores = new Scores();
        scores.setValues(100.0);
        scores.setCulture(100.0);
        scores.setMindset(100.0);
        WorkLife workLife = new WorkLife();
        workLife.setWorkLife(100.0);
        scores.setWorkLife(workLife);
        scores.setWeightings(createWeightings());

        double cvScore = 0.0;

        // Act
        double overallScore = scoreCompositionService.calculateOverallScore(scores, cvScore);

        // Assert
        assertEquals(100.0, overallScore, 0.01); // Survey score only
    }

    @Test
    void calculateOverallScore_NegativeCvScore_ReturnsSurveyScoreOnly() {
        // Arrange
        Scores scores = new Scores();
        scores.setValues(80.0);
        scores.setCulture(85.0);
        scores.setMindset(90.0);
        WorkLife workLife = new WorkLife();
        workLife.setWorkLife(75.0);
        scores.setWorkLife(workLife);
        scores.setWeightings(createWeightings());

        double cvScore = -10.0;

        // Act
        double overallScore = scoreCompositionService.calculateOverallScore(scores, cvScore);

        // Assert
        assertEquals(82.5, overallScore, 0.01); // Survey score only (negative CV ignored)
    }

    private SurveyResponseAnswer createAnswer(String reference, double score) {
        SurveyResponseAnswer answer = new SurveyResponseAnswer();
        answer.setReference(reference);
        answer.setQuestionScore(score);
        return answer;
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