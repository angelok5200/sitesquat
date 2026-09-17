package org.tafel.squating.adapters.tls;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.TlsSnapshot;
import org.tafel.squating.ports.outbound.TlsInspector;

@Component
public class TlsAdapter implements TlsInspector {

    private static final int TLS_PORT = 443;
    private static final int CONNECTION_TIMEOUT_MILLIS = 10_000;

    @Override
    public TlsSnapshot inspect(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException(
                "domain must not be blank"
            );
        }

        String normalizedDomain = normalizeDomain(domain);

        try {
            SSLSocketFactory factory =
                (SSLSocketFactory) SSLSocketFactory.getDefault();

            try (SSLSocket socket =
                     (SSLSocket) factory.createSocket()) {

                socket.connect(
                    new InetSocketAddress(
                        normalizedDomain,
                        TLS_PORT
                    ),
                    CONNECTION_TIMEOUT_MILLIS
                );

                socket.startHandshake();

                SSLSession session = socket.getSession();

                X509Certificate certificate =
                    getCertificate(session);

                return new TlsSnapshot(
                    certificate.getIssuerX500Principal()
                        .getName(),

                    certificate.getSubjectX500Principal()
                        .getName(),

                    extractSubjectAlternativeNames(
                        certificate
                    ),

                    certificate.getNotBefore()
                        .toInstant(),

                    certificate.getNotAfter()
                        .toInstant(),

                    calculateFingerprint(certificate)
                );
            }

        } catch (Exception e) {
            return new TlsSnapshot(
                null,
                null,
                List.of(),
                null,
                null,
                null
            );
        }
    }

    private X509Certificate getCertificate(
        SSLSession session
    ) throws SSLPeerUnverifiedException {

        Certificate[] certificates =
            session.getPeerCertificates();

        if (certificates.length == 0) {
            throw new IllegalStateException(
                "No peer certificate returned"
            );
        }

        if (!(certificates[0] instanceof X509Certificate)) {
            throw new IllegalStateException(
                "Peer certificate is not X509"
            );
        }

        return (X509Certificate) certificates[0];
    }

    private List<String> extractSubjectAlternativeNames(
        X509Certificate certificate
    ) {

        try {
            Collection<List<?>> names =
                certificate.getSubjectAlternativeNames();

            if (names == null) {
                return List.of();
            }

            List<String> result = new ArrayList<>();

            for (List<?> entry : names) {

                if (entry == null || entry.size() < 2) {
                    continue;
                }

                Object value = entry.get(1);

                if (value != null) {
                    result.add(value.toString());
                }
            }

            return List.copyOf(result);

        } catch (Exception e) {
            return List.of();
        }
    }

    private String calculateFingerprint(
        X509Certificate certificate
    ) {

        try {
            byte[] encoded =
                certificate.getEncoded();

            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] hash =
                digest.digest(encoded);

            StringBuilder result =
                new StringBuilder();

            for (byte value : hash) {
                result.append(
                    String.format("%02X", value)
                );
            }

            return result.toString();

        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeDomain(String domain) {
        String normalized = domain.trim();

        if (normalized.startsWith("https://")) {
            normalized =
                normalized.substring(8);
        }

        if (normalized.startsWith("http://")) {
            normalized =
                normalized.substring(7);
        }

        int slash = normalized.indexOf('/');

        if (slash >= 0) {
            normalized =
                normalized.substring(0, slash);
        }

        return normalized;
    }
}