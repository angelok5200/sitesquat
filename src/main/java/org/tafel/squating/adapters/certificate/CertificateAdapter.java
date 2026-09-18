package org.tafel.squating.adapters.certificate;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.tafel.squating.ports.outbound.CertificateDiscovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CertificateAdapter implements CertificateDiscovery {

    private static final String CRT_SH_URL =
            "https://crt.sh/?q=%25.%s&output=json";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public CertificateAdapter(
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> findCertificates(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }

        String normalizedDomain = normalizeDomain(domain);

        String encodedDomain = URLEncoder.encode(
                normalizedDomain,
                StandardCharsets.UTF_8
        );

        URI uri = URI.create(
                String.format(CRT_SH_URL, encodedDomain)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .header(
                        "User-Agent",
                        "Mozilla/5.0 (compatible; SiteSquating/1.0)"
                )
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response.body());

            if (!root.isArray()) {
                return List.of();
            }

            Set<String> certificates = new LinkedHashSet<>();

            for (JsonNode certificate : root) {
                addValue(
                        certificates,
                        certificate.path("common_name").asText(null)
                );

                String nameValue =
                        certificate.path("name_value").asText(null);

                if (nameValue != null) {
                    for (String value : nameValue.split("\\R")) {
                        addValue(certificates, value);
                    }
                }
            }

            return List.copyOf(certificates);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();

        } catch (IOException | IllegalArgumentException e) {
            return List.of();
        }
    }

    private void addValue(Set<String> values, String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        values.add(value.trim().toLowerCase());
    }

    private String normalizeDomain(String domain) {
        String normalized = domain.trim().toLowerCase();

        if (normalized.endsWith(".")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
        }

        return normalized;
    }
}