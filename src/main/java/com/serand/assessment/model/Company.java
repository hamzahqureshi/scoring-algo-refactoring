package com.serand.assessment.model;

import java.util.List;

public class Company {
    private String id;
    private List<ValuesAnswer> valuesAnswers;
    private String[] culture;
    private WorkLifeBenefitsImpactDTO workLifeBenefitsImpact;
    private Weightings weightings;
    private Pillars pillars;
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public List<ValuesAnswer> getValuesAnswers() { return valuesAnswers; }
    public void setValuesAnswers(List<ValuesAnswer> valuesAnswers) { this.valuesAnswers = valuesAnswers; }
    
    public String[] getCulture() { return culture; }
    public void setCulture(String[] culture) { this.culture = culture; }
    
    public WorkLifeBenefitsImpactDTO getWorkLifeBenefitsImpact() { return workLifeBenefitsImpact; }
    public void setWorkLifeBenefitsImpact(WorkLifeBenefitsImpactDTO workLifeBenefitsImpact) { 
        this.workLifeBenefitsImpact = workLifeBenefitsImpact; 
    }
    
    public Weightings getWeightings() { return weightings; }
    public void setWeightings(Weightings weightings) { this.weightings = weightings; }
    
    public Pillars getPillars() { return pillars; }
    public void setPillars(Pillars pillars) { this.pillars = pillars; }
} 