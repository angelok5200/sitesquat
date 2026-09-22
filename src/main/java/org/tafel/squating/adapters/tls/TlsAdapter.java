package org.tafel.squating.adapters.tls;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.TlsSnapshot;
import org.tafel.squating.ports.outbound.TlsInspector;

@Component
public class TlsAdapter implements TlsInspector {

    private static final int HTTPS_PORT = 443;
    private static final int SOCKET_TIMEOUT_MILLIS = 5000;

    @Override
    public TlsSnapshot inspect(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }

        String normalizedDomain = normalizeDomain(domain);

        try (SSLSocket socket = createSocket(normalizedDomain)) {
            socket.setSoTimeout(SOCKET_TIMEOUT_MILLIS);

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

        } catch (SSLHandshakeException e) {
            return emptySnapshot();

        } catch (IOException e) {
            return emptySnapshot();
        }
    }

    private SSLSocket createSocket(String domain)
            throws IOException {

        SSLSocketFactory factory =
                (SSLSocketFactory) SSLSocketFactory.getDefault();

        return (SSLSocket) factory.createSocket(
                domain,
                HTTPS_PORT
        );
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

        } catch (CertificateParsingException e) {
            return List.of();
        }
    }

    private String calculateFingerprint(
        X509Certificate certificate
) {
    try {
        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(
                certificate.getEncoded()
        );

        StringBuilder result = new StringBuilder();

        for (byte value : hash) {
            if (!result.isEmpty()) {
                result.append(':');
            }

            result.append(String.format("%02X", value));
        }

        return result.toString();

    } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException(
                "SHA-256 algorithm is not available",
                e
        );

    } catch (CertificateEncodingException e) {
        throw new IllegalStateException(
                "Unable to encode TLS certificate",
                e
        );
    }
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
            normalized = normalized.substring(
                    0,
                    slashIndex
            );
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