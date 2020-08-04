package h8mt;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalDataSourceConfiguration;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.quarkus.hibernate.orm.runtime.customized.QuarkusConnectionProvider;
import io.quarkus.hibernate.orm.runtime.tenant.TenantConnectionResolver;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import javax.enterprise.context.ApplicationScoped;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static io.agroal.api.configuration.AgroalConnectionFactoryConfiguration.TransactionIsolation.SERIALIZABLE;
import static io.agroal.api.configuration.AgroalConnectionPoolConfiguration.ConnectionValidator.defaultValidator;
import static java.time.Duration.ofSeconds;

@ApplicationScoped
public class SignFlowTenantConnectionResolver implements TenantConnectionResolver {

  private Map<String, ConnectionProvider> cache = new HashMap<>();

  @Override
  public ConnectionProvider resolve(String s) {
    if (!cache.containsKey(s)) {
      try {
        AgroalDataSource agroalDataSource = AgroalDataSource.from(createDataSourceConfigurationSupplier());
        QuarkusConnectionProvider quarkusConnectionProvider = new QuarkusConnectionProvider(agroalDataSource);
        cache.put(s, quarkusConnectionProvider);
        return quarkusConnectionProvider;
      } catch (SQLException ex) {
        throw new IllegalStateException("Failed to create a new data source based on the tenantId: " + s, ex);
      }
    }
    return cache.get(s);
  }

  private static AgroalDataSourceConfigurationSupplier createDataSourceConfigurationSupplier() {
    return new AgroalDataSourceConfigurationSupplier()
      .dataSourceImplementation(AgroalDataSourceConfiguration.DataSourceImplementation.AGROAL)
      .metricsEnabled(false)
      .connectionPoolConfiguration(cp -> cp
        .minSize(5)
        .maxSize(10)
        .initialSize(5)
        .connectionValidator(defaultValidator())
        .acquisitionTimeout(ofSeconds(5))
        .leakTimeout(ofSeconds(5))
        .validationTimeout(ofSeconds(50))
        .reapTimeout(ofSeconds(500))
        .connectionFactoryConfiguration(cf -> cf
          .jdbcUrl("jdbc:h2:mem:test")
          .connectionProviderClassName("org.h2.Driver")
          .autoCommit(false)
          .jdbcTransactionIsolation(SERIALIZABLE)
          .principal(new NamePrincipal("username"))
          .credential(new SimplePassword("secret"))
        )
      );
  }
}


