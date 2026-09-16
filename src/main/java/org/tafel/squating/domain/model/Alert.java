package org.tafel.squating.domain.model;

import java.time.Instant;
import java.util.UUID;

import org.tafel.squating.domain.enums.AlertType;
import org.tafel.squating.domain.enums.RiskLevel;

public class Alert {
    private final UUID id;
    private final UUID brandId;
    private final UUID candidateId;

    private final String domain;

    private final AlertType type;
    private final RiskLevel severity;

    private final String headline;
    private final String message;

    private boolean acknowledge;
    private final  Instant createdAt;

    public Alert(boolean acknowledge, UUID brandId, UUID candidateId, Instant createdAt, String domain, String headline, UUID id, String message, RiskLevel severity, AlertType type) {
        
        if (brandId == null){
            throw new IllegalArgumentException("brandId must not be null");
        }
        if (candidateId == null){
            throw new IllegalArgumentException("candidateId must not be null");
        }
        if (type == null){
            throw new IllegalArgumentException("type must not be null");
        }
        if (severity == null){
            throw new IllegalArgumentException("severity must not be null");
        }
        if (domain == null || domain.isBlank()){
            throw new IllegalArgumentException("domain must not be null");
        }

        this.acknowledge = acknowledge;
        this.brandId = brandId;
        this.candidateId = candidateId;
        this.createdAt = Instant.now();
        this.domain = domain;
        this.headline = headline;
        this.id = id != null ? id : UUID.randomUUID();
        this.message = message;
        this.severity = severity;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBrandId() {
        return brandId;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public String getDomain() {
        return domain;
    }

    public AlertType getType() {
        return type;
    }

    public RiskLevel getSeverity() {
        return severity;
    }

    public String getHeadline() {
        return headline;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAcknowledge() {
        return acknowledge;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setAcknowledge(boolean acknowledge) {
        this.acknowledge = acknowledge;
    }
    


}
