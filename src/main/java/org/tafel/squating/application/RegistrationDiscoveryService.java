package org.tafel.squating.application;

import org.springframework.stereotype.Service;
import org.tafel.squating.domain.model.CandidateDomain;
import org.tafel.squating.domain.value.RegistrationSnapshot;
import org.tafel.squating.ports.outbound.DomainRegistrationLookup;

@Service 
public class RegistrationDiscoveryService {
    
    private final DomainRegistrationLookup registrationLookup;

    public RegistrationDiscoveryService(DomainRegistrationLookup registrationLookup) {
        this.registrationLookup = registrationLookup;
    }

    public RegistrationSnapshot discoverRegistration(CandidateDomain candidate) {
        if (candidate == null) throw new IllegalArgumentException("candidate must not be null");
    
        return registrationLookup.lookupRegistration(candidate.getDomain());
    }
}
