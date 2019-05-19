package net.christophe.genin.spring.boot.paravent.queue.core.verticles;

import io.reactivex.functions.Consumer;
import io.vertx.core.DeploymentOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueProperties;
import net.christophe.genin.spring.boot.paravent.queue.core.jooq.enums.QueueState;
import net.christophe.genin.spring.boot.paravent.queue.core.manager.VacuumManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(VertxUnitRunner.class)
public class VacuumTest {


    private Vertx vertx;
    private ParaventQueueProperties prop;
    private String id;

    @Before
    public void before() {
        vertx = Vertx.vertx();
        prop = new ParaventQueueProperties();
        prop.getVacuum().setTime(600L);
    }

    @After
    public void after() {
        if (Objects.nonNull(id)) {
            vertx.undeploy(id);
        }
    }

    @Test
    public void test(TestContext testContext) {
        final Async async = testContext.async(4);

        final VacuumManager vacuumManager = mock(VacuumManager.class);
        when(vacuumManager.deleteAllSuccessQueueByState(QueueState.success))
                .thenAnswer((v) -> {
                    async.countDown();
                    return 1;
                });
        when(vacuumManager.deleteOrphanDocument())
                .thenAnswer((v) -> {
                    async.countDown();
                    return 2;
                });
        when(vacuumManager.deleteOrphanMetadata())
                .thenAnswer((v) -> {
                    async.countDown();
                    return 3;
                });

        vertx.rxDeployVerticle(new Vacuum(prop, vacuumManager), new DeploymentOptions().setWorker(true))
                .delay(650, TimeUnit.MILLISECONDS)
                .subscribe(getSubscribe(async));

    }

    private Consumer<String> getSubscribe(Async async) {
        return (id) -> {
            assertThat(id).isNotNull();
            async.countDown();
            this.id = id;
            //  vertx.undeploy(id);
        };
    }

    @Test
    public void testWithFailureIn1(TestContext testContext) {
        final Async async = testContext.async(4);
        final VacuumManager vacuumManager = mock(VacuumManager.class);
        when(vacuumManager.deleteAllSuccessQueueByState(QueueState.success))
                .thenAnswer((v) -> {
                    async.countDown();
                    throw new IllegalStateException();
                });
        when(vacuumManager.deleteOrphanDocument())
                .thenAnswer((v) -> {
                    async.countDown();
                    return 2;
                });
        when(vacuumManager.deleteOrphanMetadata())
                .thenAnswer((v) -> {
                    async.countDown();
                    return 3;
                });

        vertx.rxDeployVerticle(new Vacuum(prop, vacuumManager), new DeploymentOptions().setWorker(true))
                .delay(650, TimeUnit.MILLISECONDS)
                .subscribe(getSubscribe(async));

    }

    @Test
    public void testWithFailureIn2(TestContext testContext) {
        final Async async = testContext.async(4);
        final VacuumManager vacuumManager = mock(VacuumManager.class);
        when(vacuumManager.deleteAllSuccessQueueByState(QueueState.success))
                .thenAnswer((v) -> {
                    async.countDown();
                    return 2;
                });
        when(vacuumManager.deleteOrphanDocument())
                .thenAnswer((v) -> {
                    async.countDown();
                    throw new IllegalStateException();
                });
        when(vacuumManager.deleteOrphanMetadata())
                .thenAnswer((v) -> {
                    async.countDown();
                    return 3;
                });

        vertx.rxDeployVerticle(new Vacuum(prop, vacuumManager), new DeploymentOptions().setWorker(true))
                .delay(650, TimeUnit.MILLISECONDS)
                .subscribe(getSubscribe(async));

    }

    @Test
    public void testWithFailureIn3(TestContext testContext) {
        final Async async = testContext.async(4);
        final VacuumManager vacuumManager = mock(VacuumManager.class);
        when(vacuumManager.deleteAllSuccessQueueByState(QueueState.success))
                .thenAnswer((v) -> {
                    async.countDown();
                    return 2;
                });
        when(vacuumManager.deleteOrphanDocument())
                .thenAnswer((v) -> {
                    async.countDown();
                    return 3;
                });
        when(vacuumManager.deleteOrphanMetadata())
                .thenAnswer((v) -> {
                    async.countDown();
                    throw new IllegalStateException();
                });

        vertx.rxDeployVerticle(new Vacuum(prop, vacuumManager), new DeploymentOptions().setWorker(true))
                .delay(650, TimeUnit.MILLISECONDS)
                .subscribe(getSubscribe(async));

    }
}
