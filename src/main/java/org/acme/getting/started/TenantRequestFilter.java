package org.acme.getting.started;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@ApplicationScoped
public class TenantRequestFilter implements ContainerRequestFilter {

    @Inject
    InjectableTenantResolver tenantResolver;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String tenantId = containerRequestContext.getHeaderString("X-tenant");
        if (tenantId != null) {
            tenantResolver.setRequestTenant(tenantId);
        }
    }
}
