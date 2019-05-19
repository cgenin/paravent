package net.christophe.genin.spring.boot.paravent.queue.core;

import io.vavr.control.Either;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.DocumentsFactory;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.MetadatasFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(VertxUnitRunner.class)
public class ParaventQueueManagerTest {

    private ParaventQueueManager paraventQueueManager;
    private Consumer<Message<JsonObject>> getByKey;
    private Consumer<Message<JsonObject>> insertMetdata;
    private Vertx vertx;
    private Consumer<Message<JsonObject>> insertDocument;

    @Before
    public void before() {
        vertx = Vertx.vertx();
        paraventQueueManager = new ParaventQueueManager(vertx);
        vertx.eventBus().<JsonObject>consumer(MetadatasFactory.GET_BY_KEY, msg -> {
            getByKey.accept(msg);

        });

        vertx.eventBus().<JsonObject>consumer(MetadatasFactory.INSERT, msg -> {
            insertMetdata.accept(msg);
        });

        vertx.eventBus().<JsonObject>consumer(DocumentsFactory.INSERT, msg -> {
            insertDocument.accept(msg);
        });
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void getMetadataPeekTest() {
        final JsonObject test = new JsonObject().put("test", true);
        getByKey = (msg) -> {
            assertThat(msg.body().getString("key")).isEqualTo("test");
            msg.reply(new JsonArray().add(test));
        };
        final JsonArray arr = paraventQueueManager.getMetadataPeek("test").getOrElseThrow(() -> new IllegalStateException());
        assertThat(arr).hasSize(1);
        assertThat(arr.getJsonObject(0)).isEqualTo(test);

    }

    @Test
    public void getMetadataPeekTestException() {
        getByKey = (msg) -> {
            msg.fail(500, "erro");
        };
        final Either<Throwable, JsonArray> either = paraventQueueManager.getMetadataPeek("test");
        assertThat(either.isLeft()).isTrue();
        assertThat(either.isRight()).isFalse();

    }

    @Test
    public void saveDocumentWithoutAll() {
        final MockMultipartFile document = new MockMultipartFile("tt", new byte[]{1});
        insertDocument = msg -> {
            assertThat(msg.body().size()).isEqualTo(2);
            assertThat(msg.body().getString("key")).isEqualTo("test");
            assertThat(msg.body().getBinary("document")).hasSize(1).contains(1);
            msg.reply("result");
        };

        final String r = paraventQueueManager.saveDocument("test", null, null, document);
        assertThat(r).isEqualTo("result");
    }

    @Test
    public void saveDocument() {
        final MockMultipartFile document = new MockMultipartFile("tt", new byte[]{1});
        insertDocument = msg -> {
            assertThat(msg.body().size()).isEqualTo(4);
            assertThat(msg.body().getString("key")).isEqualTo("test");
            assertThat(msg.body().getBinary("document")).hasSize(1).contains(1);
            assertThat(msg.body().getString("filename")).isEqualTo("test.pdf");
            assertThat(msg.body().getJsonObject("metadata")).hasSize(1);
            assertThat(msg.body().getJsonObject("metadata").getBoolean("test")).isTrue();
            msg.reply("result");
        };

        final String r = paraventQueueManager.saveDocument("test", "test.pdf", "{\"test\":true}", document);
        assertThat(r).isEqualTo("result");
    }

    @Test
    public void saveMetadata() {
        final HashMap<String, Object> payload = new HashMap<>();
        payload.put("id", UUID.randomUUID().toString());
        insertMetdata = (msg) -> {
            assertThat(msg.body()).isNotNull();
            assertThat(msg.body().getString("key")).isEqualTo("test");
            final JsonObject payload1 = msg.body().getJsonObject("payload");
            assertThat(payload1.size()).isEqualTo(1);
            assertThat(payload1.getString("id")).isEqualTo(payload.get("id"));
            msg.reply("result");
        };
        final String test = paraventQueueManager.saveMetadata("test", payload);
        assertThat(test).isEqualTo("result");
    }

    @Test(expected = Exception.class)
    public void saveMetadataException() {
        final HashMap<String, Object> payload = new HashMap<>();
        payload.put("id", UUID.randomUUID().toString());
        insertMetdata = (msg) -> {

            msg.fail(500, "error");
        };
        final String test = paraventQueueManager.saveMetadata("test", payload);
    }
}
