package org.tafel.squating.generators;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.enums.CandidateStatus;
import org.tafel.squating.domain.enums.MutationType;
import org.tafel.squating.domain.model.Brand;
import org.tafel.squating.domain.model.CandidateDomain;

@Component
public class TranspositionGenerator implements CandidateGenerator  {
    
public List<CandidateDomain> generate(Brand brand) {
        List<CandidateDomain> result = new ArrayList<>();
        String domain = brand.getPrimaryDomain();
        int dot = domain.indexOf('.');
        if (dot <= 0) return result;

        String name = domain.substring(0, dot);
        String tld = domain.substring(dot);

        for (int i = 0; i < name.length() - 1; i++) {
            if (name.charAt(i) == name.charAt(i + 1)) continue;
            char[] chars = name.toCharArray();
            char temp = chars[i];
            chars[i] = chars[i + 1];
            chars[i + 1] = temp;

            String mutated = new String(chars);
            String candidateName = mutated + tld;
            Instant now = Instant.now();
            result.add(new CandidateDomain(
                null,
                brand.getId(),
                candidateName,
                domain,
                MutationType.TRANSPOSITION,
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
        return MutationType.TRANSPOSITION;
    }
}
