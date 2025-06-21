package com.serand.assessment.model;

import java.util.Map;

public class SurveyResponse {
    private String id;
    private Survey survey;
    private Candidate candidate;
    private Application application;
    private Map<String, SurveyResponseAnswer> answerMap;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }
    
    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }
    
    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }
    
    public Map<String, SurveyResponseAnswer> getAnswerMap() { return answerMap; }
    public void setAnswerMap(Map<String, SurveyResponseAnswer> answerMap) { this.answerMap = answerMap; }
} 