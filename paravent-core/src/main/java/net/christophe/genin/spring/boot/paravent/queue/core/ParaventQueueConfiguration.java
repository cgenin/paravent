package net.christophe.genin.spring.boot.paravent.queue.core;

import io.reactivex.Observable;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.reactivex.core.Vertx;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.PollManager;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.VacuumManager;
import net.christophe.genin.spring.boot.paravent.queue.core.util.DeploymentOpts;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.Vacuum;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.VerticleFactory;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll.PollBuilder;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll.PollContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Configuration of the queue beans.
 */
@Configuration
@ComponentScan
public class ParaventQueueConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParaventQueueConfiguration.class);


    @Autowired
    private ParaventQueueProperties properties;

    @Autowired
    private PollManager pollManager;


    @Autowired
    private VacuumManager vacuumManager;

    @Autowired
    private List<VerticleFactory> verticles;

    @Autowired(required = false)
    private List<PollContract> contracts = Collections.emptyList();


    /**
     * Launcher of vertx verticles
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @PostConstruct
    public void launchVerticles() {
        Vertx vertx = vertx();
        PollBuilder pollBuilder = pollBuilder();
        Supplier<Verticle> supplierVaccuum = () -> new Vacuum(properties, vacuumManager);
        Observable.concat(
                Observable.fromIterable(verticles)
                        .flatMap(v ->
                                vertx.rxDeployVerticle(v, DeploymentOpts.base(properties, v.getClass()))
                                        .doOnSuccess(id -> LOGGER.info("launch successfuly : '" + v.getClass().getCanonicalName() + "' with id : " + id))
                                        .toObservable()),
                Observable.fromIterable(contracts)
                        .flatMap(
                                contract -> vertx.rxDeployVerticle(pollBuilder.build(contract), DeploymentOpts.poll(properties, contract.getKey()))
                                        .doOnSuccess(id -> LOGGER.info("launch successfuly : '" + contract.getKey() + "' with id : " + id))
                                        .toObservable()
                        ),
                Observable.just(supplierVaccuum)
                        .flatMap(
                                v -> vertx.rxDeployVerticle(v, new DeploymentOptions())
                                        .doOnSuccess(id -> LOGGER.info("launch successfuly : '" + Vacuum.class.getName() + "' with id : " + id))
                                        .toObservable()
                        )
        )
                .reduce(0, (acc, n) -> ++acc)
                .subscribe(
                        nb -> LOGGER.info("Launch " + nb + " vertcile(s)."),
                        err -> LOGGER.error("Error in launching verticle ", err)
                );

    }


    @Bean
    public Vertx vertx() {
        return Vertx.vertx();
    }

    @Bean
    public PollBuilder pollBuilder() {
        return PollBuilder.builder()
                .withProperties(properties)
                .withManager(pollManager);
    }

}
