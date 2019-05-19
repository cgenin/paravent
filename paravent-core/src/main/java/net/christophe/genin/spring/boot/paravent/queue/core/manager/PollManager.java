package net.christophe.genin.spring.boot.paravent.queue.core.manager;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import net.christophe.genin.spring.boot.paravent.queue.core.entities.QueueDb;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueState;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.tables.records.QueueRecord;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll.PollContract;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.Tables;
import org.jooq.DSLContext;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class PollManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PollManager.class);


    private final DSLContext dslContext;

    @Autowired
    public PollManager(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void poll(PollContract contract, int pageRequest) {
        AtomicInteger nbSuccess = new AtomicInteger(0);
        AtomicInteger nbError = new AtomicInteger(0);
        final long start = System.currentTimeMillis();
        dslContext.transaction(
                (conf) -> {
                    final DSLContext context = DSL.using(conf);
                    final String key = contract.getKey();
                    final List<QueueDb> queueds = poll(context, key, pageRequest);
                    Observable.fromIterable(queueds)
                            .subscribeOn(Schedulers.io())
                            .flatMap(pollOne(contract))
                            .subscribe(res -> {
                                        if (res) {
                                            nbSuccess.incrementAndGet();
                                        } else {
                                            nbError.incrementAndGet();
                                        }
                                    },
                                    err -> LOGGER.error("unroverable Error", err),
                                    () -> {
                                        final int nbAll = nbError.get() + nbSuccess.get();
                                        if (nbAll > 0) {
                                            LOGGER.info(key + " - treatments of " + nbAll + " with " + nbError.get() + " error(s)");

                                            if (LOGGER.isDebugEnabled()) {
                                                final long time = System.currentTimeMillis() - start;
                                                LOGGER.debug("treatment in " + time + " ms");
                                                LOGGER.debug("average " + (time / nbAll) + " ms");
                                            }
                                        }
                                    });
                }

        );
    }

    private List<QueueDb> poll(DSLContext context, String key, Integer limit) {
        final List<QueueDb> queues = context.select(Tables.QUEUE.asterisk())
                .from(Tables.QUEUE)
                .where(Tables.QUEUE.ID.equal(key).and(Tables.QUEUE.STATE.eq(QueueState.created)))
                .orderBy(Tables.QUEUE.COLLECTED_AT.asc())
                .limit(limit)
                .forUpdate().skipLocked()
                .fetch(Managers.RECORD_2_QUEUE);

        if (queues.isEmpty()) {
            return queues;
        }

        final List<UpdateConditionStep<QueueRecord>> batchedUPdates = queues.parallelStream()
                .map(queueDb ->
                        context.update(Tables.QUEUE)
                                .set(Tables.QUEUE.STATE, QueueState.queued)
                                .where(Tables.QUEUE.ID.eq(queueDb.getKey())
                                        .and(Tables.QUEUE.UUID.eq(queueDb.getUuid()))
                                        .and(Tables.QUEUE.COLLECTED_AT.eq(queueDb.getCollectedAt()))))
                .collect(Collectors.toList());


        final int nbupdateddate = IntStream.of(context.batch(batchedUPdates).execute()).sum();
        LOGGER.debug(Thread.currentThread().getName() + " queued " + nbupdateddate + " on " + queues.size());
        return queues;
    }

    private Function<QueueDb, ObservableSource<? extends Boolean>> pollOne(PollContract contract) {
        return queueDb -> Single.fromCallable(
                () -> running(queueDb))
                .map(contract::run)
                .doOnSuccess((b) -> {
                    if (b) {
                        success(queueDb);
                        return;
                    }
                    error(queueDb);
                })
                .onErrorReturn(err -> {
                    LOGGER.error("Error in dequeuing " + queueDb, err);
                    error(queueDb);
                    return false;
                })
                .toObservable();
    }


    private QueueDb running(QueueDb queueDb) {
        final int nb = dslContext.update(Tables.QUEUE)
                .set(Tables.QUEUE.STATE, QueueState.running)
                .set(Tables.QUEUE.START, new Timestamp(System.currentTimeMillis()))
                .where(Tables.QUEUE.ID.eq(queueDb.getKey())
                        .and(Tables.QUEUE.UUID.eq(queueDb.getUuid()))
                        .and(Tables.QUEUE.COLLECTED_AT.eq(queueDb.getCollectedAt()))
                ).execute();
        if (nb != 1) {
            throw new IllegalStateException("Impossible to running " + queueDb);
        }
        return queueDb;
    }

    private void error(QueueDb queueDb) {
        final int nb = dslContext.update(Tables.QUEUE)
                .set(Tables.QUEUE.STATE, QueueState.error)
                .set(Tables.QUEUE.STOP, new Timestamp(System.currentTimeMillis()))
                .where(Tables.QUEUE.ID.eq(queueDb.getKey())
                        .and(Tables.QUEUE.UUID.eq(queueDb.getUuid()))
                        .and(Tables.QUEUE.COLLECTED_AT.eq(queueDb.getCollectedAt()))
                ).execute();
        if (nb != 1) {
            throw new IllegalStateException("Impossible in error " + queueDb);
        }
    }

    private void success(QueueDb queueDb) {
        final int nb = dslContext.update(Tables.QUEUE)
                .set(Tables.QUEUE.STATE, QueueState.success)
                .set(Tables.QUEUE.STOP, new Timestamp(System.currentTimeMillis()))
                .where(Tables.QUEUE.ID.eq(queueDb.getKey())
                        .and(Tables.QUEUE.UUID.eq(queueDb.getUuid()))
                        .and(Tables.QUEUE.COLLECTED_AT.eq(queueDb.getCollectedAt()))
                ).execute();
        if (nb != 1) {
            throw new IllegalStateException("Impossible in success " + queueDb);
        }
    }
}
