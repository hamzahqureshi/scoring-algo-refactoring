package com.serand.assessment.model;

import java.util.Map;
import java.util.HashMap;

public class Candidate {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private Map<String, Double> cvScoreMap = new HashMap<>();
    private Map<String, Double> surveyScore = new HashMap<>();
    private Map<String, Double> overallScoreMap = new HashMap<>();
    private double overallScore;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public Map<String, Double> getCvScoreMap() { return cvScoreMap; }
    public void setCvScoreMap(Map<String, Double> cvScoreMap) { this.cvScoreMap = cvScoreMap; }
    
    public Map<String, Double> getSurveyScore() { return surveyScore; }
    public void setSurveyScore(Map<String, Double> surveyScore) { this.surveyScore = surveyScore; }
    
    public Map<String, Double> getOverallScoreMap() { return overallScoreMap; }
    public void setOverallScoreMap(Map<String, Double> overallScoreMap) { this.overallScoreMap = overallScoreMap; }
    
    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }
} 