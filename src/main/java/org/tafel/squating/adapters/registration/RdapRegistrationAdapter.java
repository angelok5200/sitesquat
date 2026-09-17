package org.tafel.squating.adapters.registration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.RegistrationSnapshot;
import org.tafel.squating.ports.outbound.DomainRegistrationLookup;

import java.io.IOException;
import java.net.IDN;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class RdapRegistrationAdapter implements DomainRegistrationLookup {

    private static final String IANA_BOOTSTRAP_URL =
        "https://data.iana.org/rdap/dns.json";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private volatile Map<String, String> rdapServers;

    public RdapRegistrationAdapter(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        this.objectMapper = objectMapper;
    }

    @Override
    public RegistrationSnapshot lookupRegistration(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }

        String normalizedDomain = normalizeDomain(domain);
        String tld = extractTld(normalizedDomain);

        String rdapBaseUrl = findRdapServer(tld);

        if (rdapBaseUrl == null) {
            throw new IllegalStateException(
                "No RDAP server found for TLD: " + tld
            );
        }

        URI uri = buildDomainUri(rdapBaseUrl, normalizedDomain);

        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(15))
            .header("Accept", "application/rdap+json, application/json")
            .GET()
            .build();

        try {
            HttpResponse<String> response =
                httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            if (response.statusCode() == 404) {
                return new RegistrationSnapshot(
                    false,
                    null,
                    null,
                    null
                );
            }

            if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

                throw new IllegalStateException(
                    "RDAP lookup failed for " +
                    normalizedDomain +
                    ": HTTP " +
                    response.statusCode()
                );
            }

            JsonNode root =
                objectMapper.readTree(response.body());

            return new RegistrationSnapshot(
                true,
                extractEventDate(root, "registration"),
                extractEventDate(root, "expiration"),
                extractRegistrar(root)
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                "RDAP lookup interrupted for " + normalizedDomain,
                e
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                "RDAP lookup failed for " + normalizedDomain,
                e
            );
        }
    }

    private String normalizeDomain(String domain) {
        String normalized = domain.trim().toLowerCase();

        if (normalized.endsWith(".")) {
            normalized =
                normalized.substring(0, normalized.length() - 1);
        }

        return IDN.toASCII(normalized);
    }

    private String extractTld(String domain) {
        int dot = domain.lastIndexOf('.');

        if (dot <= 0 || dot == domain.length() - 1) {
            throw new IllegalArgumentException(
                "Invalid domain: " + domain
            );
        }

        return domain.substring(dot + 1);
    }

    private URI buildDomainUri(
        String baseUrl,
        String domain
    ) {
        String normalizedBase =
            baseUrl.endsWith("/")
                ? baseUrl
                : baseUrl + "/";

        return URI.create(
            normalizedBase +
            "domain/" +
            domain
        );
    }

    private String findRdapServer(String tld) {
        Map<String, String> servers = rdapServers;

        if (servers == null) {
            synchronized (this) {
                servers = rdapServers;

                if (servers == null) {
                    servers = loadBootstrapRegistry();
                    rdapServers = servers;
                }
            }
        }

        return servers.get(tld);
    }

    private Map<String, String> loadBootstrapRegistry() {
        HttpRequest request = HttpRequest.newBuilder(
                URI.create(IANA_BOOTSTRAP_URL)
            )
            .timeout(Duration.ofSeconds(15))
            .header("Accept", "application/json")
            .GET()
            .build();

        try {
            HttpResponse<String> response =
                httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

            if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

                throw new IllegalStateException(
                    "Failed to load IANA RDAP bootstrap registry: HTTP " +
                    response.statusCode()
                );
            }

            JsonNode root =
                objectMapper.readTree(response.body());

            Map<String, String> result = new HashMap<>();

            JsonNode services = root.get("services");

            if (services == null || !services.isArray()) {
                throw new IllegalStateException(
                    "Invalid IANA RDAP bootstrap response"
                );
            }

            for (JsonNode service : services) {
                if (!service.isArray() || service.size() < 2) {
                    continue;
                }

                JsonNode tlds = service.get(0);
                JsonNode urls = service.get(1);

                if (!tlds.isArray() ||
                    !urls.isArray() ||
                    urls.isEmpty()) {
                    continue;
                }

                String baseUrl = urls.get(0).asText();

                for (JsonNode tldNode : tlds) {
                    String value =
                        tldNode.asText().toLowerCase();

                    result.put(value, baseUrl);
                }
            }

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                "Loading IANA RDAP bootstrap registry interrupted",
                e
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to load IANA RDAP bootstrap registry",
                e
            );
        }
    }

    private Instant extractEventDate(
        JsonNode root,
        String eventAction
    ) {
        JsonNode events = root.get("events");

        if (events == null || !events.isArray()) {
            return null;
        }

        for (JsonNode event : events) {
            if (!eventAction.equals(
                event.path("eventAction").asText()
            )) {
                continue;
            }

            String eventDate =
                event.path("eventDate").asText(null);

            if (eventDate == null || eventDate.isBlank()) {
                return null;
            }

            try {
                return Instant.parse(eventDate);
            } catch (Exception ignored) {
                return null;
            }
        }

        return null;
    }

    private String extractRegistrar(JsonNode root) {
        JsonNode entities = root.get("entities");

        if (entities == null || !entities.isArray()) {
            return null;
        }

        for (JsonNode entity : entities) {
            JsonNode roles = entity.get("roles");

            if (roles == null || !roles.isArray()) {
                continue;
            }

            boolean isRegistrar = false;

            for (JsonNode role : roles) {
                if ("registrar".equalsIgnoreCase(role.asText())) {
                    isRegistrar = true;
                    break;
                }
            }

            if (!isRegistrar) {
                continue;
            }

            String registrarName =
                extractVcardName(entity);

            if (registrarName != null &&
                !registrarName.isBlank()) {
                return registrarName;
            }
        }

        return null;
    }

    private String extractVcardName(JsonNode entity) {
        JsonNode vcardArray = entity.get("vcardArray");

        if (vcardArray == null ||
            !vcardArray.isArray() ||
            vcardArray.size() < 2) {
            return null;
        }

        JsonNode properties = vcardArray.get(1);

        if (!properties.isArray()) {
            return null;
        }

        for (JsonNode property : properties) {
            if (!property.isArray() ||
                property.size() < 4) {
                continue;
            }

            if (!"fn".equalsIgnoreCase(
                property.get(0).asText()
            )) {
                continue;
            }

            JsonNode value = property.get(3);

            if (value.isTextual()) {
                return value.asText();
            }
        }

        return null;
    }
}