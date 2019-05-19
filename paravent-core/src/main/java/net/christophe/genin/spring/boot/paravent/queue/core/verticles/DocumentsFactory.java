package net.christophe.genin.spring.boot.paravent.queue.core.verticles;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vavr.control.Option;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.DocumentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DocumentsFactory implements VerticleFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentsFactory.class);

    public static final String INSERT = DocumentsFactory.class.getName() + ".insert";

    private final DocumentManager documentManager;

    @Autowired
    public DocumentsFactory(DocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    @Override
    public Verticle get() {
        return new Metadatas(this);
    }


    public static class Metadatas extends AbstractVerticle {
        private final DocumentsFactory documentsFactory;


        private Metadatas(DocumentsFactory documentsFactory) {
            this.documentsFactory = documentsFactory;
        }


        @Override
        public void start() {
            vertx.eventBus().consumer(INSERT, insert());
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private Handler<Message<JsonObject>> insert() {
            return msg -> {
                final JsonObject body = msg.body();
                Single.just(body.getString("key"))
                        .subscribeOn(Schedulers.io())
                        .map(key -> {
                            final QueueDb queueDb = new QueueDb(key, System.currentTimeMillis(), QueueType.document);
                            final Option<String> filename = Option.of(body.getString("filename"));
                            final Option<JsonObject> metadata = Option.of(body.getJsonObject("metadata"));
                            final byte[] buffer = Option.of(body.getBinary("document")).getOrElseThrow(() -> new IllegalStateException("document must be present " + key));
                            return documentsFactory.documentManager.save(queueDb, filename, metadata, buffer);
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
