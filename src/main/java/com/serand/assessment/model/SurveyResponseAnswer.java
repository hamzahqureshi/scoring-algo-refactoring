package com.serand.assessment.model;

public class SurveyResponseAnswer {
    private String questionId;
    private String reference;
    private String type; // "multipleChoice", "text", "coding"
    private String[] arrayAnswer;
    private String stringAnswer;
    private int intAnswer;
    private double questionScore;
    private String scoreExplanation;
    
    // Getters and setters
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String[] getArrayAnswer() { return arrayAnswer; }
    public void setArrayAnswer(String[] arrayAnswer) { this.arrayAnswer = arrayAnswer; }
    
    public String getStringAnswer() { return stringAnswer; }
    public void setStringAnswer(String stringAnswer) { this.stringAnswer = stringAnswer; }
    
    public int getIntAnswer() { return intAnswer; }
    public void setIntAnswer(int intAnswer) { this.intAnswer = intAnswer; }
    
    public double getQuestionScore() { return questionScore; }
    public void setQuestionScore(double questionScore) { this.questionScore = questionScore; }
    
    public String getScoreExplanation() { return scoreExplanation; }
    public void setScoreExplanation(String scoreExplanation) { this.scoreExplanation = scoreExplanation; }
} 