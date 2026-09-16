package org.tafel.squating.domain.value;

import java.time.Instant;

public record RegistrationSnapshot(
    String registrar,
    Instant registrationDate,
    Instant expirationDate,
    boolean registered
) {}