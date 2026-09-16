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
public class OmissionGenerator implements CandidateGenerator {
    
    @Override
        public List<CandidateDomain> generate(Brand brand) {
            List<CandidateDomain> result = new ArrayList<>();
            String domain = brand.getPrimaryDomain();
            int dot = domain.indexOf('.');
            if (dot <= 0) return result;
    
            String name = domain.substring(0, dot);
            String tld = domain.substring(dot);

            if (name.length() <= 3){
                return result;
            }

            for (int i = 0; i < name.length(); i++) {
            String mutated = name.substring(0, i) + name.substring(i + 1);
            if (mutated.equals(name) || mutated.isBlank()) {
                continue;}

                String candidateName = mutated + tld;
                Instant now = Instant.now();
                result.add(new CandidateDomain(
                    null,
                    brand.getId(),
                    candidateName,
                    domain,
                    MutationType.OMISSION,
                    1,
                    1.0,
                    CandidateStatus.GENERATED,
                    now,
                    now
                ));
            }
        

        return result;
    }

    @Override
    public MutationType getMutationType() {
        return MutationType.OMISSION;
    }
}
