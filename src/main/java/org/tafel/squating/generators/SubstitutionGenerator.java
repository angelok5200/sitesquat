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
public class SubstitutionGenerator implements CandidateGenerator {

@Override
public List<CandidateDomain> generate(Brand brand) {
    List<CandidateDomain> result = new ArrayList<>();
        
    if (brand == null || brand.getPrimaryDomain() == null || brand.getPrimaryDomain().isBlank()) return result;


    String domain = brand.getPrimaryDomain();
    int dot = domain.indexOf('.');

    if (dot <= 0) {
        return result;
    }

    String name = domain.substring(0, dot);
    String tld = domain.substring(dot);

    for (int i = 0; i < name.length(); i++) {
        char original = name.charAt(i);

        if (!Character.isLetterOrDigit(original)) {
            continue;
        }

        char[] replacements = replacementsFor(original);

        for (char replacement : replacements) {
            if (replacement == original) {
                continue;
            }

            char[] chars = name.toCharArray();
            chars[i] = replacement;

            String candidateDomain = new String(chars) + tld;
            Instant now = Instant.now();

            result.add(new CandidateDomain(
                brand.getId(),
                candidateDomain,
                null,
                MutationType.SUBSTITUTION, 1, 1.0,
                CandidateStatus.GENERATED, now, now
            ));
        }
    }

    return result;
}

private char[] replacementsFor(char character) {
    if (character >= 'a' && character <= 'z') {
        char previous = character == 'a' ? 'z' : (char) (character - 1);
        char next = character == 'z' ? 'a' : (char) (character + 1);
        return new char[]{previous, next};
    }

    if (character >= 'A' && character <= 'Z') {
        char previous = character == 'A' ? 'Z' : (char) (character - 1);
        char next = character == 'Z' ? 'A' : (char) (character + 1);
        return new char[]{previous, next};
    }

    if (character >= '0' && character <= '9') {
        char previous = character == '0' ? '9' : (char) (character - 1);
        char next = character == '9' ? '0' : (char) (character + 1);
        return new char[]{previous, next};
    }

    return new char[0];
}

@Override
public MutationType getMutationType() {
    return MutationType.SUBSTITUTION;
}
}