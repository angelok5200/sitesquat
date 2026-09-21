package org.tafel.squating.domain.model;
import java.time.Instant;
import java.util.UUID;

import org.tafel.squating.domain.enums.EvidenceType;

public class Evidence {
    private final UUID id;
    //private final EvidenceType evidenceType;
    private final UUID observationid;

    private final String location;
    private final String hash;

    private final Instant createdAt;

    public Evidence(EvidenceType evidenceType, Instant createdAt, String hash, UUID id, String location, UUID observationid) {
        //if (evidenceType == null) {
       //     throw new IllegalArgumentException("type cannot be null");
        //}
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("location cannot be null or empty");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (observationid == null) {
            throw new IllegalArgumentException("observationid cannot be null");
        }
        //this.evidenceType = evidenceType;
        this.createdAt = createdAt;
        this.hash = hash;
        this.id = id != null ? id : UUID.randomUUID();
        this.location = location;
        this.observationid = observationid;
    }

    public UUID getId() {
        return id;
    }

    //public EvidenceType getEvidenceType() {
   //     return evidenceType;
    //}

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