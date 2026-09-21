package org.tafel.squating.application;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.tafel.squating.domain.model.CandidateDomain;
import org.tafel.squating.domain.model.DomainObservation;
import org.tafel.squating.domain.value.DnsSnapshot;
import org.tafel.squating.domain.value.HttpSnapshot;
import org.tafel.squating.domain.value.MailSnapshot;
import org.tafel.squating.domain.value.MonitoringPolicy;
import org.tafel.squating.domain.value.RegistrationSnapshot;
import org.tafel.squating.domain.value.TlsSnapshot;
import org.tafel.squating.ports.outbound.DnsInspector;
import org.tafel.squating.ports.outbound.DomainRegistrationLookup;
import org.tafel.squating.ports.outbound.MailInspector;
import org.tafel.squating.ports.outbound.TlsInspector;
import org.tafel.squating.ports.outbound.WebInspector;

@Service
public class DomainInspectionService {

    private final DomainRegistrationLookup registrationLookup;
    private final DnsInspector dnsInspector;
    private final WebInspector webInspector;
    private final TlsInspector tlsInspector;
    private final MailInspector mailInspector;

    public DomainInspectionService(
            DomainRegistrationLookup registrationLookup,
            DnsInspector dnsInspector,
            WebInspector webInspector,
            TlsInspector tlsInspector,
            MailInspector mailInspector
    ) {
        this.registrationLookup = registrationLookup;
        this.dnsInspector = dnsInspector;
        this.webInspector = webInspector;
        this.tlsInspector = tlsInspector;
        this.mailInspector = mailInspector;
    }

    public DomainObservation inspect(
            CandidateDomain candidate,
            MonitoringPolicy policy
    ) {
        if (candidate == null) {
            throw new IllegalArgumentException("candidate must not be null");
        }

        if (policy == null) {
            throw new IllegalArgumentException("policy must not be null");
        }

        RegistrationSnapshot registration = null;
        DnsSnapshot dns = null;
        HttpSnapshot http = null;
        TlsSnapshot tls = null;
        MailSnapshot mail = null;

        String domain = candidate.getDomain();

        if (policy.checkRdap()) {
            registration = registrationLookup.lookupRegistration(domain);
        }

        if (policy.checkDns()) {
            dns = dnsInspector.inspect(domain);
        }

        if (policy.checkHttp()) {
            http = webInspector.inspect(domain);
        }

        if (policy.checkTls()) {
            tls = tlsInspector.inspect(domain);
        }

        if (policy.checkMx()) {
            mail = mailInspector.inspect(domain);
        }

        return new DomainObservation(
                candidate.getId(),
                null,
                dns,
                http,
                UUID.randomUUID(),
                mail,
                Instant.now(),
                registration,
                null,
                tls
        );
    }
}