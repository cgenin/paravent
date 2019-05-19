package net.christophe.genin.spring.boot.paravent.queue.core.manager;

import io.vavr.control.Option;
import io.vavr.control.Try;
import io.vertx.core.json.JsonObject;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.DocumentDb;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.Tables;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentManager {

    private static final Pattern PATTERN_EXTENSION = Pattern.compile("\\.([a-zA-Z0-9]+)$");

    private final DSLContext dslContext;

    @Autowired
    public DocumentManager(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Transactional(readOnly = true)
    public Option<DocumentDb> findById(UUID uuid) {
        return Try.of(
                () -> dslContext.select(Tables.DOCUMENTS.asterisk())
                        .from(Tables.DOCUMENTS)
                        .where(Tables.DOCUMENTS.UUID.eq(uuid))
                        .fetchOne(Managers.RECORD_2_DOCUMENT)
        ).map(Option::of)
                .getOrElseGet(err -> Option.none());
    }

    @Transactional
    public QueueDb save(QueueDb queueDb, Option<String> filename, Option<JsonObject> metadata, byte[] document) {
        int insertedQueue = Managers.saveInQueue(dslContext, queueDb);
        final UUID uuidMetadata = metadata
                .map(m -> {
                    final UUID uuid = UUID.randomUUID();
                    Managers.saveInMetadata(dslContext, uuid, m);
                    return uuid;
                }).getOrElse(() -> null);
        final String name = filename.getOrElse(() -> null);
        final String extension = filename
                .map(f -> PATTERN_EXTENSION.matcher(f))
                .filter(Matcher::find)
                .map(r -> r.group(1))
                .getOrElse(() -> null);

        int inserted = insertedQueue
                + dslContext.insertInto(Tables.DOCUMENTS, Tables.DOCUMENTS.UUID, Tables.DOCUMENTS.NAME, Tables.DOCUMENTS.EXTENSION, Tables.DOCUMENTS.BIN, Tables.DOCUMENTS.METADATA)
                .values(queueDb.getUuid(), name, extension, document, uuidMetadata)
                .execute();

        if (inserted != 2) {
            throw new IllegalStateException("Impossible of inserting " + queueDb);
        }

        return queueDb;
    }
}
