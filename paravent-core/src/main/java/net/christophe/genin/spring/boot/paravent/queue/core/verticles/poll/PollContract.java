package net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll;

import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;

/**
 * Minimal COntract for the consumers.
 */
public interface PollContract {

    /**
     * The key which must be peek.
     *
     * @return the name.
     */
    String getKey();

    /**
     * The runner method.
     * if an exception occured or the result of the method is false, then
     * the event will be in error.
     *
     * @param queueDb the result
     * @return true if succeeded. false otherwise.
     * @throws Exception checked exception
     */
    boolean run(QueueDb queueDb) throws Exception;
}
