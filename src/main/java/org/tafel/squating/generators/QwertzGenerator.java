package org.tafel.squating.generators;

import java.time.Instant;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.enums.MutationType;
import org.tafel.squating.domain.model.Brand;
import org.tafel.squating.domain.model.CandidateDomain;

import java.util.*;

import org.tafel.squating.domain.enums.CandidateStatus;

@Component
public class QwertzGenerator implements CandidateGenerator {

    private static final Map<Character, List<Character>> QWERTZ_NEIGHBORS = new HashMap<>();

    static {
        QWERTZ_NEIGHBORS.put('q', List.of('w', 'a'));
        QWERTZ_NEIGHBORS.put('w', List.of('q', 'e', 's'));
        QWERTZ_NEIGHBORS.put('e', List.of('w', 'r', 'd'));
        QWERTZ_NEIGHBORS.put('r', List.of('e', 't', 'f'));
        QWERTZ_NEIGHBORS.put('t', List.of('r', 'z', 'g'));
        QWERTZ_NEIGHBORS.put('z', List.of('t', 'u', 'h', 'y')); // Note Z <-> Y
        QWERTZ_NEIGHBORS.put('u', List.of('z', 'i', 'j'));
        QWERTZ_NEIGHBORS.put('i', List.of('u', 'o', 'k'));
        QWERTZ_NEIGHBORS.put('o', List.of('i', 'p', 'l'));
        QWERTZ_NEIGHBORS.put('p', List.of('o', 'l'));

        QWERTZ_NEIGHBORS.put('a', List.of('q', 's', 'y'));
        QWERTZ_NEIGHBORS.put('s', List.of('a', 'd', 'w', 'x'));
        QWERTZ_NEIGHBORS.put('d', List.of('s', 'f', 'e', 'c'));
        QWERTZ_NEIGHBORS.put('f', List.of('d', 'g', 'r', 'v'));
        QWERTZ_NEIGHBORS.put('g', List.of('f', 'h', 't', 'b'));
        QWERTZ_NEIGHBORS.put('h', List.of('g', 'j', 'z', 'n'));
        QWERTZ_NEIGHBORS.put('j', List.of('h', 'k', 'u', 'm'));
        QWERTZ_NEIGHBORS.put('k', List.of('j', 'l', 'i'));
        QWERTZ_NEIGHBORS.put('l', List.of('k', 'o', 'p'));

        QWERTZ_NEIGHBORS.put('y', List.of('z', 'a', 'x'));
        QWERTZ_NEIGHBORS.put('x', List.of('y', 'c', 's'));
        QWERTZ_NEIGHBORS.put('c', List.of('x', 'v', 'd'));
        QWERTZ_NEIGHBORS.put('v', List.of('c', 'b', 'f'));
        QWERTZ_NEIGHBORS.put('b', List.of('v', 'n', 'g'));
        QWERTZ_NEIGHBORS.put('n', List.of('b', 'm', 'h'));
        QWERTZ_NEIGHBORS.put('m', List.of('n', 'j'));
    }

    @Override
    public List<CandidateDomain> generate(Brand brand) {
        List<CandidateDomain> result = new ArrayList<>();
        String domain = brand.getPrimaryDomain();
        int dot = domain.indexOf('.');
        if (dot <= 0) return result;

        String name = domain.substring(0, dot);
        String tld = domain.substring(dot);

        for (int i = 0; i < name.length(); i++) {
            char original = name.charAt(i);
            List<Character> neighbors = QWERTZ_NEIGHBORS.get(original);
            if (neighbors != null) { continue;}
                for (char neighbor : neighbors) {
                    String mutated = name.substring(0, i) + neighbor + name.substring(i + 1);
                    String candidateDomain = mutated + tld;
                    Instant now = Instant.now();
                    result.add(new CandidateDomain(
                        null,
                        brand.getId(),
                        candidateDomain,
                        domain,
                        MutationType.QWERTZ,
                        1,
                        1.0,
                        CandidateStatus.GENERATED,
                        now,
                        now
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