package net.christophe.genin.spring.boot.paravent.queue.core.manager;

import io.vavr.control.Option;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueState;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.Tables;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GraphqlManager {

    private final DSLContext dslContext;

    @Autowired
    public GraphqlManager(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    private Option<Condition> queueFilter(Map<String, Object> filter) {
        final String queueType = (String) filter.get("queueType");
        final String queueState = (String) filter.get("queueState");

        return io.vavr.collection.List.of(
                Option.of(queueType)
                        .filter(qt -> !"all".equals(qt))
                        .map(QueueType::valueOf)
                        .flatMap(qt -> {
                            switch (qt) {
                                case metadata:
                                case document:
                                    return Option.of(Tables.QUEUE.TYPE.eq(qt));
                                default:
                                    return Option.none();
                            }
                        }),
                Option.of(queueState)
                        .filter(qt -> !"all".equals(qt))
                        .map(QueueState::valueOf)
                        .flatMap(qs -> {
                            switch (qs) {
                                case success:
                                case error:
                                case created:
                                case running:
                                    return Option.of(Tables.QUEUE.STATE.eq(qs));
                                default:
                                    return Option.none();
                            }
                        }))
                .reduce((opt1, opt2) -> {
                    if (opt1.isEmpty())
                        return opt2;

                    return opt1.map(cond ->
                            opt2.map(cond::and)
                                    .getOrElse(() -> cond)
                    );
                });


    }

    public Long countQueue(Map<String, Object> filter) {
        final SelectJoinStep<Record1<Integer>> select = dslContext.select(DSL.count())
                .from(Tables.QUEUE);

        return queueFilter(filter)
                .map(c -> (SelectOrderByStep<Record1<Integer>>) select.where(c))
                .getOrElse(() -> select)
                .fetchOne(0, Long.class);
    }

    public List<QueueDb> getQueue(Map<String, Object> filter, Integer skip, Integer first) {
        final SelectJoinStep<Record> select = dslContext.select(Tables.QUEUE.asterisk())
                .from(Tables.QUEUE);

        return queueFilter(filter)
                .map(c -> (SelectOrderByStep<Record>) select.where(c))
                .getOrElse(() -> select)
                .orderBy(Tables.QUEUE.COLLECTED_AT.asc())
                .limit(first)
                .offset(skip)
                .fetch(Managers.RECORD_2_QUEUE);
    }

    public Long countMetadata() {
        return dslContext.select(DSL.count())
                .from(Tables.QUEUE)
                .where(Tables.QUEUE.TYPE.eq(QueueType.metadata))
                .fetchOne(0, Long.class);
    }


    public Long countDocument() {
        return dslContext.select(DSL.count())
                .from(Tables.QUEUE)
                .where(Tables.QUEUE.TYPE.eq(QueueType.document))
                .fetchOne(0, Long.class);
    }

    public QueueDb findQueueBy(UUID uuid) {
        return dslContext.select(Tables.QUEUE.asterisk())
                .from(Tables.QUEUE)
                .where(Tables.QUEUE.UUID.eq(uuid))
                .fetchOne(Managers.RECORD_2_QUEUE);
    }

    public List<Map<String, Object>> getMetadata(Integer skip, Integer first) {
        return dslContext.select(Tables.METADATAS.UUID, Tables.METADATAS.PAYLOAD)
                .from(Tables.METADATAS.innerJoin(Tables.QUEUE).on(Tables.QUEUE.UUID.eq(Tables.METADATAS.UUID)))
                .limit(first)
                .offset(skip)
                .fetch(record -> {
                    final HashMap<String, Object> result = new HashMap<>();
                    result.put(record.field1().getName(), record.value1());
                    result.put(record.field2().getName(), record.value2());
                    return result;
                });
    }

    public List<Map<String, Object>> getDocument(Integer skip, Integer first) {
        return dslContext.select(Tables.DOCUMENTS.UUID, Tables.DOCUMENTS.NAME, Tables.DOCUMENTS.EXTENSION)
                .from(Tables.DOCUMENTS.innerJoin(Tables.QUEUE).on(Tables.QUEUE.UUID.eq(Tables.DOCUMENTS.UUID)))
                .limit(first)
                .offset(skip)
                .fetch(record -> {
                    final HashMap<String, Object> result = new HashMap<>();
                    result.put("uuid", record.value1());
                    result.put("filename", record.value2());
                    result.put("extension", record.value3());
                    return result;
                });
    }

    public String getDocumentMetadata(UUID uuid) {
        return dslContext.select(Tables.METADATAS.PAYLOAD)
                .from(Tables.METADATAS.innerJoin(Tables.DOCUMENTS).on(Tables.DOCUMENTS.METADATA.eq(Tables.METADATAS.UUID)))
                .where(Tables.DOCUMENTS.UUID.eq(uuid))
                .fetchOne(Record1::value1);
    }

    public byte[] getDocumentBin(UUID uuid) {
        return dslContext.select(Tables.DOCUMENTS.BIN)
                .from(Tables.DOCUMENTS)
                .where(Tables.DOCUMENTS.UUID.eq(uuid))
                .fetchOne(Record1::value1);
    }


}
