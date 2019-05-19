package net.christophe.genin.spring.boot.paravent.queue.core.manager;

import net.christophe.genin.spring.boot.paravent.queue.core.entities.AwaitingNumber;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueState;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueType;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.Tables;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.inline;

@Service
public class QueueManager {

    private final DSLContext dslContext;

    @Autowired
    public QueueManager(DSLContext dslContext) {
        this.dslContext = dslContext;
    }


    @Transactional(readOnly = true)
    public List<AwaitingNumber> getGroupByKey() {
        return dslContext.select(Tables.QUEUE.ID.as("key"), count().as("nb")).from(Tables.QUEUE)
                .groupBy(Tables.QUEUE.ID)
                .orderBy(inline(2).desc())
                .fetchInto(AwaitingNumber.class);
    }


    static List<QueueDb> peek(DSLContext ctxt, String key, QueueType type, Integer limit) {
        return ctxt.select(Tables.QUEUE.asterisk())
                .from(Tables.QUEUE)
                .where(Tables.QUEUE.ID.equal(key).and(Tables.QUEUE.STATE.eq(QueueState.created))
                        .and(Tables.QUEUE.TYPE.eq(type))
                )
                .orderBy(Tables.QUEUE.COLLECTED_AT.asc())
                .limit(limit)
                .fetch(Managers.RECORD_2_QUEUE);
    }


    @Transactional(readOnly = true)
    public int addToQueue(List<UUID> ids) {
        return dslContext.update(Tables.QUEUE)
                .set(Tables.QUEUE.STATE, QueueState.created)
                .set(Tables.QUEUE.START, (Timestamp) null)
                .set(Tables.QUEUE.STOP, (Timestamp) null)
                .where(Tables.QUEUE.UUID.in(ids))
                .execute();
    }
}
