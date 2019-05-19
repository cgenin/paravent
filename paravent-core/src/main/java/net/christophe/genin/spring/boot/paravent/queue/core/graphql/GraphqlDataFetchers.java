package net.christophe.genin.spring.boot.paravent.queue.core.graphql;


import graphql.schema.DataFetcher;
import io.vavr.Function2;
import io.vavr.control.Option;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.GraphqlManager;
import net.christophe.genin.spring.boot.paravent.queue.core.util.Futures;
import net.christophe.genin.spring.boot.paravent.queue.core.util.Tasks;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.QueuesFactory;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.TasksFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Datafetcher Builder.
 */
class GraphqlDataFetchers {

    private final Vertx vertx;
    private final GraphqlManager graphqlManager;


    GraphqlDataFetchers(Vertx vertx, GraphqlManager graphqlManager) {
        this.vertx = vertx;
        this.graphqlManager = graphqlManager;
    }


    @SuppressWarnings("unchecked")
    DataFetcher getQueueOperation() {
        return env -> {
            final Map<String, Object> arguments = env.getArguments();
            final Map<String, Object> filter = (Map<String, Object>) arguments.getOrDefault("filter", new HashMap<>());
            final Integer skip = (Integer) arguments.get("skip");
            final Integer first = (Integer) arguments.get("first");

            final HashMap<String, Object> result = new HashMap<>();
            result.put("total", graphqlManager.countQueue(filter));
            result.put("result", graphqlManager.getQueue(filter, skip, first));
            return result;
        };
    }

    DataFetcher getMetadataOperation() {
        return getOperation(graphqlManager::countMetadata, graphqlManager::getMetadata);
    }

    DataFetcher getSubQueue() {
        return env -> {
            final Map<String, Object> source = env.getSource();
            final UUID uuid = (UUID) source.get("uuid");
            return graphqlManager.findQueueBy(uuid);
        };
    }

    DataFetcher getDocumentOperation() {
        return getOperation(graphqlManager::countDocument, graphqlManager::getDocument);
    }

    private DataFetcher getOperation(Supplier total, Function2<Integer, Integer, List<Map<String, Object>>> list) {
        return env -> {
            final Map<String, Object> arguments = env.getArguments();
            final Integer skip = (Integer) arguments.get("skip");
            final Integer first = (Integer) arguments.get("first");
            final HashMap<String, Object> result = new HashMap<>();
            result.put("total", total.get());
            result.put("result", list.apply(skip, first));
            return result;
        };
    }

    DataFetcher getDocumentMetadata() {
        return env -> {
            final Map<String, Object> source = env.getSource();
            final UUID uuid = (UUID) source.get("uuid");
            return graphqlManager.getDocumentMetadata(uuid);
        };
    }

    DataFetcher getDocumentBin() {
        return env -> {
            final Map<String, Object> source = env.getSource();
            final UUID uuid = (UUID) source.get("uuid");
            final byte[] documentBin = graphqlManager.getDocumentBin(uuid);
            return Base64.getEncoder().encodeToString(documentBin);
        };
    }

    DataFetcher getTasksOperation() {
        return env -> getTasks();
    }

    private List<HashMap<String, String>> getTasks() {
        return new Tasks(vertx).findAll().entrySet().stream()
                .map(entry -> {
                    final HashMap<String, String> result = new HashMap<>();
                    result.put("name", entry.getKey());
                    result.put("state", entry.getValue().name());
                    return result;
                }).collect(Collectors.toList());
    }


    DataFetcher startAllTasks() {
        return env -> sendForTask(TasksFactory.START_ALL, new JsonObject());
    }

    private CompletableFuture<List<HashMap<String, String>>> sendForTask(String address, Object payload) {
        return Futures.from(vertx.eventBus().rxSend(address, payload)
                .map(res -> getTasks()));
    }

    DataFetcher stopAllTasks() {
        return env -> sendForTask(TasksFactory.STOP_ALL, new JsonObject());
    }

    DataFetcher startTasks() {
        return env -> {
            final List<String> ids = env.getArgument("ids");
            return Option.of(ids)
                    .filter(l -> !l.isEmpty())
                    .map(JsonArray::new)
                    .map(arr -> sendForTask(TasksFactory.START, arr))
                    .getOrElse(() -> CompletableFuture.completedFuture(Collections.emptyList()));
        };
    }

    DataFetcher stopTasks() {
        return env -> {
            final List<String> ids = env.getArgument("ids");
            return Option.of(ids)
                    .filter(l -> !l.isEmpty())
                    .map(JsonArray::new)
                    .map(arr -> sendForTask(TasksFactory.STOP, arr))
                    .getOrElse(() -> CompletableFuture.completedFuture(Collections.emptyList()));
        };
    }

    DataFetcher addToQueue() {
        return env -> {
            final List<String> ids = env.getArgument("ids");
            return Option.of(ids)
                    .filter(l -> !l.isEmpty())
                    .map(JsonArray::new)
                    .map(arr -> Futures.from(vertx.eventBus().<Integer>rxSend(QueuesFactory.ADD_ALL, arr).map(Message::body)))
                    .getOrElse(() -> CompletableFuture.completedFuture(0));
        };
    }
}
