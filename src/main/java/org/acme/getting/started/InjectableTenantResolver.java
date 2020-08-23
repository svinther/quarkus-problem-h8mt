package org.acme.getting.started;

import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.Optional;

@RequestScoped
@Unremovable
public class InjectableTenantResolver implements TenantResolver {

    @Inject
    TenantConnections tenantConnections;

    private Optional<String> requestTenant = Optional.empty();

    public void setRequestTenant(String requestTenant) {
        this.requestTenant = Optional.of(requestTenant);
    }

    @Override
    public String getDefaultTenantId() {
        return tenantConnections.allTenants()
                .stream()
                .findAny()
                .orElseThrow(() -> new RuntimeException("No tenants known at all"));
    }

    @Override
    public String resolveTenantId() {
        return requestTenant.orElseThrow(() -> new RuntimeException("No tenant specified by current request"));
    }
}
