package com.serand.assessment.service;

import com.serand.assessment.model.Candidate;
import org.springframework.stereotype.Service;

@Service
public class CandidateService {
    
    public void saveCandidate(Candidate candidate) {
        // Mock save
        System.out.println("Saving candidate: " + candidate.getId());
    }
} 