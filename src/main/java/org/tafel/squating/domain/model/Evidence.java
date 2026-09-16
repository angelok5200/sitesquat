package org.tafel.squating.domain.model;
import java.time.Instant;
import java.util.UUID;

import org.tafel.squating.domain.enums.EvidenceType;

public class Evidence {
    private UUID id;
    private EvidenceType type;
    private UUID observationid;

    private String location;
    private String hash;

    private Instant createdAt;

    public Evidence(EvidenceType Type, Instant createdAt, String hash, UUID id, String location, UUID observationid) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("location cannot be null or empty");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        this.type = type;
        this.createdAt = createdAt;
        this.hash = hash;
        this.id = id;
        this.location = location;
        this.observationid = observationid;
    }

    public UUID getId() {
        return id;
    }

    public EvidenceType getType() {
        return type;
    }

    public UUID getObservationid() {
        return observationid;
    }

    public String getLocation() {
        return location;
    }

    public String getHash() {
        return hash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}