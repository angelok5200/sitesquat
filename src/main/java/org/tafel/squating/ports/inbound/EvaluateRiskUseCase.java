package org.tafel.squating.ports.inbound;

import java.util.List;
import java.util.UUID;

import org.tafel.squating.domain.model.Alert;
import org.tafel.squating.domain.model.CandidateDomain;
import org.tafel.squating.domain.model.RiskAssessment;

public interface EvaluateRiskUseCase {

    RiskAssessment evaluateCandidate (CandidateDomain candidate);
    List<Alert> evaluateBrandRisksAndAlert (UUID brandId);
    List<Alert> getPendingAlerts (UUID brandId);
}
