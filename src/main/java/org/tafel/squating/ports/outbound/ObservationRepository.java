package org.tafel.squating.ports.outbound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.tafel.squating.domain.model.DomainObservation;

public interface ObservationRepository {
    
    Optional<DomainObservation> findLatestByCandidateId(UUID candidateId);
    List<DomainObservation> findByCandidateId(UUID candidateId);
    DomainObservation save(DomainObservation observation);
}
