package com.serand.assessment.service;

import com.serand.assessment.model.Application;
import org.springframework.stereotype.Service;

@Service
public class ApplicationTrackingService {
    
    public void pushingScoreToAts(Application application) {
        // Mock implementation
        System.out.println("Pushing scores to ATS for application: " + application.getId());
    }
} 