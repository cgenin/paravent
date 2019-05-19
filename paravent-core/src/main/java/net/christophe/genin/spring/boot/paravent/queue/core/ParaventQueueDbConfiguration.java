package net.christophe.genin.spring.boot.paravent.queue.core;

import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.jooq.impl.*;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;


@Configuration
public class ParaventQueueDbConfiguration {


    @Bean(name = "paraventQueueDataSource")
    @FlywayDataSource
    @ConfigurationProperties(prefix = "paravent.queue.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "paraventQueueTransactionAwareDataSource")
    public TransactionAwareDataSourceProxy transactionAwareDataSource() {
        return new TransactionAwareDataSourceProxy(dataSource());
    }

    @Bean(name = "paraventQueueTransactionManager")
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public ExceptionTranslator exceptionTransformer() {
        return new ExceptionTranslator();
    }

    @Bean(name = "paraventQueueJooqConfiguration")
    public DefaultConfiguration configuration() {
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        jooqConfiguration.set(connectionProvider());
        jooqConfiguration.set(new DefaultExecuteListenerProvider(exceptionTransformer()));
        jooqConfiguration.set(SQLDialect.POSTGRES_9_5);

        return jooqConfiguration;
    }

    @Bean(name = "paraventQueueConnectionProvider")
    public DataSourceConnectionProvider connectionProvider() {
        return new DataSourceConnectionProvider(transactionAwareDataSource());
    }


    @Bean
    public DefaultDSLContext dsl() {
        return new DefaultDSLContext(configuration());
    }


    public static class ExceptionTranslator extends DefaultExecuteListener {
        public void exception(ExecuteContext context) {
            SQLDialect dialect = context.configuration().dialect();
            SQLExceptionTranslator translator
                    = new SQLErrorCodeSQLExceptionTranslator(dialect.name());
            context.exception(translator
                    .translate("Access database using jOOQ", context.sql(), context.sqlException()));
        }
    }
}
