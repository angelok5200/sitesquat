package org.tafel.squating.adapters.certificate;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.tafel.squating.ports.outbound.CertificateDiscovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CertificateAdapter implements CertificateDiscovery {

    private static final String CRT_SH_URL =
        "https://crt.sh/?q=%25s&output=json";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CertificateAdapter(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> findCertificate(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException(
                "domain must not be blank"
            );
        }

        String normalizedDomain =
            normalizeDomain(domain);

        String encodedDomain =
            URLEncoder.encode(
                "%." + normalizedDomain,
                StandardCharsets.UTF_8
            );

        String url =
            String.format(
                CRT_SH_URL,
                encodedDomain
            );

        HttpRequest request =
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header(
                    "Accept",
                    "application/json"
                )
                .header(
                    "User-Agent",
                    "SiteSquatingMonitor/1.0"
                )
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            if (response.statusCode() != 200) {
                return List.of();
            }

            JsonNode root =
                objectMapper.readTree(response.body());

            if (!root.isArray()) {
                return List.of();
            }

            List<String> certificates =
                new ArrayList<>();

            for (JsonNode certificate : root) {
                String commonName =
                    certificate
                        .path("common_name")
                        .asText(null);

                if (commonName != null &&
                    !commonName.isBlank()) {

                    certificates.add(commonName);
                }

                JsonNode nameValue =
                    certificate.get("name_value");

                if (nameValue == null ||
                    nameValue.isNull()) {
                    continue;
                }

                String[] names =
                    nameValue.asText().split("\\R");

                for (String name : names) {
                    String normalized =
                        name.trim().toLowerCase();

                    if (!normalized.isBlank()) {
                        certificates.add(normalized);
                    }
                }
            }

            return certificates.stream()
                .distinct()
                .toList();

        } catch (Exception e) {
            return List.of();
        }
    }

    private String normalizeDomain(String domain) {
        String normalized =
            domain.trim().toLowerCase();

        if (normalized.startsWith("https://")) {
            normalized =
                normalized.substring(8);
        }

        if (normalized.startsWith("http://")) {
            normalized =
                normalized.substring(7);
        }

        int slash =
            normalized.indexOf('/');

        if (slash >= 0) {
            normalized =
                normalized.substring(0, slash);
        }

        if (normalized.endsWith(".")) {
            normalized =
                normalized.substring(
                    0,
                    normalized.length() - 1
                );
        }

        return normalized;
    }
}