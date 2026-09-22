package org.tafel.squating.scoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.tafel.squating.domain.enums.ContentIndicator;
import org.tafel.squating.domain.enums.RiskLevel;
import org.tafel.squating.domain.model.RiskAssessment;

@Service
public class RiskScoringService {

    public RiskAssessment calculate(
            UUID candidateId,
            Set<ContentIndicator> indicators,
            boolean mxConfigured,
            boolean certificateFound,
            boolean certificateTransparencyFound,
            boolean recentRegistration
    ) {
        if (candidateId == null) {
            throw new IllegalArgumentException(
                    "candidateId must not be null"
            );
        }

        int score = 0;
        List<String> triggeredRules = new ArrayList<>();

        if (indicators != null) {
            for (ContentIndicator indicator : indicators) {
                if (indicator == null) {
                    continue;
                }

                int weight = RiskRules.CONTENT_WEIGHTS
                        .getOrDefault(indicator, 0);

                if (weight > 0) {
                    score += weight;
                    triggeredRules.add(indicator.name());
                }
            }
        }

        if (mxConfigured) {
            score += RiskRules.MX_CONFIGURED_WEIGHT;
            triggeredRules.add("MX configured");
        }

        if (certificateFound) {
            score += RiskRules.CERTIFICATE_FOUND_WEIGHT;
            triggeredRules.add("TLS certificate found");
        }

        if (certificateTransparencyFound) {
            score += RiskRules.CERTIFICATE_TRANSPARENCY_WEIGHT;
            triggeredRules.add("Certificate found in Certificate Transparency");
        }

        if (recentRegistration) {
            score += RiskRules.RECENT_REGISTRATION_WEIGHT;
            triggeredRules.add("Recent registration");
        }

        RiskLevel riskLevel = determineLevel(score);

        return new RiskAssessment(
                null,
                candidateId,
                score,
                riskLevel,
                triggeredRules
        );
    }

    private RiskLevel determineLevel(int score) {
        if (score <= RiskRules.LOW_MAX_SCORE) {
            return RiskLevel.LOW;
        }

        if (score <= RiskRules.MEDIUM_MAX_SCORE) {
            return RiskLevel.MEDIUM;
        }

        if (score <= RiskRules.HIGH_MAX_SCORE) {
            return RiskLevel.HIGH;
        }

        return RiskLevel.CRITICAL;
    }
}
