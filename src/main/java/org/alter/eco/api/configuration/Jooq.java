package org.alter.eco.api.configuration;

import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import javax.sql.DataSource;

@Configuration
public class Jooq {

    @Bean
    public DefaultDSLContext dsl(org.jooq.Configuration configuration) {
        return new DefaultDSLContext(configuration);
    }

    @Bean
    public org.jooq.Configuration configuration(DataSourceConnectionProvider connectionProvider) {
        return new DefaultConfiguration()
            .set(SQLDialect.POSTGRES)
            .set(connectionProvider)
            .set(new DefaultExecuteListenerProvider(new ExceptionTranslator()));
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider(DataSource dataSource) {
        return new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(dataSource));
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    public static class ExceptionTranslator extends DefaultExecuteListener {

        @Override
        public void exception(ExecuteContext ctx) {
            var translator = new SQLErrorCodeSQLExceptionTranslator(SQLDialect.POSTGRES.thirdParty().springDbName());
            ctx.exception(translator.translate("Access database using jOOQ", ctx.sql(), ctx.sqlException()));
        }
    }
}
