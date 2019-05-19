package net.christophe.genin.spring.boot.paravent.queue.core.rest.api;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Administration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Administration.class);
    private final ParaventQueueManager manager;

    @Autowired
    public Administration(ParaventQueueManager manager) {
        this.manager = manager;
    }

    @RequestMapping(value = "/api/paravent/queue/metadatas/{key}/poll", method = {RequestMethod.GET})
    public String poll(@PathVariable("key") String key) {
        return manager.getMetadataPeek(key)
                .map(JsonArray::encode)
                .getOrElseThrow((err) -> {
                    LOGGER.error("Error in getting metadata peek", err);
                    return new IllegalStateException();
                });
    }

    @RequestMapping(value = "/api/tasks", method = {RequestMethod.GET})
    public String getTasks() {
        return manager.getTasks()
                .getOrElseThrow((err) -> {
                    LOGGER.error("Error in getting metadata tasks", err);
                    return new IllegalStateException();
                });
    }

    @RequestMapping(value = "/api/tasks/start", method = {RequestMethod.POST})
    public String startAllTasks() {
        return manager.startAllTasks()
                .map(b -> new JsonObject().put("running", b).encode())
                .getOrElseThrow((err) -> {
                    LOGGER.error("Error in getting metadata tasks", err);
                    return new IllegalStateException();
                });
    }

    @RequestMapping(value = "/api/tasks/stop", method = {RequestMethod.POST})
    public String stopAllTasks() {
        return manager.stopAllTasks()
                .map(b -> new JsonObject().put("running", b).encode())
                .getOrElseThrow((err) -> {
                    LOGGER.error("Error in getting metadata tasks", err);
                    return new IllegalStateException();
                });
    }
}
