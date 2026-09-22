package org.tafel.squating.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.tafel.squating.analysis.ContentAnalysis;
import org.tafel.squating.analysis.SimilarityResult;
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
import org.tafel.squating.ports.outbound.SimilarityAnalyzer;
import org.tafel.squating.ports.outbound.RiskAssessmentRepository;
import org.tafel.squating.ports.outbound.WebInspector;
import org.tafel.squating.domain.enums.ContentIndicator;

@Service
public class MonitoringService {

    private final DomainInspectionService domainInspectionService;
    private final RiskAssessmentService riskAssessmentService;
    private final AlertDecisionService alertDecisionService;

    private final BrandRepository brandRepository;
    private final ContentAnalyser contentAnalyser;
    private final SimilarityAnalyzer similarityAnalyzer;
    private final WebInspector webInspector;

    private final ObservationRepository observationRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final AlertRepository alertRepository;

    public MonitoringService(
            DomainInspectionService domainInspectionService,
            RiskAssessmentService riskAssessmentService,
            AlertDecisionService alertDecisionService,
            BrandRepository brandRepository,
            ContentAnalyser contentAnalyser,
            SimilarityAnalyzer similarityAnalyzer,
            WebInspector webInspector,
            ObservationRepository observationRepository,
            RiskAssessmentRepository riskAssessmentRepository,
            AlertRepository alertRepository
    ) {
        this.domainInspectionService = domainInspectionService;
        this.riskAssessmentService = riskAssessmentService;
        this.alertDecisionService = alertDecisionService;
        this.brandRepository = brandRepository;
        this.contentAnalyser = contentAnalyser;
        this.similarityAnalyzer = similarityAnalyzer;
        this.webInspector = webInspector;
        this.observationRepository = observationRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.alertRepository = alertRepository;
    }

    public Alert monitor(
            CandidateDomain candidate,
            MonitoringPolicy policy,
            boolean recentRegistration
    ) {
        if (candidate == null) {
            throw new IllegalArgumentException("candidate must not be null");
        }

        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }

        Brand brand = brandRepository.findById(candidate.getBrandId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Brand not found: " + candidate.getBrandId()
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
                domainInspectionService.inspect(candidate, policy);

        ContentAnalysis contentAnalysis =
                analyseContent(candidate, brand, currentObservation, policy);

        SimilarityResult similarityResult =
                calculateSimilarity(
                        brand,
                        currentObservation,
                        contentAnalysis,
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
                        similarityResult,
                        recentRegistration
                );

        observationRepository.save(enrichedObservation);
        riskAssessmentRepository.save(currentAssessment);

        Alert alert = alertDecisionService.decide(
                candidate,
                previousObservation,
                enrichedObservation,
                previousAssessment,
                currentAssessment
        );

        if (alert != null) {
            return alertRepository.save(alert);
        }

        return null;
    }

    private ContentAnalysis analyseContent(
            CandidateDomain candidate,
            Brand brand,
            DomainObservation observation,
            MonitoringPolicy policy
    ) {
        if (!policy.analyzeContent()) {
            return null;
        }

        HttpSnapshot http = observation.getHttp();

        if (http == null || http.body() == null || http.body().isBlank()) {
            return null;
        }

        return contentAnalyser.analyse(
                http.body(),
                brand.getName()
        );
    }

    private SimilarityResult calculateSimilarity(
            Brand brand,
            DomainObservation observation,
            ContentAnalysis contentAnalysis,
            MonitoringPolicy policy
    ) {
        if (!policy.calculateSimilarity()) {
            return emptySimilarityResult();
        }

        HttpSnapshot candidateHttp = observation.getHttp();

        if (candidateHttp == null || candidateHttp.body() == null) {
            return emptySimilarityResult();
        }

        if (brand.getReferenceUrl() == null
                || brand.getReferenceUrl().isBlank()) {
            return emptySimilarityResult();
        }

        HttpSnapshot referenceHttp =
                webInspector.inspect(brand.getReferenceUrl());

        String candidateText = candidateHttp.body();
        String referenceText = referenceHttp != null
                ? referenceHttp.body()
                : null;

        return similarityAnalyzer.analyze(
                candidateText,
                referenceText,
                null,
                null,
                null,
                null
        );
    }

    private SimilarityResult emptySimilarityResult() {
        return new SimilarityResult(
                0.0,
                0.0,
                false
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