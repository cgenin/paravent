package net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll;

import io.vavr.control.Option;
import io.vertx.core.json.JsonObject;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.MetadataDb;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.MetadataManager;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class PollMetadataContract extends AbstractPollContract {

    @Autowired
    private MetadataManager repository;

    public PollMetadataContract(String key) {
        super(key, QueueType.metadata);
    }


    @Override
    public boolean run(QueueDb queueDb) throws Exception {
        validate(queueDb);
        final Option<JsonObject> optMetadata = repository.findById(queueDb.getUuid())
                .map(MetadataDb::getPayload)
                .map(JsonObject::new);
        return execute(queueDb, optMetadata);
    }


    protected abstract boolean execute(QueueDb queueDb, Option<JsonObject> optPayload) throws Exception;
}
