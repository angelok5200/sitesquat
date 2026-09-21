package org.tafel.squating.application;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.tafel.squating.analysis.ContentAnalysis;
import org.tafel.squating.analysis.SimilarityResult;
import org.tafel.squating.domain.enums.ContentIndicator;
import org.tafel.squating.domain.model.CandidateDomain;
import org.tafel.squating.domain.model.DomainObservation;
import org.tafel.squating.domain.model.RiskAssessment;
import org.tafel.squating.scoring.RiskScoringService;

@Service
public class RiskAssessmentService {

    private final RiskScoringService riskScoringService;

    public RiskAssessmentService(RiskScoringService riskScoringService) {
        this.riskScoringService = riskScoringService;
    }

    public RiskAssessment assess(
            CandidateDomain candidate,
            DomainObservation observation,
            ContentAnalysis contentAnalysis,
            SimilarityResult similarityResult,
            boolean recentRegistration
    ) {
        if (candidate == null) {
            throw new IllegalArgumentException("candidate must not be null");
        }

        if (observation == null) {
            throw new IllegalArgumentException("observation must not be null");
        }

        if (!candidate.getId().equals(observation.getCandidateId())) {
            throw new IllegalArgumentException(
                    "Candidate id does not match observation"
            );
        }

        if (similarityResult == null) {
            throw new IllegalArgumentException(
                    "similarityResult must not be null"
            );
        }

        Set<ContentIndicator> indicators = contentAnalysis != null
                ? contentAnalysis.matchedIndicators()
                : Set.of();

        boolean mxConfigured = observation.getMail() != null
                && observation.getMail().mxConfigured();

        boolean certificateFound = observation.getTls() != null
                && observation.getTls().certificateFingerprint() != null
                && !observation.getTls().certificateFingerprint().isBlank();

        return riskScoringService.calculate(
                candidate.getId(),
                indicators,
                similarityResult.textSimilarity(),
                mxConfigured,
                certificateFound,
                recentRegistration
        );
    }
}