package org.tafel.squating.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.tafel.squating.domain.value.MonitoringPolicy;

public class Brand {
    private UUID id;
    private String name;
    private String primaryDomain;
    private String referenceUrl;

    private Set<String> monitoredIds;
    private MonitoringPolicy monitoringPolicy;

    private Instant createdAt;

    public Brand(Instant createdAt, UUID id, Set<String> monitoredIds, MonitoringPolicy monitoringPolicy, String name, String primaryDomain, String referenceUrl) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        this.createdAt = createdAt;
        this.id = id;
        this.monitoredIds = monitoredIds;
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
