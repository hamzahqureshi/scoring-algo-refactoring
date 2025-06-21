package com.serand.assessment.service;

import com.serand.assessment.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CentralScoringEngineTest {

    @Mock
    private GeminiService geminiService;

    private CentralScoringEngineImpl centralScoringEngine;

    @BeforeEach
    void setUp() {
        centralScoringEngine = new CentralScoringEngineImpl(geminiService);
    }

    @Test
    void calculateQuestionScore_FixedMcqQuestion_ReturnsCorrectScore() {
        // Arrange
        SurveyResponseAnswer answer = new SurveyResponseAnswer();
        answer.setType("multipleChoice");
        answer.setArrayAnswer(new String[]{"always", "often"});

        Question question = new Question();
        question.setId("q1");
        question.setCorrectAnswers(new String[]{"always", "often", "sometimes"});

        Company company = new Company();

        // Act
        double score = centralScoringEngine.calculateQuestionScore(answer, question, company, "Test Survey");

        // Assert
        assertEquals(58.33, score, 0.01);
    }

    @Test
    void calculateQuestionScore_VariableMcqQuestion_ReturnsCorrectScore() {
        // Arrange
        SurveyResponseAnswer answer = new SurveyResponseAnswer();
        answer.setType("multipleChoice");
        answer.setArrayAnswer(new String[]{"Java", "Python"});

        Question question = new Question();
        question.setId("q2");
        question.setCorrectAnswers(new String[]{"Java", "Python", "C++"});

        Company company = new Company();

        // Act
        double score = centralScoringEngine.calculateQuestionScore(answer, question, company, "Test Survey");

        // Assert
        assertEquals(66.67, score, 0.01); // 2 matches / 3 total * 100 = 66.67
    }

    @Test
    void calculateQuestionScore_TextQuestion_ReturnsGeminiScore() {
        // Arrange
        SurveyResponseAnswer answer = new SurveyResponseAnswer();
        answer.setType("text");
        answer.setStringAnswer("I am passionate about coding and learning new technologies.");

        Question question = new Question();
        question.setId("q3");
        question.setQuestion("What motivates you in your career?");
        question.setGemini(true);

        Company company = new Company();

        when(geminiService.getSurveyAnswerRelevanceScore(anyString(), anyString(), anyString()))
                .thenReturn("0.85-This answer shows strong motivation and passion for technology.");

        // Act
        double score = centralScoringEngine.calculateQuestionScore(answer, question, company, "Test Survey");

        // Assert
        assertEquals(85.0, score, 0.01); // 0.85 * 100 = 85.0
        verify(geminiService).getSurveyAnswerRelevanceScore(answer.getStringAnswer(), question.getQuestion(), "Test Survey");
        assertEquals("This answer shows strong motivation and passion for technology.", answer.getScoreExplanation());
    }

    @Test
    void calculateQuestionScore_NullInputs_ReturnsZero() {
        // Act & Assert
        assertEquals(0.0, centralScoringEngine.calculateQuestionScore(null, new Question(), new Company(), "Test"));
        assertEquals(0.0, centralScoringEngine.calculateQuestionScore(new SurveyResponseAnswer(), null, new Company(), "Test"));
    }

    @Test
    void calculateQuestionScore_EmptyCorrectAnswers_ReturnsZero() {
        // Arrange
        SurveyResponseAnswer answer = new SurveyResponseAnswer();
        answer.setType("multipleChoice");
        answer.setArrayAnswer(new String[]{"always"});

        Question question = new Question();
        question.setCorrectAnswers(new String[0]);

        Company company = new Company();

        // Act
        double score = centralScoringEngine.calculateQuestionScore(answer, question, company, "Test Survey");

        // Assert
        assertEquals(0.0, score, 0.01);
    }

    @Test
    void calculateAllQuestionScores_ValidResponse_ReturnsAllScores() {
        // Arrange
        SurveyResponse response = new SurveyResponse();
        Map<String, SurveyResponseAnswer> answersMap = new HashMap<>();

        // Create test answers
        SurveyResponseAnswer answer1 = new SurveyResponseAnswer();
        answer1.setQuestionId("q1");
        answer1.setType("multipleChoice");
        answer1.setArrayAnswer(new String[]{"always"});
        answersMap.put("q1", answer1);

        SurveyResponseAnswer answer2 = new SurveyResponseAnswer();
        answer2.setQuestionId("q2");
        answer2.setType("text");
        answer2.setStringAnswer("Great answer");
        answersMap.put("q2", answer2);

        // Skip personal info
        SurveyResponseAnswer personalInfo = new SurveyResponseAnswer();
        personalInfo.setQuestionId(null);
        answersMap.put("personalInfo", personalInfo);

        response.setAnswerMap(answersMap);

        Survey survey = new Survey();
        survey.setName("Test Survey");
        List<Question> questions = Arrays.asList(
                createQuestion("q1", new String[]{"always", "often"}),
                createQuestion("q2", "What is your motivation?", true)
        );
        survey.setQuestions(questions);

        Company company = new Company();

        when(geminiService.getSurveyAnswerRelevanceScore(anyString(), anyString(), anyString()))
                .thenReturn("0.75-Good answer");

        // Act
        Map<String, Double> scores = centralScoringEngine.calculateAllQuestionScores(response, survey, company);

        // Assert
        assertEquals(2, scores.size());
        assertEquals(50.0, scores.get("q1"), 0.01); // 1.0 / 2 * 100 = 50.0
        assertEquals(75.0, scores.get("q2"), 0.01); // 0.75 * 100 = 75.0
        assertFalse(scores.containsKey("personalInfo"));
    }

    @Test
    void calculateAllQuestionScores_EmptyResponse_ReturnsEmptyMap() {
        // Arrange
        SurveyResponse response = new SurveyResponse();
        response.setAnswerMap(new HashMap<>());

        Survey survey = new Survey();
        survey.setQuestions(new ArrayList<>());

        Company company = new Company();

        // Act
        Map<String, Double> scores = centralScoringEngine.calculateAllQuestionScores(response, survey, company);

        // Assert
        assertTrue(scores.isEmpty());
    }

    @Test
    void calculateAllQuestionScores_QuestionNotFound_ContinuesProcessing() {
        // Arrange
        SurveyResponse response = new SurveyResponse();
        Map<String, SurveyResponseAnswer> answersMap = new HashMap<>();

        SurveyResponseAnswer answer = new SurveyResponseAnswer();
        answer.setQuestionId("nonexistent");
        answer.setType("multipleChoice");
        answersMap.put("q1", answer);

        response.setAnswerMap(answersMap);

        Survey survey = new Survey();
        survey.setQuestions(new ArrayList<>());

        Company company = new Company();

        // Act
        Map<String, Double> scores = centralScoringEngine.calculateAllQuestionScores(response, survey, company);

        // Assert
        assertTrue(scores.isEmpty());
    }

    @Test
    void isTypeOneQuestion_AlwaysOftenAnswers_ReturnsTrue() {
        // This test verifies the private method indirectly through public interface
        SurveyResponseAnswer answer = new SurveyResponseAnswer();
        answer.setType("multipleChoice");
        answer.setArrayAnswer(new String[]{"sometimes"});

        Question question = new Question();
        question.setCorrectAnswers(new String[]{"always", "often"});

        Company company = new Company();

        // Act
        double score = centralScoringEngine.calculateQuestionScore(answer, question, company, "Test");

        // Assert - should use fixed MCQ scoring (not variable)
        assertEquals(25.0, score, 0.01); // 0.5 / 2 * 100 = 25.0
    }

    @Test
    void isTypeOneQuestion_OtherAnswers_ReturnsFalse() {
        // This test verifies the private method indirectly through public interface
        SurveyResponseAnswer answer = new SurveyResponseAnswer();
        answer.setType("multipleChoice");
        answer.setArrayAnswer(new String[]{"Java"});

        Question question = new Question();
        question.setCorrectAnswers(new String[]{"Java", "Python", "C++"});

        Company company = new Company();

        // Act
        double score = centralScoringEngine.calculateQuestionScore(answer, question, company, "Test");

        // Assert - should use variable MCQ scoring (not fixed)
        assertEquals(33.33, score, 0.01); // 1 match / 3 total * 100 = 33.33
    }

    @Test
    void scoreExplanations_AreProperlyManaged() {
        // Arrange
        SurveyResponseAnswer answer = new SurveyResponseAnswer();
        answer.setType("text");
        answer.setStringAnswer("Test answer");

        Question question = new Question();
        question.setId("q1");
        question.setQuestion("Test question");
        question.setGemini(true);

        Company company = new Company();

        when(geminiService.getSurveyAnswerRelevanceScore(anyString(), anyString(), anyString()))
                .thenReturn("0.8-Test explanation");

        // Act
        centralScoringEngine.calculateQuestionScore(answer, question, company, "Test Survey");

        // Assert
        Map<String, String> explanations = centralScoringEngine.getScoreExplanations();
        assertEquals("Test explanation", explanations.get("q1"));

        // Test clearing
        centralScoringEngine.clearScoreExplanations();
        assertTrue(centralScoringEngine.getScoreExplanations().isEmpty());
    }

    private Question createQuestion(String id, String[] correctAnswers) {
        Question question = new Question();
        question.setId(id);
        question.setCorrectAnswers(correctAnswers);
        return question;
    }

    private Question createQuestion(String id, String questionText, boolean isGemini) {
        Question question = new Question();
        question.setId(id);
        question.setQuestion(questionText);
        question.setGemini(isGemini);
        return question;
    }
} 