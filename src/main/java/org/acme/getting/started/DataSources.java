package org.acme.getting.started;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalConnectionPoolConfiguration;
import io.agroal.api.configuration.AgroalDataSourceConfiguration;
import io.agroal.api.configuration.supplier.AgroalConnectionFactoryConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalConnectionPoolConfigurationSupplier;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.agroal.narayana.NarayanaTransactionIntegration;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.sql.Driver;
import java.util.Iterator;
import java.util.ServiceLoader;

@Singleton
public class DataSources {

    private static final Logger log = Logger.getLogger(DataSources.class);
    private TransactionManager transactionManager;
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    public DataSources(TransactionManager transactionManager, TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.transactionManager = transactionManager;
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }

    /**
     * Uses the {@link ServiceLoader#load(Class) ServiceLoader to load the JDBC drivers} in context
     * of the current {@link Thread#getContextClassLoader() TCCL}
     */
    private static void loadDriversInTCCL() {
        // load JDBC drivers in the current TCCL
        final ServiceLoader<Driver> drivers = ServiceLoader.load(Driver.class);
        final Iterator<Driver> iterator = drivers.iterator();
        while (iterator.hasNext()) {
            try {
                // load the driver
                iterator.next();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    void startup(@Observes StartupEvent event) {
    }

    public AgroalDataSource doCreateDataSource(DBConnectionInfo dbConnectionInfo) {

        // we first make sure that all available JDBC drivers are loaded in the current TCCL
        loadDriversInTCCL();

        String resolvedDriverClass = "org.postgresql.Driver";
        Class<?> driver;
        try {
            driver = Class.forName(resolvedDriverClass, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Unable to load the datasource driver " + resolvedDriverClass + " for datasource", e);
        }

        AgroalDataSourceConfigurationSupplier dataSourceConfiguration = new AgroalDataSourceConfigurationSupplier();

        // Set pool-less mode
        if (!dbConnectionInfo.isPoolingEnabled()) {
            dataSourceConfiguration.dataSourceImplementation(AgroalDataSourceConfiguration.DataSourceImplementation.AGROAL_POOLLESS);
        }

        AgroalConnectionPoolConfigurationSupplier poolConfiguration = dataSourceConfiguration.connectionPoolConfiguration();
        AgroalConnectionFactoryConfigurationSupplier connectionFactoryConfiguration = poolConfiguration
                .connectionFactoryConfiguration();

        applyNewConfiguration(dataSourceConfiguration, poolConfiguration, connectionFactoryConfiguration, driver, dbConnectionInfo);

        // Explicit reference to bypass reflection need of the ServiceLoader used by AgroalDataSource#from
        AgroalDataSourceConfiguration agroalConfiguration = dataSourceConfiguration.get();
        AgroalDataSource dataSource = new io.agroal.pool.DataSource(agroalConfiguration,
                new AgroalEventLoggingListener(dbConnectionInfo.getId()));
        log.debugv("Started datasource {0} connected to {1}", dbConnectionInfo.getId(),
                agroalConfiguration.connectionPoolConfiguration().connectionFactoryConfiguration().jdbcUrl());

        return dataSource;
    }

    private void applyNewConfiguration(AgroalDataSourceConfigurationSupplier dataSourceConfiguration,
                                       AgroalConnectionPoolConfigurationSupplier poolConfiguration,
                                       AgroalConnectionFactoryConfigurationSupplier connectionFactoryConfiguration, Class<?> driver,
                                       DBConnectionInfo dbConnectionInfo) {
        connectionFactoryConfiguration.jdbcUrl("jdbc:postgresql://" + dbConnectionInfo.getHost() + ":" + dbConnectionInfo.getPort() + "/" + dbConnectionInfo.getDb());
        connectionFactoryConfiguration.connectionProviderClass(driver);
        connectionFactoryConfiguration.trackJdbcResources(true);

        io.agroal.api.transaction.TransactionIntegration txIntegration = new NarayanaTransactionIntegration(
                transactionManager,
                transactionSynchronizationRegistry
        );
        poolConfiguration.transactionIntegration(txIntegration);

        dataSourceConfiguration.metricsEnabled(true);

        connectionFactoryConfiguration.principal(new NamePrincipal(dbConnectionInfo.getUser()));
        connectionFactoryConfiguration.credential(new SimplePassword(dbConnectionInfo.getPassword()));

        // Pool size configuration:
        poolConfiguration.minSize(0);
        poolConfiguration.maxSize(10);

        // Connection management
        poolConfiguration.connectionValidator(AgroalConnectionPoolConfiguration.ConnectionValidator.defaultValidator());
    }


}
