package org.tafel.squating.generators;

import java.util.List;

import org.tafel.squating.domain.enums.MutationType;
import org.tafel.squating.domain.model.Brand;
import org.tafel.squating.domain.model.CandidateDomain;

public interface CandidateGenerator {
    List<CandidateDomain> generate (Brand brand);
    MutationType getMutationType();
}
