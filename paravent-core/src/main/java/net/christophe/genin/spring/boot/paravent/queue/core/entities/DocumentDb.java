package net.christophe.genin.spring.boot.paravent.queue.core.entities;

import java.util.Objects;
import java.util.UUID;

public class DocumentDb {

    private UUID id;

    private String name;

    private String extension;

    private UUID metadataId;

    private byte[] bytes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentDb that = (DocumentDb) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public UUID getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(UUID metadataId) {
        this.metadataId = metadataId;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
