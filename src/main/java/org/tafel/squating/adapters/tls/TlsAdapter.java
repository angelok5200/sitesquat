package org.tafel.squating.adapters.tls;

import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.TlsSnapshot;
import org.tafel.squating.ports.outbound.TlsInspector;

@Component
public class TlsAdapter implements TlsInspector {

    private static final int HTTPS_PORT = 443;
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;

    @Override
    public TlsSnapshot inspect(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }

        String normalizedDomain = normalizeDomain(domain);

        try (SSLSocket socket = createSocket(normalizedDomain)) {
            socket.setSoTimeout(CONNECT_TIMEOUT_MILLIS);

            socket.startHandshake();

            Certificate[] certificates =
                    socket.getSession().getPeerCertificates();

            if (certificates.length == 0
                    || !(certificates[0] instanceof X509Certificate certificate)) {
                return emptySnapshot();
            }

            return new TlsSnapshot(
                    certificate.getIssuerX500Principal().getName(),
                    certificate.getSubjectX500Principal().getName(),
                    extractSubjectAlternativeNames(certificate),
                    certificate.getNotBefore().toInstant(),
                    certificate.getNotAfter().toInstant(),
                    calculateFingerprint(certificate)
            );

        } catch (SocketTimeoutException e) {
            return emptySnapshot();

        } catch (Exception e) {
            return emptySnapshot();
        }
    }

    private SSLSocket createSocket(String domain) throws Exception {
        SSLSocketFactory factory =
                (SSLSocketFactory) SSLSocketFactory.getDefault();

        SSLSocket socket =
                (SSLSocket) factory.createSocket();

        socket.connect(
                new java.net.InetSocketAddress(domain, HTTPS_PORT),
                CONNECT_TIMEOUT_MILLIS
        );

        return socket;
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

            return result;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String calculateFingerprint(
            X509Certificate certificate
    ) throws Exception {
        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(certificate.getEncoded());

        StringBuilder result = new StringBuilder();

        for (byte value : hash) {
            if (!result.isEmpty()) {
                result.append(':');
            }

            result.append(String.format("%02X", value));
        }

        return result.toString();
    }

    private String normalizeDomain(String domain) {
        String normalized = domain.trim().toLowerCase();

        if (normalized.startsWith("https://")) {
            normalized = normalized.substring(8);
        } else if (normalized.startsWith("http://")) {
            normalized = normalized.substring(7);
        }

        int slashIndex = normalized.indexOf('/');

        if (slashIndex >= 0) {
            normalized = normalized.substring(0, slashIndex);
        }

        return normalized;
    }

    private TlsSnapshot emptySnapshot() {
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