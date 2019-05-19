package net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll;

import io.vertx.core.Verticle;
import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueProperties;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.PollManager;

import java.util.function.Supplier;

/**
 * Builder for creating an dequeuing task.
 */
public interface PollBuilder {

    /**
     * create an instance.
     *
     * @return the builder
     */
    static PollBuilderProperties builder() {
        return new PollBuilderImpl();
    }

    /**
     * create an verticle for the specific contract to peek this event.
     *
     * @param contract the contarct.
     * @return the verticle.
     */
    Supplier<Verticle> build(PollContract contract);

    /**
     * The properties to set.
     */
    interface PollBuilderProperties {

        PollBuilderManager withProperties(ParaventQueueProperties properties);
    }

    /**
     * the manager which peek the datas.
     */
    interface PollBuilderManager {

        PollBuilder withManager(PollManager pollManager);
    }
}
