package org.tafel.squating.ports.outbound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.tafel.squating.domain.enums.EvidenceType;
import org.tafel.squating.domain.model.Evidence;

public interface EvidenceRepository {
    Optional<Evidence> findById(UUID id);
    List<Evidence> findByObservationId(UUID observationid);

    Optional<Evidence> findByObservationIdAndType (
        UUID observationid,
        EvidenceType type
    );
    
    void save (Evidence evidence);
}
