package org.tafel.squating.adapters.registration;

import java.io.IOException;
import java.net.IDN;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.tafel.squating.domain.value.RegistrationSnapshot;
import org.tafel.squating.ports.outbound.DomainRegistrationLookup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RdapRegistrationAdapter implements DomainRegistrationLookup {

    private static final URI RDAP_BOOTSTRAP_URI =
            URI.create("https://data.iana.org/rdap/dns.json");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private volatile Map<String, String> rdapServers;

    public RdapRegistrationAdapter(
            HttpClient httpClient,
            ObjectMapper objectMapper
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public RegistrationSnapshot lookupRegistration(String domain) {
        if (domain == null || domain.isBlank()) {
            throw new IllegalArgumentException("domain must not be blank");
        }

        String normalizedDomain = normalizeDomain(domain);
        String tld = extractTld(normalizedDomain);

        Map<String, String> servers = getRdapServers();

        String rdapServer = servers.get(tld);

        if (rdapServer == null) {
            return new RegistrationSnapshot(
                    false,
                    null,
                    null,
                    null
            );
        }

        URI lookupUri = URI.create(
                ensureTrailingSlash(rdapServer)
                        + "domain/"
                        + normalizedDomain
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(lookupUri)
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

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "RDAP lookup failed for "
                                + normalizedDomain
                                + ": HTTP "
                                + response.statusCode()
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
                    "RDAP lookup interrupted for "
                            + normalizedDomain,
                    e
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "RDAP lookup failed for "
                            + normalizedDomain,
                    e
            );
        }
    }

    private Map<String, String> getRdapServers() {
        Map<String, String> servers = rdapServers;

        if (servers != null) {
            return servers;
        }

        synchronized (this) {
            servers = rdapServers;

            if (servers == null) {
                servers = loadBootstrapServers();
                rdapServers = servers;
            }

            return servers;
        }
    }

    private Map<String, String> loadBootstrapServers() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(RDAP_BOOTSTRAP_URI)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Unable to load IANA RDAP bootstrap registry: HTTP "
                                + response.statusCode()
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
                JsonNode tlds = service.get(0);
                JsonNode urls = service.get(1);

                if (tlds == null
                        || !tlds.isArray()
                        || urls == null
                        || !urls.isArray()
                        || urls.isEmpty()) {
                    continue;
                }

                String server = urls.get(0).asText(null);

                if (server == null || server.isBlank()) {
                    continue;
                }

                for (JsonNode tldNode : tlds) {
                    String tld = tldNode.asText(null);

                    if (tld != null && !tld.isBlank()) {
                        result.put(
                                normalizeTld(tld),
                                server
                        );
                    }
                }
            }

            return Map.copyOf(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Loading IANA RDAP bootstrap registry interrupted",
                    e
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to load IANA RDAP bootstrap registry",
                    e
            );
        }
    }

    private String normalizeDomain(String domain) {
        String normalized = domain.trim().toLowerCase();

        if (normalized.endsWith(".")) {
            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
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

        return normalizeTld(
                domain.substring(dot + 1)
        );
    }

    private String normalizeTld(String tld) {
        String normalized = tld
                .trim()
                .toLowerCase();

        while (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }

    private String ensureTrailingSlash(String url) {
        return url.endsWith("/")
                ? url
                : url + "/";
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
            if (!eventAction.equalsIgnoreCase(
                    event.path("eventAction").asText()
            )) {
                continue;
            }

            String eventDate =
                    event.path("eventDate").asText(null);

            if (eventDate == null || eventDate.isBlank()) {
                continue;
            }

            try {
                return Instant.parse(eventDate);
            } catch (java.time.format.DateTimeParseException ignored) {
                // Invalid event date should not invalidate
                // the complete registration response.
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
            if (!hasRole(entity, "registrar")) {
                continue;
            }

            JsonNode vcardArray =
                    entity.get("vcardArray");

            if (vcardArray == null
                    || !vcardArray.isArray()
                    || vcardArray.size() < 2) {
                continue;
            }

            JsonNode properties =
                    vcardArray.get(1);

            if (!properties.isArray()) {
                continue;
            }

            for (JsonNode property : properties) {
                if (!property.isArray()
                        || property.size() < 4) {
                    continue;
                }

                if ("fn".equalsIgnoreCase(
                        property.get(0).asText()
                )) {
                    return property.get(3).asText(null);
                }
            }
        }

        return null;
    }

    private boolean hasRole(
            JsonNode entity,
            String expectedRole
    ) {
        JsonNode roles = entity.get("roles");

        if (roles == null || !roles.isArray()) {
            return false;
        }

        Iterator<JsonNode> iterator =
                roles.elements();

        while (iterator.hasNext()) {
            if (expectedRole.equalsIgnoreCase(
                    iterator.next().asText()
            )) {
                return true;
            }
        }

        return false;
    }
}