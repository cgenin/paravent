package net.christophe.genin.spring.boot.paravent.queue.core.util;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import net.christophe.genin.spring.boot.paravent.queue.core.ParaventQueueProperties;
import net.christophe.genin.spring.boot.paravent.queue.core.verticles.VerticleFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DeploymentOptsTest {

    @Test
    public void base() {
        final ParaventQueueProperties paraventQueueProperties = new ParaventQueueProperties();
        paraventQueueProperties.getInstances().setNbDefault(1);
        paraventQueueProperties.getInstances().getByVerticle().put("A", 2);

        final DeploymentOptions optA = DeploymentOpts.base(paraventQueueProperties, A.class);
        assertThat(optA.getInstances()).isEqualTo(2);

        final DeploymentOptions optB = DeploymentOpts.base(paraventQueueProperties, B.class);
        assertThat(optB.getInstances()).isEqualTo(1);
    }

    @Test
    public void pollWithAvailableProcessor() {
        final ParaventQueueProperties paraventQueueProperties = new ParaventQueueProperties();
        paraventQueueProperties.getInstances().setNbPollDefault(1);
        paraventQueueProperties.getInstances().setForPoll(ParaventQueueProperties.ForPoll.availableProcessor);
        final DeploymentOptions optB = DeploymentOpts.poll(paraventQueueProperties, "test");
        assertThat(optB.getInstances()).isGreaterThan(1);
    }

    @Test
    public void pollWithFixed() {
        final ParaventQueueProperties paraventQueueProperties = new ParaventQueueProperties();
        paraventQueueProperties.getInstances().setNbPollDefault(1);
        paraventQueueProperties.getInstances().setForPoll(ParaventQueueProperties.ForPoll.fixed);
        final DeploymentOptions optB = DeploymentOpts.poll(paraventQueueProperties, "test");
        assertThat(optB.getInstances()).isEqualTo(1);
    }

    @Test(expected = RuntimeException.class)
    public void pollWithFixedException() {
        final ParaventQueueProperties paraventQueueProperties = new ParaventQueueProperties();
        paraventQueueProperties.getInstances().setNbPollDefault(null);
        paraventQueueProperties.getInstances().setForPoll(ParaventQueueProperties.ForPoll.fixed);
        DeploymentOpts.poll(paraventQueueProperties, "test");
    }

    @Test
    public void pollWithFixedSpecific() {
        final ParaventQueueProperties paraventQueueProperties = new ParaventQueueProperties();
        paraventQueueProperties.getInstances().setNbPollDefault(1);
        paraventQueueProperties.getInstances().setForPoll(ParaventQueueProperties.ForPoll.fixed);
        paraventQueueProperties.getInstances().getPoll().put("test", 2);
        final DeploymentOptions optB = DeploymentOpts.poll(paraventQueueProperties, "test");
        assertThat(optB.getInstances()).isEqualTo(2);

    }

    public static class A implements VerticleFactory {

        @Override
        public Verticle get() {
            return null;
        }
    }

    public static class B implements VerticleFactory {

        @Override
        public Verticle get() {
            return null;
        }
    }
}
