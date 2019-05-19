package net.christophe.genin.spring.boot.paravent.queue.core.entities;

import java.util.Objects;
import java.util.UUID;

public class MetadataDb {

    private UUID id;

    private String payload;

    public MetadataDb() {
    }

    public MetadataDb(UUID id, String payload) {
        this.id = id;
        this.payload = payload;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataDb that = (MetadataDb) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
