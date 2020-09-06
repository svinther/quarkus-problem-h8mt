package org.acme.getting.started;

import io.agroal.api.AgroalDataSource;
import io.quarkus.arc.Arc;
import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.runtime.customized.QuarkusConnectionProvider;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
@Unremovable
public class TenantConnections implements TenantConnectionResolver {

    private static final Logger log = Logger.getLogger(TenantConnectionResolver.class);

    private final Map<String, DBConnectionInfo> dbConnectionInfoMap =
            IntStream.range(1, 10).boxed()
                    .collect(Collectors.toMap(
                            n -> String.format("tenant%02d", n),
                            n -> new DBConnectionInfo(String.valueOf(n), "localhost", 5432, "hibernate", "hibernate", String.format("hibernate_db%02d", n))
                    ));
    private final Map<String, ConnectionProvider> cache = new HashMap<>();


    public Set<String> allTenants() {
        return dbConnectionInfoMap.keySet();
    }

    @Override
    public ConnectionProvider resolve(String tenant) {
        if (!dbConnectionInfoMap.containsKey(tenant)) {
            throw new IllegalStateException("Unknown tenantId: " + tenant);
        }

        if (!cache.containsKey(tenant)) {
            DBConnectionInfo dbConnectionInfo = dbConnectionInfoMap.get(tenant);
            AgroalDataSource agroalDataSource = Arc.container().instance(DataSources.class).get().doCreateDataSource(dbConnectionInfo);
            QuarkusConnectionProvider quarkusConnectionProvider = new QuarkusConnectionProvider(agroalDataSource);
            cache.put(tenant, quarkusConnectionProvider);
            return quarkusConnectionProvider;
        }
        return cache.get(tenant);
    }


}


