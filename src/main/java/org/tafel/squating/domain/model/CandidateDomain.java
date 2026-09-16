package org.tafel.squating.domain.model;

import java.time.Instant;
import java.util.UUID;

import org.tafel.squating.domain.enums.CandidateStatus;
import org.tafel.squating.domain.enums.MutationType;

public class CandidateDomain {
    private UUID id;
    private UUID brandid;

    private String domain;
    private String sourceDomain;

    private MutationType mutationType;

    private int editDistance;
    private double confidence;

    private CandidateStatus status;

    private Instant firstSeen;
    private Instant lastSeen;

    public CandidateDomain(UUID brandid, double confidence, String domain, int editDistance, Instant firstSeen, UUID id, Instant lastSeen, MutationType mutationType, String sourceDomain, CandidateStatus status) {
        this.brandid = brandid;
        this.confidence = confidence;
        this.domain = domain;
        this.editDistance = editDistance;
        this.firstSeen = firstSeen;
        this.id = id;
        this.lastSeen = lastSeen;
        this.mutationType = mutationType;
        this.sourceDomain = sourceDomain;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBrandid() {
        return brandid;
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
