package org.tafel.squating.ports.inbound;

import java.util.List;
import java.util.UUID;

import org.tafel.squating.domain.model.CandidateDomain;

public interface ScanCandidatesUseCase {

    List<CandidateDomain> generateCandidates (UUID brandId);
    List<CandidateDomain> getCandidatesForBrand (UUID brandId);
}
