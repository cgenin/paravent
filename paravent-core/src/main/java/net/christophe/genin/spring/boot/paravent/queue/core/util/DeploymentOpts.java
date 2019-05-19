package net.christophe.genin.spring.boot.paravent.queue.core.util;

import io.vavr.control.Option;
import io.vertx.core.DeploymentOptions;
import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueProperties;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.VerticleFactory;

public final class DeploymentOpts {

    public static DeploymentOptions poll(ParaventQueueProperties properties, String key) {
        final int instance = Option.of(properties.getInstances().getPoll().get(key))
                .orElse(() -> {
                    switch (properties.getInstances().getForPoll()) {
                        case availableProcessor:
                            return Option.of(Runtime.getRuntime().availableProcessors() + 1);
                        case fixed:
                            return Option.of(properties.getInstances().getNbPollDefault());
                        default:
                            throw new IllegalArgumentException("unknown 'paravent.queue.instances.forPoll' : " + properties.getInstances().getForPoll());

                    }
                }).getOrElseThrow(() -> new IllegalArgumentException("In fixed mode, the property paravent.queue.instances.nbPollDefault must be set"));
        return new DeploymentOptions().setInstances(instance);
    }


    public static DeploymentOptions base(ParaventQueueProperties properties, Class<? extends VerticleFactory> clazz) {
        final DeploymentOptions deploymentOptions = new DeploymentOptions();

        final ParaventQueueProperties.Instances instances = properties.getInstances();
        final Integer nb = instances.getByVerticle().getOrDefault(clazz.getSimpleName(), instances.getNbDefault());
        deploymentOptions.setInstances(nb);


        return deploymentOptions;
    }
}
