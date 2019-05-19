package net.christophe.genin.spring.boot.paravent.queue.core;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import net.christophe.genin.spring.boot.paravent.queue.core.util.SingleSync;
import net.christophe.genin.spring.boot.paravent.queue.core.util.Tasks;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.DocumentsFactory;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.MetadatasFactory;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.TasksFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ParaventQueueManager {

    private final Vertx vertx;

    @Autowired
    public ParaventQueueManager(Vertx vertx) {
        this.vertx = vertx;
    }


    public Either<Throwable, JsonArray> getMetadataPeek(String key) {
        return SingleSync.fromSingle(getMetadataPeekAsync(key)).either();
    }

    private Single<JsonArray> getMetadataPeekAsync(String key) {
        return vertx.eventBus().<JsonArray>rxSend(MetadatasFactory.GET_BY_KEY, new JsonObject().put("key", key))
                .subscribeOn(Schedulers.computation())
                .map(msg -> Option.of(msg.body())
                        .getOrElseThrow(IllegalStateException::new));
    }

    public String saveMetadata(String key, Map<String, Object> json) {
        return SingleSync.fromSingle(saveMetadataAsync(key, json)).either()
                .getOrElseThrow(err -> new IllegalStateException());
    }

    private Single<String> saveMetadataAsync(String key, Map<String, Object> json) {
        return Single.just(json)
                .subscribeOn(Schedulers.computation())
                .map(JsonObject::new)
                .flatMap(obj -> vertx.eventBus().<String>rxSend(MetadatasFactory.INSERT, new JsonObject().put("key", key).put("payload", obj)))
                .map(Message::body);
    }

    public String saveDocument(String key, String filename, String metadata, MultipartFile document) {
        return SingleSync.fromSingle(saveDocumentAsync(key, filename, metadata, document)).either()
                .getOrElseThrow(err -> new IllegalStateException());
    }

    private Single<String> saveDocumentAsync(String key, String filename, String metadata, MultipartFile document) {
        return Single.just(new JsonObject().put("key", key))
                .subscribeOn(Schedulers.computation())
                .map(obj -> Option.of(filename).map(f -> obj.put("filename", f)).getOrElse(obj))
                .map(obj -> Option.of(metadata)
                        .map(JsonObject::new)
                        .map(m -> obj.put("metadata", m)).getOrElse(obj))
                .map(obj -> obj.put("document", document.getBytes()))
                .flatMap(obj -> vertx.eventBus().<String>rxSend(DocumentsFactory.INSERT, obj))
                .map(Message::body);
    }

    public Either<Throwable, String> getTasks() {
        return SingleSync.fromSingle(
                Single.just(new Tasks(vertx).findAll().entrySet())
                        .subscribeOn(Schedulers.computation())
                        .map(entries -> entries.stream()
                                .map(entry -> new JsonObject().put("name", entry.getKey()).put("state", entry.getValue().name()))
                                .collect(Collectors.toList()))
                        .map(JsonArray::new)
                        .map(JsonArray::encode)
        )
                .either();
    }

    public Either<Throwable, Boolean> startAllTasks() {
        return SingleSync.fromSingle(
                vertx.eventBus().rxSend(TasksFactory.START_ALL, new JsonObject())
                        .subscribeOn(Schedulers.computation())
                        .map(Objects::nonNull)
        ).either();
    }

    public Either<Throwable, Boolean> stopAllTasks() {
        return SingleSync.fromSingle(
                vertx.eventBus().rxSend(TasksFactory.STOP_ALL, new JsonObject())
                        .subscribeOn(Schedulers.computation())
                        .map(Objects::nonNull)
        ).either();
    }
}
