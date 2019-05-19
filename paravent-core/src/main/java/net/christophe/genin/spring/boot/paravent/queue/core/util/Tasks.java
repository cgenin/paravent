package net.christophe.genin.spring.boot.paravent.queue.core.util;

import io.vertx.core.shareddata.Shareable;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.shareddata.LocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Tasks {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tasks.class);

    private static final String TASK_SUFFIX = ".task";
    private static final String START_SUFFIX = ".start";
    private static final String STOP_SUFFIX = ".stop";
    public static final String ID = "___tasks";

    private final Vertx vertx;

    public static String nameStart(String key) {
        Objects.requireNonNull(key);
        return key + TASK_SUFFIX + START_SUFFIX;
    }

    public static String nameStop(String key) {
        Objects.requireNonNull(key);
        return key + TASK_SUFFIX + STOP_SUFFIX;
    }


    public static String generate(String key) {
        return key + "." + UUID.randomUUID().toString();
    }

    public Tasks(Vertx vertx) {
        this.vertx = vertx;
    }

    public void start(String task) {
        localMap(map -> {
            map.put(task, StateTask.start);
            LOGGER.info("Start task " + task + ".");
        });
    }

    public void register(String task) {
        localMap(map -> map.put(task, StateTask.registered));
    }

    public void stop(String task) {
        localMap(map -> map.put(task, StateTask.stop));
    }

    public Map<String, StateTask> findAll() {
        return vertx.sharedData().<String, Map<String, StateTask>>getLocalMap(ID)
                .getOrDefault(ID, new HashMap<>());
    }

    private void localMap(Consumer<Map<String, StateTask>> supplier) {
        final LocalMap<String, Map<String, StateTask>> localMap = vertx.sharedData().getLocalMap(ID);
        localMap.putIfAbsent(ID, new ShareableMap<>());
        final Map<String, StateTask> tasksMap = localMap.get(ID);
        supplier.accept(tasksMap);
    }

    public void createTasksStop(String task, Supplier<Long> suppId, Consumer<Long> update) {
        vertx.eventBus().consumer(Tasks.nameStop(task), msg_ -> {
            final Long currentId = suppId.get();
            if (Objects.nonNull(currentId)) {
                LOGGER.info("Stop task " + task + " " + vertx.cancelTimer(currentId));
                update.accept(null);
            }
            new Tasks(vertx).stop(task);
            msg_.reply(true);
        });
    }

    public enum StateTask implements Serializable {
        start, registered, stop
    }

    private static class ShareableMap<k, v> extends ConcurrentHashMap<k, v> implements Shareable {


    }
}
