package org.tafel.squating.domain.value;

import java.time.Instant;
import java.util.List;

public record  TlsSnapshot(
    String issuer,
    String subject,
    List<String> subjectAlternativeNames,
    Instant validFrom,
    Instant validTo,
    String certificateFingerprint
) {
    public TlsSnapshot{
        subjectAlternativeNames = List.copyOf(subjectAlternativeNames);
    }
}