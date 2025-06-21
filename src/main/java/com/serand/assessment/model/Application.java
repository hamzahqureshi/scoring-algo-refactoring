package com.serand.assessment.model;

public class Application {
    private String id;
    private Candidate candidate;
    private Survey survey;
    private Company company;
    private boolean complete;
    private SurveyResponse candidateResponse;
    private Scores scores;
    private double applicationOverallScore;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }
    
    public Survey getSurvey() { return survey; }
    public void setSurvey(Survey survey) { this.survey = survey; }
    
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    
    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }
    
    public SurveyResponse getCandidateResponse() { return candidateResponse; }
    public void setCandidateResponse(SurveyResponse candidateResponse) { this.candidateResponse = candidateResponse; }
    
    public Scores getScores() { return scores; }
    public void setScores(Scores scores) { this.scores = scores; }
    
    public double getApplicationOverallScore() { return applicationOverallScore; }
    public void setApplicationOverallScore(double applicationOverallScore) { this.applicationOverallScore = applicationOverallScore; }
} 