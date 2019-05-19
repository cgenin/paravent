package net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll;

import io.vertx.core.Verticle;
import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueProperties;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.PollManager;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Implementation.
 */
class PollBuilderImpl implements PollBuilder, PollBuilder.PollBuilderProperties,
        PollBuilder.PollBuilderManager {


    ParaventQueueProperties properties;
    PollManager pollManager;
    PollContract contract;

    @Override
    public Supplier<Verticle> build(PollContract contract) {
        final PollBuilderImpl copy = new PollBuilderImpl();
        copy.contract = contract;
        copy.properties = properties;
        copy.pollManager = pollManager;
        return () -> new PollVerticle(copy);
    }

    @Override
    public PollBuilderManager withProperties(ParaventQueueProperties properties) {
        Objects.requireNonNull(properties);
        this.properties = properties;
        return this;
    }

    @Override
    public PollBuilder withManager(PollManager pollManager) {
        Objects.requireNonNull(pollManager);
        this.pollManager = pollManager;
        return this;
    }


}
