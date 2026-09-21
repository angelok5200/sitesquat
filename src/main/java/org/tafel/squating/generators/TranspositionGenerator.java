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
public class TranspositionGenerator implements CandidateGenerator  {
    
    @Override 
    public List<CandidateDomain> generate(Brand brand) {
        List<CandidateDomain> result = new ArrayList<>();
                
        if (brand == null || brand.getPrimaryDomain() == null || brand.getPrimaryDomain().isBlank()) return result;

        String domain = brand.getPrimaryDomain();
        int dot = domain.indexOf('.');
        if (dot <= 0) return result;

        String name = domain.substring(0, dot);
        String tld = domain.substring(dot);

        if(name.length() < 2) return result;

        for (int i = 0; i < name.length() - 1; i++) {
            
            char first = name.charAt(i);
            char second = name.charAt(i + 1);

            if(first == second) continue;

            char[] chars = name.toCharArray();
            chars[i] = second;
            chars[i + 1] = first;

            String candidateName = new String(chars) + tld;
            Instant now = Instant.now();
            result.add(new CandidateDomain(
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
