package net.christophe.genin.spring.boot.paravent.queue.core;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class TasksTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    public void testGraphql() {
        this.webClient.post().uri("/graphql")
                .header(HttpHeaders.CONTENT_TYPE.toString(),  "application/json")
                .syncBody(new JsonObject().put("query","query{\n" +
                        "  task{\n" +
                        "   \tname\n" +
                        "    state\n" +
                        "  }\n" +
                        "}").encode())
                .exchange().expectStatus().isOk().expectBody(String.class)
                .value(
                        str -> {
                            final JsonArray arr = new JsonObject(str).getJsonObject("data").getJsonArray("task");
                            valid("start", arr);
                        }
                );

        this.webClient.post().uri("/graphql")
                .header(HttpHeaders.CONTENT_TYPE.toString(),  "application/json")
                .syncBody(new JsonObject().put("query","mutation{\n" +
                        "  stopAll{\n" +
                        "   \tname\n" +
                        "    state\n" +
                        "  }\n" +
                        "}").encode())
                .exchange().expectStatus().isOk().expectBody(String.class)
                .value(str -> {
                    final JsonArray arr = new JsonObject(str).getJsonObject("data").getJsonArray("stopAll");
                    valid("stop", arr);
                });

        this.webClient.post().uri("/graphql")
                .header(HttpHeaders.CONTENT_TYPE.toString(),  "application/json")
                .syncBody(new JsonObject().put("query","mutation{\n" +
                        "  startAll{\n" +
                        "   \tname\n" +
                        "    state\n" +
                        "  }\n" +
                        "}").encode())
                .exchange().expectStatus().isOk().expectBody(String.class)
                .value(str -> {
                    final JsonArray arr = new JsonObject(str).getJsonObject("data").getJsonArray("startAll");
                    valid("start", arr);
                });
    }



    private Consumer<String> valid(String state){
        return str -> {
            final JsonArray arr = new JsonArray(str);
            valid(state, arr);
        };
    }

    private void valid(String state, JsonArray arr) {
        assertThat(arr).hasSize(1);
        final JsonObject jsonObject = arr.getJsonObject(0);
        assertThat(jsonObject.getString("name")).contains("Vacuum");
        assertThat(jsonObject.getString("state")).isEqualTo(state);
    }


    @Test
    public void testRest() {
        this.webClient.get().uri("/api/tasks")
                .exchange().expectStatus().isOk().expectBody(String.class)
                .value(valid("start"));

        this.webClient.post().uri("/api/tasks/stop").exchange().expectStatus().isOk();

        this.webClient.get().uri("/api/tasks")
                .exchange().expectStatus().isOk().expectBody(String.class)
                .value(valid("stop"));

        this.webClient.post().uri("/api/tasks/start").exchange().expectStatus().isOk();

        this.webClient.get().uri("/api/tasks")
                .exchange().expectStatus().isOk().expectBody(String.class)
                .value(valid("start"));
    }


}
