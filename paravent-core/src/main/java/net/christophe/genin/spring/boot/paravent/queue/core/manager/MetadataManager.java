package net.christophe.genin.spring.boot.paravent.queue.core.manager;

import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vertx.core.json.JsonObject;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.MetadataDb;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.Tables;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class MetadataManager {

    private static final RecordMapper<Record, MetadataDb> RECORD_2_METADATA = record -> {
        final MetadataDb metadataDb = new MetadataDb();
        metadataDb.setId(record.get(Tables.METADATAS.UUID));
        metadataDb.setPayload(record.get(Tables.METADATAS.PAYLOAD));
        return metadataDb;
    };

    private final DSLContext dslContext;

    @Autowired
    public MetadataManager(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Transactional(readOnly = true)
    public List<QueueDb> peek(String key, Integer limit) {
        return QueueManager.peek(dslContext, key, QueueType.metadata, limit);
    }

    @Transactional(readOnly = true)
    public Option<MetadataDb> findById(UUID uuid) {
        return Try.of(
                () -> dslContext.select(Tables.METADATAS.asterisk())
                        .from(Tables.METADATAS)
                        .where(Tables.METADATAS.UUID.eq(uuid))
                        .fetchOne(RECORD_2_METADATA)
        ).map(Option::of)
                .getOrElseGet(err -> Option.none());
    }

    @Transactional
    public QueueDb save(QueueDb queueDb, JsonObject payload) {
        final int inserted = Managers.saveInQueue(dslContext, queueDb)
                + Managers.saveInMetadata(dslContext, queueDb.getUuid(), payload);

        if (inserted != 2) {
            throw new IllegalStateException("Impossible of inserting " + queueDb);
        }
        return queueDb;
    }

}
