package org.tafel.squating.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.tafel.squating.domain.enums.RiskLevel;

public class RiskAssessment {
    private UUID id;
    private UUID candidateId;
    
    private int totalScore;
    private RiskLevel riskLevel;

    private Instant assessedAt;
    private List<String> triggeredRuleDescriptions;

    public RiskAssessment(UUID id,
                          UUID candidateId,
                          int totalScore,
                          RiskLevel riskLevel,
                          List<String> triggeredRuleDescriptions) {
        
        if (candidateId == null){
            throw new IllegalArgumentException("candidateid must not be null");
        }
        if (riskLevel == null){
            throw new IllegalArgumentException("riskLevel must not be null");
        }
        this.assessedAt = Instant.now();
        this.candidateId = candidateId;
        this.id = id != null ? id : UUID.randomUUID();
        this.riskLevel = riskLevel;
        this.totalScore = Math.min(100, Math.max(0, totalScore));
        this.triggeredRuleDescriptions = triggeredRuleDescriptions != null ? List.copyOf(triggeredRuleDescriptions) : List.of();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public Instant getAssessedAt() {
        return assessedAt;
    }

    public List<String> getTriggeredRuleDescriptions() {
        return triggeredRuleDescriptions;
    }
}
