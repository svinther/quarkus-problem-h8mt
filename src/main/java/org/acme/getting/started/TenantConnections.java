package org.acme.getting.started;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalDataSourceConfiguration;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.runtime.customized.QuarkusConnectionProvider;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import javax.enterprise.context.ApplicationScoped;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.agroal.api.configuration.AgroalConnectionPoolConfiguration.ConnectionValidator.defaultValidator;
import static java.time.Duration.ofSeconds;

@ApplicationScoped
@Unremovable
public class TenantConnections implements TenantConnectionResolver {

    private final Map<String, DBConnectionInfo> dbConnectionInfoMap = Collections.unmodifiableMap(Map.of(
            "tenant1", new DBConnectionInfo("localhost", 5432, "hibernate", "hibernate", "hibernate_db1"),
            "tenant2", new DBConnectionInfo("localhost", 5432, "hibernate", "hibernate", "hibernate_db2"),
            "tenant3", new DBConnectionInfo("localhost", 5432, "hibernate", "hibernate", "hibernate_db3")
    ));

    private final Map<String, ConnectionProvider> cache = new HashMap<>();

    private static AgroalDataSourceConfiguration createDataSourceConfiguration(DBConnectionInfo dbConnectionInfo) {
        return new AgroalDataSourceConfigurationSupplier()
                .dataSourceImplementation(AgroalDataSourceConfiguration.DataSourceImplementation.AGROAL)
                .metricsEnabled(false)
                .connectionPoolConfiguration(cp -> cp
                        .minSize(0)
                        .maxSize(5)
                        .initialSize(0)
                        .connectionValidator(defaultValidator())
                        .acquisitionTimeout(ofSeconds(5))
                        .leakTimeout(ofSeconds(5))
                        .validationTimeout(ofSeconds(50))
                        .reapTimeout(ofSeconds(500))
                        .connectionFactoryConfiguration(cf -> cf
                                .jdbcUrl("jdbc:postgresql://" + dbConnectionInfo.getHost() + ":" + dbConnectionInfo.getPort() + "/" + dbConnectionInfo.getDb())
                                .connectionProviderClassName("org.postgresql.Driver")
                                .principal(new NamePrincipal(dbConnectionInfo.getUser()))
                                .credential(new SimplePassword(dbConnectionInfo.getPassword()))
                        )
                ).get();
    }

    public Set<String> allTenants() {
        return dbConnectionInfoMap.keySet();
    }

    @Override
    public ConnectionProvider resolve(String tenant) {
        if (!dbConnectionInfoMap.containsKey(tenant)) {
            throw new IllegalStateException("Unknown tenantId: " + tenant);
        }

        if (!cache.containsKey(tenant)) {
            try {
                DBConnectionInfo dbConnectionInfo = dbConnectionInfoMap.get(tenant);
                AgroalDataSource agroalDataSource = AgroalDataSource.from(createDataSourceConfiguration(dbConnectionInfo));
                QuarkusConnectionProvider quarkusConnectionProvider = new QuarkusConnectionProvider(agroalDataSource);
                cache.put(tenant, quarkusConnectionProvider);
                return quarkusConnectionProvider;
            } catch (SQLException ex) {
                throw new IllegalStateException("Failed to create a new data source based on the tenantId: " + tenant, ex);
            }
        }
        return cache.get(tenant);
    }
}


