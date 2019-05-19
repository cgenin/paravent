package net.christophe.genin.spring.boot.paravent.queue.core.verticles;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueProperties;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.MetadataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MetadatasFactory implements VerticleFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadatasFactory.class);

    public static final String INSERT = MetadatasFactory.class.getName() + ".insert";
    public static final String GET_BY_KEY = MetadatasFactory.class.getName() + ".get.by.key";


    private final ParaventQueueProperties paraventQueueProperties;

    private final MetadataManager metadataManager;

    @Autowired
    public MetadatasFactory(ParaventQueueProperties paraventQueueProperties, MetadataManager metadataManager) {
        this.paraventQueueProperties = paraventQueueProperties;
        this.metadataManager = metadataManager;
    }


    @Override
    public Verticle get() {
        return new Metadatas(this);
    }


    public static class Metadatas extends AbstractVerticle {
        private final MetadatasFactory metadatasFactory;


        private Metadatas(MetadatasFactory metadatasFactory) {
            this.metadatasFactory = metadatasFactory;
        }


        @Override
        public void start() {
            vertx.eventBus().consumer(INSERT, insert());
            vertx.eventBus().consumer(GET_BY_KEY, getByKey());
        }


        @SuppressWarnings("ResultOfMethodCallIgnored")
        private Handler<Message<JsonObject>> getByKey() {
            return msg -> {
                final JsonObject body = msg.body();
                Single.fromCallable(
                        () -> Optional.ofNullable(body.getString("key"))
                                .orElseThrow(() -> new IllegalArgumentException("key must be defined :" + body.encode()))
                )
                        .subscribeOn(Schedulers.io())
                        .map(key -> {
                            final Integer limit = body.getInteger("limit", metadatasFactory.paraventQueueProperties.getPoll().getDefaultBatch());
                            return metadatasFactory.metadataManager.peek(key, limit);
                        })
                        .map(page -> page.stream()
                                .map(JsonObject::mapFrom)
                                .collect(Collectors.toList()
                                )
                        )
                        .map(JsonArray::new)
                        .subscribe(
                                msg::reply,
                                err -> {
                                    LOGGER.error("error in getting " + body.encode(), err);
                                    msg.fail(500, "error");
                                }
                        );
            };
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private Handler<Message<JsonObject>> insert() {
            return msg -> {
                final JsonObject body = msg.body();
                Single.just(body.getString("key"))
                        .subscribeOn(Schedulers.io())
                        .map(key -> {
                            final JsonObject payload = Optional.ofNullable(body.getJsonObject("payload"))
                                    .orElseThrow(() -> new IllegalArgumentException("payload null for " + key));
                            final QueueDb queueDb = new QueueDb(key, System.currentTimeMillis(), QueueType.metadata);
                            return metadatasFactory.metadataManager.save(queueDb, payload);
                        })
                        .subscribe(
                                rs -> {
                                    LOGGER.debug("insert ok for " + body.encode());
                                    msg.reply(rs.getUuid().toString());
                                },
                                err -> {
                                    LOGGER.error(" Error in inserting '" + body.encode() + "'", err);
                                    msg.fail(500, "Error");
                                }
                        );
            };
        }
    }
}
