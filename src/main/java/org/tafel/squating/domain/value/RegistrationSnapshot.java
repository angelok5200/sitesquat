package org.tafel.squating.domain.value;

import java.time.Instant;

public record RegistrationSnapshot(
    boolean registered,
    Instant registrationDate,
    Instant expirationDate,
    String registrar
) {}