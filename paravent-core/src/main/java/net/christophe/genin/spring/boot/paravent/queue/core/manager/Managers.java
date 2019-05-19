package net.christophe.genin.spring.boot.paravent.queue.core.manager;

import io.vertx.core.json.JsonObject;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.DocumentDb;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;

import java.util.UUID;

public final class Managers {


    public static final RecordMapper<Record, QueueDb> RECORD_2_QUEUE = record -> {
        final QueueDb queueDb = new QueueDb();
        queueDb.setKey(record.get(Tables.QUEUE.ID));
        queueDb.setUuid(record.get(Tables.QUEUE.UUID));
        queueDb.setCollectedAt(record.get(Tables.QUEUE.COLLECTED_AT));
        queueDb.setType(record.get(Tables.QUEUE.TYPE));
        queueDb.setState(record.get(Tables.QUEUE.STATE));
        queueDb.setStart(record.get(Tables.QUEUE.START));
        queueDb.setStop(record.get(Tables.QUEUE.STOP));
        return queueDb;
    };

    static final RecordMapper<Record, DocumentDb> RECORD_2_DOCUMENT = record -> {
        final DocumentDb documentDb = new DocumentDb();
        documentDb.setId(record.get(Tables.DOCUMENTS.UUID));
        documentDb.setBytes(record.get(Tables.DOCUMENTS.BIN));
        documentDb.setExtension(record.get(Tables.DOCUMENTS.EXTENSION));
        documentDb.setMetadataId(record.get(Tables.DOCUMENTS.METADATA));
        documentDb.setName(record.get(Tables.DOCUMENTS.NAME));
        return documentDb;
    };


     static Integer saveInQueue(DSLContext ctxt, QueueDb queueDb) {
        return ctxt.insertInto(Tables.QUEUE)
                .columns(Tables.QUEUE.ID, Tables.QUEUE.COLLECTED_AT,
                        Tables.QUEUE.UUID, Tables.QUEUE.STATE,
                        Tables.QUEUE.TYPE)
                .values(queueDb.getKey(), queueDb.getCollectedAt(),
                        queueDb.getUuid(), queueDb.getState(), queueDb.getType())
                .execute();


    }

     static int saveInMetadata(DSLContext dslContext, UUID uuid, JsonObject payload) {
        return dslContext.insertInto(Tables.METADATAS, Tables.METADATAS.UUID, Tables.METADATAS.PAYLOAD)
                .values(uuid, payload.encode())
                .execute();
    }
}
