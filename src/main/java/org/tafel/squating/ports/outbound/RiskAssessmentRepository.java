package org.tafel.squating.ports.outbound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.tafel.squating.domain.model.RiskAssessment;

public interface RiskAssessmentRepository {
    Optional<RiskAssessment> findById(UUID id);
    Optional<RiskAssessment> findLatestByCandidateId(UUID candidateId);
    List<RiskAssessment> findByCandidateId(UUID candidateId);
    RiskAssessment save(RiskAssessment assessment);
}
