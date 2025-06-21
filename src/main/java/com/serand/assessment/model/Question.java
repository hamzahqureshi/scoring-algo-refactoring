package com.serand.assessment.model;

public class Question {
    private String id;
    private String reference;
    private String question;
    private String[] availableAnswers;
    private String[] correctAnswers;
    private boolean isGemini;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public String[] getAvailableAnswers() { return availableAnswers; }
    public void setAvailableAnswers(String[] availableAnswers) { this.availableAnswers = availableAnswers; }
    
    public String[] getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(String[] correctAnswers) { this.correctAnswers = correctAnswers; }
    
    public boolean isGemini() { return isGemini; }
    public void setGemini(boolean gemini) { isGemini = gemini; }
} 