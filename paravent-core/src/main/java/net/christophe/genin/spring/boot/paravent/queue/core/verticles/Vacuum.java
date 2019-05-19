package net.christophe.genin.spring.boot.paravent.queue.core.verticles;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueProperties;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueState;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.VacuumManager;
import net.christophe.genin.spring.boot.paravent.queue.core.util.Tasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Vacuum extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Vacuum.class);

    private static final String ENDPOINT = Vacuum.class.getName();

    private final ParaventQueueProperties properties;
    private final VacuumManager manager;
    private Long currentId;

    public Vacuum(ParaventQueueProperties properties, VacuumManager repository) {
        this.properties = properties;
        this.manager = repository;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public Completable rxStart() {
        final Long vaccuumTime = properties.getVacuum().getTime();
        final Tasks tasks = new Tasks(vertx);
        String endpoint = Tasks.generate(ENDPOINT);
        tasks.register(endpoint);

        vertx.eventBus().consumer(Tasks.nameStart(endpoint), msg -> {
            if (Objects.isNull(currentId)) {
                currentId = vertx.setPeriodic(vaccuumTime,
                        id ->
                                deleteSuccess()
                                        .flatMap(nb -> deleteOrphanDocuments().map(n -> n + nb))
                                        .flatMap(nb -> deleteOrphanMetadata().map(n -> n + nb))
                                        .subscribeOn(Schedulers.io())
                                        .subscribe(
                                                nbDeleted -> {
                                                    if (nbDeleted > 0) {
                                                        LOGGER.info("All rows - Deleted " + nbDeleted);
                                                    }
                                                },
                                                err -> LOGGER.error("error", err)
                                        )
                );
                tasks.start(endpoint);

            }
            msg.reply(true);
        });

        tasks.createTasksStop(endpoint, () -> currentId, (v) -> currentId = v);

        return vertx.eventBus().rxSend(Tasks.nameStart(endpoint), new JsonObject()).ignoreElement();
    }

    @Override
    public void stop() {
        if (Objects.nonNull(currentId) && vertx.cancelTimer(currentId)) {
            currentId = null;
            LOGGER.info("Task stop when stopping verticles " + currentId);
        }
    }

    private Single<Integer> deleteSuccess() {
        return Single.fromCallable(() -> manager.deleteAllSuccessQueueByState(QueueState.success))
                .subscribeOn(Schedulers.io())
                .doOnSuccess(
                        nbDeleted -> {
                            if (nbDeleted > 0) {
                                LOGGER.info("Queue - Deleted " + nbDeleted + " rows");
                            }
                        });

    }


    private Single<Integer> deleteOrphanDocuments() {
        return Single.fromCallable(manager::deleteOrphanDocument)
                .doOnSuccess(
                        nbDeleted -> {
                            if (nbDeleted > 0) {
                                LOGGER.info("Documents - Deleted " + nbDeleted + " rows");
                            }
                        });

    }


    private Single<Integer> deleteOrphanMetadata() {
        return Single.fromCallable(manager::deleteOrphanMetadata)
                .doOnSuccess(
                        nbDeleted -> {
                            if (nbDeleted > 0) {
                                LOGGER.info("Metadata - Deleted " + nbDeleted + " rows");
                            }
                        });

    }


}
