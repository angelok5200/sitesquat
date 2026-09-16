package org.tafel.squating.domain.model;
import java.time.Instant;
import java.util.UUID;

import org.tafel.squating.domain.value.DnsSnapshot;
import org.tafel.squating.domain.value.HttpSnapshot;
import org.tafel.squating.domain.value.MailSnapshot;
import org.tafel.squating.domain.value.RegistrationSnapshot;
import org.tafel.squating.domain.value.TlsSnapshot;

public class DomainObservation {
    private UUID id;
    private UUID candidateId;

    private Instant observedAt;

    private String contentHash;
    private String screenshotHash;

    private RegistrationSnapshot registration;
    private DnsSnapshot dns;
    private HttpSnapshot http;
    private TlsSnapshot tls;
    private MailSnapshot mail;

    public DomainObservation(UUID candidateId, String contentHash, DnsSnapshot dns, HttpSnapshot http, UUID id, MailSnapshot mail, Instant observedAt, RegistrationSnapshot registration, String screenshotHash, TlsSnapshot tls) {
        if (candidateId == null) {
            throw new IllegalArgumentException("candidateId cannot be null");
        }
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (observedAt == null) {
            throw new IllegalArgumentException("observedAt cannot be null");
        }
        this.candidateId = candidateId;
        this.contentHash = contentHash;
        this.dns = dns;
        this.http = http;
        this.id = id;
        this.mail = mail;
        this.observedAt = observedAt;
        this.registration = registration;
        this.screenshotHash = screenshotHash;
        this.tls = tls;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCandidateId() {
        return candidateId;
    }

    public Instant getObservedAt() {
        return observedAt;
    }

    public String getContentHash() {
        return contentHash;
    }

    public String getScreenshotHash() {
        return screenshotHash;
    }

    public RegistrationSnapshot getRegistration() {
        return registration;
    }

    public DnsSnapshot getDns() {
        return dns;
    }

    public HttpSnapshot getHttp() {
        return http;
    }

    public TlsSnapshot getTls() {
        return tls;
    }

    public MailSnapshot getMail() {
        return mail;
    }
}