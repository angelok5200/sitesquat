package org.tafel.squating.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.tafel.squating.domain.model.Brand;
import org.tafel.squating.domain.model.CandidateDomain;
import org.tafel.squating.generators.CandidateGenerator;
import org.tafel.squating.ports.inbound.ScanCandidatesUseCase;
import org.tafel.squating.ports.outbound.BrandRepository;
import org.tafel.squating.ports.outbound.CandidateRepository;

@Service
public class CandidateGenerationService implements ScanCandidatesUseCase {

    private final BrandRepository brandRepository;
    private final CandidateRepository candidateRepository;
    private final List<CandidateGenerator> generators;

    public CandidateGenerationService(
            BrandRepository brandRepository,
            CandidateRepository candidateRepository,
            List<CandidateGenerator> generators
    ) {
        this.brandRepository = brandRepository;
        this.candidateRepository = candidateRepository;
        this.generators = generators;
    }

    @Override
    public List<CandidateDomain> generateCandidates(UUID brandId) {
        if (brandId == null) {
            throw new IllegalArgumentException("brandId must not be null");
        }

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Brand not found: " + brandId));

        if (brand.getMonitoringPolicy() == null) {
            throw new IllegalStateException(
                    "Monitoring policy is not configured for brand: " + brandId
            );
        }

        Set<String> monitoredTlds =
                brand.getMonitoringPolicy().monitoredTlds();

        if (monitoredTlds == null || monitoredTlds.isEmpty()) {
            return List.of();
        }

        Map<String, CandidateDomain> uniqueCandidates =
                new LinkedHashMap<>();

        for (CandidateGenerator generator : generators) {
            List<CandidateDomain> generated = generator.generate(brand);

            if (generated == null || generated.isEmpty()) {
                continue;
            }

            for (CandidateDomain candidate : generated) {
                if (candidate == null) {
                    continue;
                }

                String candidateLabel =
                        extractLabel(candidate.getDomain());

                if (candidateLabel == null || candidateLabel.isBlank()) {
                    continue;
                }

                for (String tld : monitoredTlds) {
                    String normalizedTld = normalizeTld(tld);

                    if (normalizedTld.isBlank()) {
                        continue;
                    }

                    String candidateDomain =
                            candidateLabel + "." + normalizedTld;

                    if (candidateDomain.equalsIgnoreCase(
                            brand.getPrimaryDomain())) {
                        continue;
                    }

                    String deduplicationKey =
                            candidateDomain.toLowerCase();

                    if (uniqueCandidates.containsKey(deduplicationKey)) {
                        continue;
                    }

                    CandidateDomain tldCandidate =
                            new CandidateDomain(
                                    candidate.getBrandId(),
                                    candidateDomain,
                                    candidate.getSourceDomain(),
                                    candidate.getMutationType(),
                                    candidate.getEditDistance(),
                                    candidate.getConfidence(),
                                    candidate.getStatus(),
                                    candidate.getFirstSeen(),
                                    Instant.now()
                            );

                    uniqueCandidates.put(
                            deduplicationKey,
                            tldCandidate
                    );
                }
            }
        }

        List<CandidateDomain> result =
                new ArrayList<>(uniqueCandidates.values());

        for (CandidateDomain candidate : result) {
            candidateRepository.save(candidate);
        }

        return result;
    }

    @Override
    public List<CandidateDomain> getCandidatesForBrand(UUID brandId) {
        if (brandId == null) {
            throw new IllegalArgumentException("brandId must not be null");
        }

        return candidateRepository.findByBrandId(brandId);
    }

    private String extractLabel(String domain) {
        if (domain == null || domain.isBlank()) {
            return null;
        }

        String normalizedDomain =
                domain.trim().toLowerCase();

        int dot = normalizedDomain.indexOf('.');

        if (dot <= 0) {
            return null;
        }

        return normalizedDomain.substring(0, dot);
    }

    private String normalizeTld(String tld) {
        if (tld == null) {
            return "";
        }

        String normalized =
                tld.trim().toLowerCase();

        while (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }
}