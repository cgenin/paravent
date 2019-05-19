package net.christophe.genin.spring.boot.paravent.queue.example;

import net.christophe.genin.spring.boot.paravent.queue.core.util.ParaventPrimaryJpaConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
@EntityScan
public class DbConfiguration extends ParaventPrimaryJpaConfiguration {


}
