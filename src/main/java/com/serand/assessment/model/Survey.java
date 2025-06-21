package com.serand.assessment.model;

import java.util.List;

public class Survey {
    private String id;
    private String name;
    private List<Question> questions;
    private Company company;
    private PersonalityProfile personalityProfile;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
    
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    
    public PersonalityProfile getPersonalityProfile() { return personalityProfile; }
    public void setPersonalityProfile(PersonalityProfile personalityProfile) { this.personalityProfile = personalityProfile; }
} 