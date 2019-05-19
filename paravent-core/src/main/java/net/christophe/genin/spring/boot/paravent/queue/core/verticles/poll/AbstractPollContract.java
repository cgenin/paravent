package net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll;

import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;

import java.util.Objects;

/**
 * First implement of PollContract.
 */
public abstract class AbstractPollContract implements PollContract {

    private final String key;
    private final QueueType typeOfQueue;

    /**
     * COnstructor.
     *
     * @param key         the id.
     * @param typeOfQueue the type of data
     */
    protected AbstractPollContract(String key, QueueType typeOfQueue) {
        this.key = key;
        this.typeOfQueue = typeOfQueue;
    }

    @Override
    public String getKey() {
        return key;
    }

    /**
     * Minimal validation.
     *
     * @param queueDb the event to validate.
     */
    void validate(QueueDb queueDb) {
        Objects.requireNonNull(queueDb);
        if (!typeOfQueue.equals(queueDb.getType())) {
            throw new IllegalArgumentException("incorrect type for " + queueDb.getUuid());
        }
    }
}
