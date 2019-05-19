package net.christophe.genin.spring.boot.paravent.queue.core.entities;


import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueState;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

public class QueueDb {

    private String key;

    private UUID uuid;

    private Timestamp collectedAt;

    private QueueType type;

    private QueueState state;

    private Timestamp start;

    private Timestamp stop;


    public QueueDb() {
    }

    public QueueDb(String key,  long collectedAt, QueueType type) {
        this.key = key;
        this.uuid = UUID.randomUUID();
        this.collectedAt = new Timestamp(collectedAt);
        this.type = type;
        this.setState(QueueState.created);
    }

    public QueueState getState() {
        return state;
    }

    public void setState(QueueState state) {
        this.state = state;
    }

    public Timestamp getStart() {
        return start;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public Timestamp getStop() {
        return stop;
    }

    public void setStop(Timestamp stop) {
        this.stop = stop;
    }

    public QueueType getType() {
        return type;
    }

    public void setType(QueueType type) {
        this.type = type;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Timestamp getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(Timestamp collectedAt) {
        this.collectedAt = collectedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueDb queueDb = (QueueDb) o;
        return Objects.equals(key, queueDb.key) &&
                Objects.equals(uuid, queueDb.uuid) &&
                Objects.equals(collectedAt, queueDb.collectedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, uuid, collectedAt);
    }

    @Override
    public String toString() {
        return "QueueDb{" +
                "key='" + key + '\'' +
                ", uuid=" + uuid +
                ", collectedAt=" + collectedAt +
                ", type=" + type +
                ", state=" + state +
                ", start=" + start +
                ", stop=" + stop +
                '}';
    }
}
