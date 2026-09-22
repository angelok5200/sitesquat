package org.tafel.squating.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.tafel.squating.analysis.ContentAnalysis;
import org.tafel.squating.domain.enums.ContentIndicator;
import org.tafel.squating.domain.model.Alert;
import org.tafel.squating.domain.model.Brand;
import org.tafel.squating.domain.model.CandidateDomain;
import org.tafel.squating.domain.model.DomainObservation;
import org.tafel.squating.domain.model.RiskAssessment;
import org.tafel.squating.domain.value.HttpSnapshot;
import org.tafel.squating.domain.value.MonitoringPolicy;
import org.tafel.squating.ports.outbound.AlertRepository;
import org.tafel.squating.ports.outbound.BrandRepository;
import org.tafel.squating.ports.outbound.ContentAnalyser;
import org.tafel.squating.ports.outbound.ObservationRepository;
import org.tafel.squating.ports.outbound.RiskAssessmentRepository;

@Service
public class MonitoringService {

    private final DomainInspectionService domainInspectionService;
    private final RiskAssessmentService riskAssessmentService;
    private final AlertDecisionService alertDecisionService;

    private final BrandRepository brandRepository;
    private final ContentAnalyser contentAnalyser;

    private final ObservationRepository observationRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final AlertRepository alertRepository;

    public MonitoringService(
            DomainInspectionService domainInspectionService,
            RiskAssessmentService riskAssessmentService,
            AlertDecisionService alertDecisionService,
            BrandRepository brandRepository,
            ContentAnalyser contentAnalyser,
            ObservationRepository observationRepository,
            RiskAssessmentRepository riskAssessmentRepository,
            AlertRepository alertRepository
    ) {
        this.domainInspectionService = domainInspectionService;
        this.riskAssessmentService = riskAssessmentService;
        this.alertDecisionService = alertDecisionService;
        this.brandRepository = brandRepository;
        this.contentAnalyser = contentAnalyser;
        this.observationRepository = observationRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.alertRepository = alertRepository;
    }

    public List<Alert> monitor(
            CandidateDomain candidate,
            MonitoringPolicy policy,
            boolean recentRegistration
    ) {
        if (candidate == null) {
            throw new IllegalArgumentException(
                    "candidate must not be null"
            );
        }

        if (policy == null) {
            throw new IllegalArgumentException(
                    "policy must not be null"
            );
        }

        Brand brand = brandRepository.findById(candidate.getBrandId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Brand not found: "
                                        + candidate.getBrandId()
                        )
                );

        DomainObservation previousObservation =
                observationRepository
                        .findLatestByCandidateId(candidate.getId())
                        .orElse(null);

        RiskAssessment previousAssessment =
                riskAssessmentRepository
                        .findLatestByCandidateId(candidate.getId())
                        .orElse(null);

        DomainObservation currentObservation =
                domainInspectionService.inspect(
                        candidate,
                        policy
                );

        ContentAnalysis contentAnalysis =
                analyseContent(
                        brand,
                        currentObservation,
                        policy
                );

        DomainObservation enrichedObservation =
                enrichObservation(
                        currentObservation,
                        contentAnalysis
                );

        RiskAssessment currentAssessment =
                riskAssessmentService.assess(
                        candidate,
                        enrichedObservation,
                        contentAnalysis,
                        recentRegistration
                );

        observationRepository.save(enrichedObservation);
        riskAssessmentRepository.save(currentAssessment);

        List<Alert> alerts =
                alertDecisionService.decide(
                        candidate,
                        previousObservation,
                        enrichedObservation,
                        previousAssessment,
                        currentAssessment
                );

        if (alerts.isEmpty()) {
            return List.of();
        }

        return alerts.stream()
                .map(alertRepository::save)
                .toList();
    }

    private ContentAnalysis analyseContent(
            Brand brand,
            DomainObservation observation,
            MonitoringPolicy policy
    ) {
        if (!policy.analyzeContent()) {
            return null;
        }

        HttpSnapshot http = observation.getHttp();

        if (http == null
                || http.body() == null
                || http.body().isBlank()) {
            return null;
        }

        return contentAnalyser.analyse(
                http.body(),
                brand.getName()
        );
    }

    private DomainObservation enrichObservation(
            DomainObservation observation,
            ContentAnalysis contentAnalysis
    ) {
        Set<ContentIndicator> indicators =
                contentAnalysis != null
                        ? contentAnalysis.matchedIndicators()
                        : Set.of();

        String contentHash = null;

        if (observation.getHttp() != null
                && observation.getHttp().body() != null) {

            contentHash = sha256(
                    observation.getHttp().body()
            );
        }

        return new DomainObservation(
                observation.getCandidateId(),
                contentHash,
                indicators,
                observation.getDns(),
                observation.getHttp(),
                observation.getId(),
                observation.getMail(),
                observation.getObservedAt(),
                observation.getRegistration(),
                observation.getScreenshotHash(),
                observation.getTls(),
                observation.getCertificateNames()
        );
    }

    private String sha256(String value) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            StringBuilder result = new StringBuilder();

            for (byte b : hash) {
                result.append(
                        String.format("%02x", b)
                );
            }

            return result.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }
}
