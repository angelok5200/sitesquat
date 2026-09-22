package org.tafel.squating.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.tafel.squating.domain.value.MonitoringPolicy;

public class Brand {

    private final UUID id;
    private final String name;
    private final String primaryDomain;
    private final String referenceUrl;

    private final Set<String> monitoredIds;
    private final MonitoringPolicy monitoringPolicy;

    private Instant createdAt;

    public Brand(
            Instant createdAt,
            UUID id,
            Set<String> monitoredIds,
            MonitoringPolicy monitoringPolicy,
            String name,
            String primaryDomain,
            String referenceUrl
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException(
                    "name cannot be null or empty"
            );
        }

        if (monitoringPolicy == null) {
            throw new IllegalArgumentException(
                    "monitoringPolicy cannot be null"
            );
        }

        if (primaryDomain == null || primaryDomain.isBlank()) {
            throw new IllegalArgumentException(
                    "primaryDomain cannot be null or empty"
            );
        }

        this.createdAt =
                createdAt != null ? createdAt : Instant.now();

        this.id = id;
        this.monitoredIds =
                monitoredIds != null
                        ? Set.copyOf(monitoredIds)
                        : Set.of();

        this.monitoringPolicy = monitoringPolicy;
        this.name = name;
        this.primaryDomain = primaryDomain;
        this.referenceUrl = referenceUrl;
    }

    public String getName() {
        return name;
    }

    public String getReferenceUrl() {
        return referenceUrl;
    }

    public UUID getId() {
        return id;
    }

    public String getPrimaryDomain() {
        return primaryDomain;
    }

    public Set<String> getMonitoredIds() {
        return monitoredIds;
    }

    public MonitoringPolicy getMonitoringPolicy() {
        return monitoringPolicy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}