package org.tafel.squating.application;

import org.springframework.stereotype.Service;
import org.tafel.squating.analysis.ContentAnalysis;
import org.tafel.squating.analysis.SimilarityResult;
import org.tafel.squating.domain.model.Alert;
import org.tafel.squating.domain.model.CandidateDomain;
import org.tafel.squating.domain.model.DomainObservation;
import org.tafel.squating.domain.model.RiskAssessment;
import org.tafel.squating.domain.value.MonitoringPolicy;
import org.tafel.squating.ports.outbound.AlertRepository;
import org.tafel.squating.ports.outbound.ObservationRepository;
import org.tafel.squating.ports.outbound.RiskAssessmentRepository;

@Service
public class MonitoringService {

    private final DomainInspectionService domainInspectionService;
    private final RiskAssessmentService riskAssessmentService;
    private final AlertDecisionService alertDecisionService;
    private final ObservationRepository observationRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final AlertRepository alertRepository;

    public MonitoringService(
            DomainInspectionService domainInspectionService,
            RiskAssessmentService riskAssessmentService,
            AlertDecisionService alertDecisionService,
            ObservationRepository observationRepository,
            RiskAssessmentRepository riskAssessmentRepository,
            AlertRepository alertRepository
    ) {
        this.domainInspectionService = domainInspectionService;
        this.riskAssessmentService = riskAssessmentService;
        this.alertDecisionService = alertDecisionService;
        this.observationRepository = observationRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.alertRepository = alertRepository;
    }

    public Alert monitor(
            CandidateDomain candidate,
            MonitoringPolicy policy,
            ContentAnalysis contentAnalysis,
            SimilarityResult similarityResult,
            boolean recentRegistration
    ) {
        if (candidate == null) {
            throw new IllegalArgumentException("candidate must not be null");
        }

        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }

        DomainObservation previousObservation =
                observationRepository
                        .findLatestByCandidateId(candidate.getId())
                        .orElse(null);

        RiskAssessment previousAssessment =
                riskAssessmentRepository
                        .findLatestByCandidateId(candidate.getId())
                        .orElse(null);

        DomainObservation currentObservation =
                domainInspectionService.inspect(candidate, policy);

        RiskAssessment currentAssessment =
                riskAssessmentService.assess(
                        candidate,
                        currentObservation,
                        contentAnalysis,
                        similarityResult,
                        recentRegistration
                );

        boolean previousLoginFormDetected = false;

        boolean currentLoginFormDetected =
                contentAnalysis != null
                        && contentAnalysis.loginFormDetected();

        observationRepository.save(currentObservation);
        riskAssessmentRepository.save(currentAssessment);

        Alert alert = alertDecisionService.decide(
                candidate,
                previousObservation,
                currentObservation,
                previousAssessment,
                currentAssessment,
                previousLoginFormDetected,
                currentLoginFormDetected
        );

        if (alert != null) {
            return alertRepository.save(alert);
        }

        return null;
    }
}