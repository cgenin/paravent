package net.christophe.genin.spring.boot.paravent.queue.core.manager;

import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueProperties;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueState;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.Tables;
import org.jooq.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.UUID;

import static org.jooq.impl.DSL.row;

@Service
public class VacuumManager {

    private final DSLContext dslContext;
    private final ParaventQueueProperties properties;

    @Autowired
    public VacuumManager(DSLContext dslContext, ParaventQueueProperties properties) {
        this.dslContext = dslContext;
        this.properties = properties;
    }


    @Transactional
    public Integer deleteAllSuccessQueueByState(QueueState state) {
        final Result<Record3<String, UUID, Timestamp>> fetch = dslContext
                .select(Tables.QUEUE.ID, Tables.QUEUE.UUID, Tables.QUEUE.COLLECTED_AT)
                .from(Tables.QUEUE)
                .where(Tables.QUEUE.STATE.eq(state))
                .orderBy(Tables.QUEUE.COLLECTED_AT.asc())
                .limit(properties.getVacuum().getMax())
                .fetch();
        return dslContext.delete(Tables.QUEUE)
                .where(row(Tables.QUEUE.ID, Tables.QUEUE.UUID, Tables.QUEUE.COLLECTED_AT).in(fetch))
                .execute();


    }

    @Transactional
    public Integer deleteOrphanDocument() {
        final SelectJoinStep<Record1<UUID>> subSelect = dslContext.select(Tables.QUEUE.UUID).from(Tables.QUEUE);
        return dslContext.delete(Tables.DOCUMENTS)
                .where(Tables.DOCUMENTS.UUID.notIn(subSelect)).execute();
    }

    @Transactional
    public Integer deleteOrphanMetadata() {
        final SelectOrderByStep<Record1<UUID>> subselect = dslContext.select(Tables.METADATAS.UUID).from(Tables.METADATAS)
                .except(
                        dslContext.select(Tables.QUEUE.UUID).from(Tables.QUEUE)
                                .where(Tables.QUEUE.TYPE.eq(QueueType.metadata))
                )
                .except(
                        dslContext.select(Tables.DOCUMENTS.METADATA).from(Tables.DOCUMENTS)
                );
        return dslContext.delete(Tables.METADATAS)
                .where(
                        Tables.METADATAS.UUID.in(subselect)
                ).execute();

    }
}
