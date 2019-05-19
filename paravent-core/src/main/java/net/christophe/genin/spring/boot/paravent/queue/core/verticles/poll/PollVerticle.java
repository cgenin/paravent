package net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll;

import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import net.christophe.genin.spring.boot.paravent.queue.core.util.Tasks;

import java.util.Objects;

public class PollVerticle extends AbstractVerticle {

    private final PollBuilderImpl builder;
    private Long currentId;

    PollVerticle(PollBuilderImpl builder) {
        this.builder = builder;
    }


    Long generatedLong() {
        long leftLimit = 1L;
        long rightLimit = 2000L;
        return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));

    }

    Long pollTime() {
        return builder.properties.getPoll().getTimeByKey().getOrDefault(builder.contract.getKey(),
                builder.properties.getPoll().getDefaultTime());
    }

    Integer nbBatch() {
        return builder.properties.getPoll().getBatchByKey().getOrDefault(builder.contract.getKey(),
                builder.properties.getPoll().getDefaultBatch());
    }

    @Override
    public Completable rxStart() {

        final Long pollTime = pollTime() + generatedLong();
        final Integer nbBatch = nbBatch();
        final Tasks tasks = new Tasks(vertx);
        final String addressTask = Tasks.generate("poll." + builder.contract.getKey());
        tasks.register(addressTask);
        vertx.eventBus().consumer(Tasks.nameStart(addressTask), msg -> {
            if (Objects.isNull(currentId)) {
                registerTask(pollTime, nbBatch);
                tasks.start(addressTask);
            }
            msg.reply(true);

        });
        tasks.createTasksStop(addressTask, () -> currentId, (v) -> currentId = v);


        return vertx.eventBus().rxSend(Tasks.nameStart(addressTask), new JsonObject())
                .ignoreElement();
    }

    @SuppressWarnings("unused")
    private void registerTask(Long pollTime, int pageRequest) {
        currentId = vertx.setPeriodic(pollTime,
                (id) -> builder.pollManager.poll(builder.contract, pageRequest)
        );
    }


}
