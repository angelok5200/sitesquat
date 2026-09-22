package org.tafel.squating.application;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.tafel.squating.config.ApplicationProperties;
import org.tafel.squating.domain.model.CandidateDomain;
import org.tafel.squating.domain.model.DomainObservation;
import org.tafel.squating.domain.value.DnsSnapshot;
import org.tafel.squating.domain.value.HttpSnapshot;
import org.tafel.squating.domain.value.MailSnapshot;
import org.tafel.squating.domain.value.MonitoringPolicy;
import org.tafel.squating.domain.value.RegistrationSnapshot;
import org.tafel.squating.domain.value.TlsSnapshot;
import org.tafel.squating.ports.outbound.CertificateDiscovery;
import org.tafel.squating.ports.outbound.DnsInspector;
import org.tafel.squating.ports.outbound.DomainRegistrationLookup;
import org.tafel.squating.ports.outbound.MailInspector;
import org.tafel.squating.ports.outbound.ScreenshotService;
import org.tafel.squating.ports.outbound.TlsInspector;
import org.tafel.squating.ports.outbound.WebInspector;

@Service
public class DomainInspectionService {

    private final DomainRegistrationLookup registrationLookup;
    private final DnsInspector dnsInspector;
    private final WebInspector webInspector;
    private final TlsInspector tlsInspector;
    private final MailInspector mailInspector;
    private final CertificateDiscovery certificateDiscovery;
    private final ScreenshotService screenshotService;
    private final ApplicationProperties applicationProperties;

    public DomainInspectionService(
            DomainRegistrationLookup registrationLookup,
            DnsInspector dnsInspector,
            WebInspector webInspector,
            TlsInspector tlsInspector,
            MailInspector mailInspector,
            CertificateDiscovery certificateDiscovery,
            ScreenshotService screenshotService,
            ApplicationProperties applicationProperties
    ) {
        this.registrationLookup = registrationLookup;
        this.dnsInspector = dnsInspector;
        this.webInspector = webInspector;
        this.tlsInspector = tlsInspector;
        this.mailInspector = mailInspector;
        this.certificateDiscovery = certificateDiscovery;
        this.screenshotService = screenshotService;
        this.applicationProperties = applicationProperties;
    }

    public DomainObservation inspect(
            CandidateDomain candidate,
            MonitoringPolicy policy
    ) {
        if (candidate == null) {
            throw new IllegalArgumentException(
                    "candidate must not be null"
            );
        }

        if (policy == null) {
            throw new IllegalArgumentException(
                    "policy must not be null"
            );
        }

        RegistrationSnapshot registration = null;
        DnsSnapshot dns = null;
        HttpSnapshot http = null;
        TlsSnapshot tls = null;
        MailSnapshot mail = null;

        List<String> certificateNames = List.of();

        String screenshotHash = null;

        String domain = candidate.getDomain();

        if (policy.checkRdap()) {
            registration =
                    registrationLookup.lookupRegistration(domain);
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

        if (policy.checkCertificateTransparency()) {
            certificateNames =
                    certificateDiscovery.findCertificates(domain);
        }

        if (policy.takeScreenshots()) {
            screenshotHash =
                    screenshotService.capture(
                            domain,
                            applicationProperties.getEvidenceDirectory()
                    );
        }

        return new DomainObservation(
                candidate.getId(),
                null,
                Set.of(),
                dns,
                http,
                UUID.randomUUID(),
                mail,
                Instant.now(),
                registration,
                screenshotHash,
                tls,
                certificateNames
        );
    }
}
