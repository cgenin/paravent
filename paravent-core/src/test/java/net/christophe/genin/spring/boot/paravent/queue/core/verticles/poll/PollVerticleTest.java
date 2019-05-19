package net.christophe.genin.spring.boot.paravent.queue.core.verticles.poll;

import io.vertx.core.Verticle;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueProperties;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.PollManager;
import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(VertxUnitRunner.class)
public class PollVerticleTest {


    private static final long VALUETIME = 450L;
    private Vertx vertx;
    private ParaventQueueProperties properties;
    private PollManager pollManager;
    private PollContract pollContract;
    private PollVerticle pollVerticle;
    private String id;

    @Before
    public void before() {
        vertx = Vertx.vertx();
        properties = new ParaventQueueProperties();
        properties.getPoll().getTimeByKey().put("key", VALUETIME);
        properties.getPoll().setDefaultTime(1500L);
        properties.getPoll().getBatchByKey().put("key", 1);
        properties.getPoll().setDefaultBatch(2);

        pollManager = mock(PollManager.class);

        pollContract = mock(PollContract.class);
        when(pollContract.getKey()).thenReturn("key");

        final Supplier<Verticle> supplier = PollBuilder.builder().withProperties(properties).withManager(pollManager).build(pollContract);
        assertThat(supplier).isNotNull();
        final Verticle verticle = supplier.get();
        assertThat(verticle).isNotNull().has(new Condition<>(v -> v instanceof PollVerticle, "instance poll verticle"));
        pollVerticle = (PollVerticle) verticle;

    }

    @After
    public void after() {
        if (Objects.nonNull(id)) {
            vertx.undeploy(id);
        }
    }

    @Test
    public void should_generate() {
        final Long l1 = pollVerticle.generatedLong();
        assertThat(l1).isGreaterThan(0L).isNotEqualTo(pollVerticle.generatedLong());
    }

    @Test
    public void should_get_poll_time() {
        assertThat(pollVerticle.pollTime()).isEqualTo(VALUETIME);
        when(pollContract.getKey()).thenReturn("key2");
        assertThat(pollVerticle.pollTime()).isEqualTo(1500L);

    }

    @Test
    public void should_get_nb_batch() {
        assertThat(pollVerticle.nbBatch()).isEqualTo(1);
        when(pollContract.getKey()).thenReturn("key2");
        assertThat(pollVerticle.nbBatch()).isEqualTo(2);

    }

    @Ignore
    @Test
    public void should_start(TestContext testContext) {
        final Async async = testContext.async(1);
        vertx.rxDeployVerticle(pollVerticle)
                .delay(2650, TimeUnit.MILLISECONDS)
                .subscribe((id) -> {
                    this.id = id;
                    assertThat(id).isNotNull();

                    ArgumentCaptor<PollContract> argPoll = ArgumentCaptor.forClass(PollContract.class);
                    ArgumentCaptor<Integer> argNb = ArgumentCaptor.forClass(Integer.class);
                    verify(pollManager, only()).poll(argPoll.capture(), argNb.capture());

                    assertThat(argNb.getValue()).isEqualTo(1);
                    assertThat(argPoll.getValue().getKey()).isEqualTo("key");
                    async.countDown();
                });
    }
}
