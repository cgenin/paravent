package net.christophe.genin.spring.boot.paravent.queue.core.verticles;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.vavr.Function1;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import net.christophe.genin.spring.boot.paravent.queue.core.util.Tasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TasksFactory implements VerticleFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TasksFactory.class);

    public static final String START_ALL = TasksFactory.class.getName() + ".start.all";
    public static final String STOP_ALL = TasksFactory.class.getName() + ".stop.all";
    public static final String START = TasksFactory.class.getName() + ".start";
    public static final String STOP = TasksFactory.class.getName() + ".stop";

    @Override
    public Verticle get() {
        return new TasksVerticle();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static class TasksVerticle extends AbstractVerticle {


        private Tasks tasks;

        @Override
        public Completable rxStart() {
            return Single.fromCallable(this::initialize)
                    .ignoreElement();
        }

        private Boolean initialize() {
            tasks = new Tasks(vertx);
            vertx.eventBus().consumer(START_ALL, publish(Tasks::nameStart));
            vertx.eventBus().consumer(STOP_ALL, publish(Tasks::nameStop));
            vertx.eventBus().consumer(START, sendForOne(Tasks::nameStart));
            vertx.eventBus().consumer(STOP, sendForOne(Tasks::nameStop));
            return true;
        }

        private Handler<Message<JsonArray>> sendForOne(Function1<String, String> addressName) {
            return (msg) ->
                    Observable.fromIterable(msg.body())
                            .flatMap(sendForSpecificKey(addressName))
                            .subscribe(
                                    s -> LOGGER.info("send to " + s.encode()),
                                    onError(msg),
                                    onCompleted(msg)
                            );
        }

        private Function<Object, ObservableSource<? extends JsonObject>> sendForSpecificKey(Function1<String, String> addressName) {
            return key -> {
                final String adress = addressName.apply(key.toString());
                return vertx.eventBus().<Boolean>rxSend(adress, new JsonObject())
                        .map(Message::body)
                        .map(result -> new JsonObject().put("result", result).put("task", adress))
                        .toObservable();
            };
        }

        private Handler<Message<Object>> publish(Function1<String, String> addressName) {
            return (msg) ->
                    Observable.fromIterable(tasks.findAll().keySet())
                            .flatMap(sendForSpecificKey(addressName))
                            .subscribe(
                                    s -> LOGGER.debug("publish to " + s),
                                    onError(msg),
                                    onCompleted(msg)
                            );
        }

        private <T> Action onCompleted(Message<T> msg) {
            return () -> {
                LOGGER.info("publish completed.");
                msg.reply(true);
            };
        }

        private <T> Consumer<Throwable> onError(Message<T> msg) {
            return err -> {
                LOGGER.info("Error in publishing", err);
                msg.fail(500, "Error");
            };
        }


    }
}
