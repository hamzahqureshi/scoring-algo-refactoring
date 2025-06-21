package com.serand.assessment.service;

import com.serand.assessment.model.Mindset;
import com.serand.assessment.model.PersonalityProfile;
import org.springframework.stereotype.Service;

@Service
public class SurveyService {
    
    public Mindset calculateZindexScore(Mindset candidateMindset, PersonalityProfile targetProfile) {
        // Mock Z-index calculation - in real implementation this would normalize scores
        Mindset zIndexed = new Mindset();
        zIndexed.setNeuroticism(candidateMindset.getNeuroticism() * 0.8);
        zIndexed.setExtraversion(candidateMindset.getExtraversion() * 0.8);
        zIndexed.setOpenness(candidateMindset.getOpenness() * 0.8);
        zIndexed.setConscientiousness(candidateMindset.getConscientiousness() * 0.8);
        zIndexed.setAgreeableness(candidateMindset.getAgreeableness() * 0.8);
        zIndexed.setSocialDesirability(candidateMindset.getSocialDesirability() * 0.8);
        return zIndexed;
    }
} 