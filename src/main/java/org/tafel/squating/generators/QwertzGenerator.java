package org.tafel.squating.generators;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.enums.CandidateStatus;
import org.tafel.squating.domain.enums.MutationType;
import org.tafel.squating.domain.model.Brand;
import org.tafel.squating.domain.model.CandidateDomain;

@Component
public class QwertzGenerator implements CandidateGenerator {

    private static final Map<Character, List<Character>> NEIGHBORS = new HashMap<>();

    static {
        NEIGHBORS.put('q', List.of('w', 'a'));
        NEIGHBORS.put('w', List.of('q', 'a', 'e', 's'));
        NEIGHBORS.put('e', List.of('w', 's', 'r', 'd'));
        NEIGHBORS.put('r', List.of('e', 'd', 't', 'f'));
        NEIGHBORS.put('t', List.of('r', 'f', 'z', 'g'));
        NEIGHBORS.put('z', List.of('t', 'u', 'h', 'g')); // Note Z <-> Y
        NEIGHBORS.put('u', List.of('z', 'h', 'i', 'j'));
        NEIGHBORS.put('i', List.of('u', 'j', 'o', 'k'));
        NEIGHBORS.put('o', List.of('i', 'k', 'l'));
        NEIGHBORS.put('p', List.of('o', 'l'));

        NEIGHBORS.put('a', List.of('q', 's', 'w'));
        NEIGHBORS.put('s', List.of('a', 'e', 'd', 'w', 'x'));
        NEIGHBORS.put('d', List.of('s', 'r', 'f', 'e', 'c'));
        NEIGHBORS.put('f', List.of('d', 't', 'g', 'r', 'v'));
        NEIGHBORS.put('g', List.of('f', 'z', 'h', 't', 'b'));
        NEIGHBORS.put('h', List.of('g', 'j', 'y', 'u', 'n'));
        NEIGHBORS.put('j', List.of('h', 'i', 'k', 'u', 'm'));
        NEIGHBORS.put('k', List.of('j', 'o', 'l', 'i'));
        NEIGHBORS.put('l', List.of('k', 'o', 'p'));

        NEIGHBORS.put('y', List.of('a', 'x'));
        NEIGHBORS.put('x', List.of('d', 'y', 'c', 's'));
        NEIGHBORS.put('c', List.of('f', 'x', 'v', 'd'));
        NEIGHBORS.put('v', List.of('g', 'c', 'b', 'f'));
        NEIGHBORS.put('b', List.of('v', 'h', 'n', 'g'));
        NEIGHBORS.put('n', List.of('b', 'j', 'm', 'h'));
        NEIGHBORS.put('m', List.of('n', 'j', 'k'));
    }

    @Override
    public List<CandidateDomain> generate(Brand brand) {
        List<CandidateDomain> result = new ArrayList<>();

        if (brand == null || brand.getPrimaryDomain() == null || brand.getPrimaryDomain().isBlank()) return result;


        String domain = brand.getPrimaryDomain();
        int dot = domain.indexOf('.');
        if (dot <= 0) return result;

        String name = domain.substring(0, dot).toLowerCase();
        String tld = domain.substring(dot);

        for (int i = 0; i < name.length(); i++) {
            char original = name.charAt(i);
            List<Character> neighbors = NEIGHBORS.get(original);
            if (neighbors != null) continue;
                for (char neighbor : neighbors) {
                    String mutated = name.substring(0, i) + neighbor + name.substring(i + 1);
                    String candidateDomain = mutated + tld;
                    Instant now = Instant.now();
                    result.add(new CandidateDomain(
                        brand.getId(),
                        candidateDomain,
                        null,
                        MutationType.QWERTZ, 1, 1.0,
                        CandidateStatus.GENERATED, now, now
                    ));
            }
        }

        return result;
    }

    @Override
    public MutationType getMutationType() {
        return MutationType.QWERTZ;
    }
}