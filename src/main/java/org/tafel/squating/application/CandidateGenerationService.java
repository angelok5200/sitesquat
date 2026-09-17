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
    Brand brand = brandRepository.findById(brandId)
        .orElseThrow(() ->
            new IllegalArgumentException("Brand not found: " + brandId)
        );

    Set<String> monitoredTlds =
        brand.getMonitoringPolicy().monitoredTlds();

    if (monitoredTlds == null || monitoredTlds.isEmpty()) {
        return List.of();
    }

    Map<String, CandidateDomain> uniqueCandidates =
        new LinkedHashMap<>();

    for (CandidateGenerator generator : generators) {
        List<CandidateDomain> generated = generator.generate(brand);

        for (CandidateDomain candidate : generated) {
            String candidateLabel = extractLabel(candidate.getDomain());

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
                    brand.getPrimaryDomain()
                )) {
                    continue;
                }

                CandidateDomain tldCandidate = new CandidateDomain(
                    candidate.getBrandid(),
                    candidate.getConfidence(),
                    candidateDomain,
                    candidate.getEditDistance(),
                    candidate.getFirstSeen(),
                    null,
                    Instant.now(),
                    candidate.getMutationType(),
                    candidate.getSourceDomain(),
                    candidate.getStatus()
                    
                );

                uniqueCandidates.putIfAbsent(
                    candidateDomain.toLowerCase(),
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
    return candidateRepository.findByBrandId(brandId);
}

private String extractLabel(String domain) {
    if (domain == null || domain.isBlank()) {
        return null;
    }

    int dot = domain.indexOf('.');

    if (dot <= 0) {
        return null;
    }

    return domain.substring(0, dot);
}

private String normalizeTld(String tld) {
    if (tld == null) {
        return "";
    }

    String normalized = tld.trim().toLowerCase();

    while (normalized.startsWith(".")) {
        normalized = normalized.substring(1);
    }

    return normalized;
}
}