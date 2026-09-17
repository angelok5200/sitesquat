package org.tafel.squating.ports.outbound;

import org.tafel.squating.domain.value.RegistrationSnapshot;

public interface DomainRegistrationLookup {
    RegistrationSnapshot lookupRegistration(String domain);
}
