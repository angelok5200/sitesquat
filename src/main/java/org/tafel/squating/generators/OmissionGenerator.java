package org.tafel.squating.generators;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.enums.CandidateStatus;
import org.tafel.squating.domain.enums.MutationType;
import org.tafel.squating.domain.model.Brand;
import org.tafel.squating.domain.model.CandidateDomain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class OmissionGenerator implements CandidateGenerator {

    @Override
    public List<CandidateDomain> generate(Brand brand) {
        List<CandidateDomain> result = new ArrayList<>();

        if (brand == null || brand.getPrimaryDomain() == null
                || brand.getPrimaryDomain().isBlank()) {
            return result;
        }

        String sourceDomain = brand.getPrimaryDomain();
        int dot = sourceDomain.indexOf('.');

        if (dot <= 0) {
            return result;
        }

        String name = sourceDomain.substring(0, dot);
        String tld = sourceDomain.substring(dot);

        if (name.length() <= 1) {
            return result;
        }

        for (int i = 0; i < name.length(); i++) {
            String mutatedName =
                    name.substring(0, i) + name.substring(i + 1);

            String candidateDomain = mutatedName + tld;
            Instant now = Instant.now();

            result.add(new CandidateDomain(
                    brand.getId(),
                    candidateDomain,
                    null,
                    MutationType.OMISSION, 1, 1.0,
                    CandidateStatus.GENERATED, now, now
            ));
        }

        return result;
    }

    @Override
    public MutationType getMutationType() {
        return MutationType.OMISSION;
    }
}