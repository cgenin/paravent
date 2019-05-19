package net.christophe.genin.spring.boot.paravent.queue.core.verticles;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.core.AbstractVerticle;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.QueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Component
public class QueuesFactory implements VerticleFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadatasFactory.class);

    public static final String ADD_ALL = MetadatasFactory.class.getName() + ".add.all";
    private final QueueManager queueManager;

    @Autowired
    public QueuesFactory(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public Verticle get() {
        return new QueueVerticle(this);
    }

    public static class QueueVerticle extends AbstractVerticle {

        private final QueuesFactory queuesFactory;

        private QueueVerticle(QueuesFactory queuesFactory) {
            this.queuesFactory = queuesFactory;
        }

        @Override
        public Completable rxStart() {
            return Single.fromCallable(initialize())
                    .doOnSuccess((res) -> LOGGER.debug("events registered : " + res))
                    .ignoreElement();
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private Callable<Boolean> initialize() {
            return () -> {
                vertx.eventBus().<JsonArray>consumer(ADD_ALL, msg -> {
                    final JsonArray ids = msg.body();
                    Single.just(ids)
                            .subscribeOn(Schedulers.io())
                            .map(arr -> arr.stream()
                                    .map(Object::toString)
                                    .map(UUID::fromString)
                                    .collect(Collectors.toList())
                            )
                            .map(queuesFactory.queueManager::addToQueue)
                            .subscribe((nb) -> {
                                        LOGGER.info("Add to queue " + ids.encode() + " : " + nb);
                                        msg.reply(nb);
                                    },
                                    err -> {
                                        LOGGER.error("Error in updating " + ids.encode(), err);
                                        msg.fail(500, "Error");
                                    });
                });
                return true;
            };
        }
    }
}
