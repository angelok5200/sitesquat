package org.tafel.squating.scoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.tafel.squating.analysis.ContentIndicator;
import org.tafel.squating.domain.enums.RiskLevel;
import org.tafel.squating.domain.model.RiskAssessment;

@Service 
public class RiskScoringService {

    public RiskAssessment calculate(
        UUID candidateId,
        Set <ContentIndicator> indicators,
        double textSimilarity,
        boolean mxConfigured,
        boolean certificateFound,
        boolean recentRegistration 
    ){
        if (candidateId == null) throw new IllegalArgumentException("candidateId must not be null");
        int score = 0;

        List<String> triggeredRules = new ArrayList<>();
        if (textSimilarity >= 0.8) {
            score += RiskRules.HIGH_TEXT_SIMILARITY_WEIGHT;
            triggeredRules.add("High text similarity");
        }
        
        if (indicators != null) {
            for (ContentIndicator indicator : indicators) {
                int weight = RiskRules.CONTENT_WEIGHTS.getOrDefault(indicator,0);
                if (weight > 0){
                    score += weight;
                    triggeredRules.add(indicator.name());
                }
            }
        }

        if(mxConfigured){ 
            score += RiskRules.MX_CONFIGURED_WEIGHT;
            triggeredRules.add("MX CONFIGURED");
        }
        if(certificateFound){ 
            score += RiskRules.CERTIFICATE_FOUND_WEIGHT;
            triggeredRules.add("CERTIFICATE FOUND");
        }
        if(recentRegistration){ 
            score += RiskRules.RECENT_REGISTRATION_WEIGHT;
            triggeredRules.add("RECENT REGISTRATION");
        }

        RiskLevel risklevel = determineLevel(score);
        
        return new RiskAssessment(null, candidateId, score, risklevel, triggeredRules);
    }

    private RiskLevel determineLevel(int score){
        if (score <= RiskRules.LOW_MAX_SCORE) return RiskLevel.LOW;
        if (score <= RiskRules.MEDIUM_MAX_SCORE) return RiskLevel.MEDIUM;
        if (score <= RiskRules.HIGH_MAX_SCORE) return RiskLevel.HIGH;
        return RiskLevel.CRITICAL;
    }
    
}
