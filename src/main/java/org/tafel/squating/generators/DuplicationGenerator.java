package org.tafel.squating.generators;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.enums.CandidateStatus;
import org.tafel.squating.domain.enums.MutationType;
import org.tafel.squating.domain.model.Brand;
import org.tafel.squating.domain.model.CandidateDomain;

@Component
public class DuplicationGenerator implements CandidateGenerator  {

    public List<CandidateDomain> generate(Brand brand) {
        List<CandidateDomain> result = new ArrayList<>();

        if (brand == null || brand.getPrimaryDomain() == null || brand.getPrimaryDomain().isBlank()) return result;
        String domain = brand.getPrimaryDomain();
        int dot = domain.indexOf('.');
        if (dot <= 0) return result;

        String name = domain.substring(0, dot);
        String tld = domain.substring(dot);

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '-' || c == '.') continue;
            String mutated = name.substring(0, i) + c + name.substring(i);
            String candidateDomain = mutated + tld;
            Instant now = Instant.now();
            result.add(new CandidateDomain(
                brand.getId(),
                candidateDomain,
                null,
                MutationType.DUPLICATION, 1, 1.0,
                CandidateStatus.GENERATED, now, now
            ));
        }

        return result;
    }

    @Override
    public MutationType getMutationType() {
        return MutationType.DUPLICATION;
    }
}
