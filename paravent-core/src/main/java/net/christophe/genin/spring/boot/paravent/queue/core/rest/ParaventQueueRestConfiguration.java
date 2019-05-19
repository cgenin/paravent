package net.christophe.genin.spring.boot.paravent.queue.core.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty("paravent.queue.rest.enabled")
@ComponentScan("net.christophe.genin.spring.boot.c.queue.core.rest.api")
public class ParaventQueueRestConfiguration {


}
