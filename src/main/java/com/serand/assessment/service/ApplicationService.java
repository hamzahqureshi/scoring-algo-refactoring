package com.serand.assessment.service;

import com.serand.assessment.model.Application;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {
    
    public void saveApplication(Application application) {
        // Mock save
        System.out.println("Saving application: " + application.getId());
    }
} 