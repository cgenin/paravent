package net.christophe.genin.spring.boot.paravent.queue.core.verticles;

import io.vertx.core.Verticle;

import java.util.function.Supplier;

/**
 * Base class to mount verticles.
 */
public interface VerticleFactory extends Supplier<Verticle> {
}
