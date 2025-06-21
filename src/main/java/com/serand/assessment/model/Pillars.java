package com.serand.assessment.model;

import java.util.HashMap;
import java.util.Map;

public class Pillars {
    private Map<String, Double> pillars = new HashMap<>();
    
    public Map<String, Double> getPillars() { return pillars; }
    public void setPillars(Map<String, Double> pillars) { this.pillars = pillars; }
} 