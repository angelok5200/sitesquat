package org.tafel.squating.domain.model;

import java.time.Instant;
import java.util.UUID;

import org.tafel.squating.domain.enums.CandidateStatus;
import org.tafel.squating.domain.enums.MutationType;

public class CandidateDomain {

    private final UUID id;
    private final UUID brandId;
    private final String domain;
    private final String sourceDomain;
    private final MutationType mutationType;
    private final int editDistance;
    private final double confidence;
    private final CandidateStatus status;
    private final Instant firstSeen;
    private final Instant lastSeen;

    public CandidateDomain(
            UUID brandId, String domain, String sourceDomain, MutationType mutationType, int editDistance, double confidence, CandidateStatus status, Instant firstSeen, Instant lastSeen) {
        if (brandId == null) {
            throw new IllegalArgumentException("brandId must not be null");
        }

        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }

        if (sourceDomain == null || sourceDomain.isBlank()) {
            throw new IllegalArgumentException("sourceDomain must not be blank");
        }

        if (mutationType == null) {
            throw new IllegalArgumentException("mutationType must not be null");
        }

        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        this.id = UUID.randomUUID();
        this.brandId = brandId;
        this.domain = domain;
        this.sourceDomain = sourceDomain;
        this.mutationType = mutationType;
        this.editDistance = editDistance;
        this.confidence = confidence;
        this.status = status;
        this.firstSeen = firstSeen != null ? firstSeen : Instant.now();
        this.lastSeen = lastSeen != null ? lastSeen : this.firstSeen;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBrandId() {
        return brandId;
    }

    public String getDomain() {
        return domain;
    }

    public String getSourceDomain() {
        return sourceDomain;
    }

    public MutationType getMutationType() {
        return mutationType;
    }

    public int getEditDistance() {
        return editDistance;
    }

    public double getConfidence() {
        return confidence;
    }

    public CandidateStatus getStatus() {
        return status;
    }

    public Instant getFirstSeen() {
        return firstSeen;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }
}