package h8mt;

import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;

import javax.enterprise.context.RequestScoped;
import java.util.logging.Logger;

@RequestScoped
@Unremovable
public class OrganizationTenantResolver implements TenantResolver {

    private Logger log = Logger.getLogger("OrganizationTenantResolver");

    @Override
    public String getDefaultTenantId() {
      log.info("method hit: getDefaultTenantId");
      return "localdemo";
    }

    @Override
    public String resolveTenantId() {
        log.info("method hit: resolveTenantId");
        return "localdemo";
    }
}
