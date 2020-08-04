# h8mt project

## hibernate-orm multitenant config not being picked up

The methods of class `OrganizationTenantResolver` is never invoked

## application.properties needs to have default ds 

e.g.

```
# datasource configuration
quarkus.datasource.db-kind = h2
quarkus.datasource.username = localdemo
quarkus.datasource.password = localdemo123
quarkus.datasource.jdbc.url = jdbc:h2://localhost:5432/localdemo
```

Or EntityMAnager can not be injected.

There is a single `@Entity` defined in this project, but result is the same with or without this