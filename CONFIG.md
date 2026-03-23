# Configuration Guide

Configuration is managed through `src/main/resources/application.properties` and profile-specific overrides.

---

## spring.application.name

**Type:** string  
**Default:** `mod-craft`  
**Description:** The name of the Spring Boot application, used in logging and actuator endpoints.

---

## spring.datasource.url

**Type:** string  
**Default:** `jdbc:h2:mem:modcraft;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`  
**Description:** JDBC URL for the database. The default uses an in-memory H2 database named `modcraft` that persists for the lifetime of the JVM.

**Example (persistent file-based H2):**
```
spring.datasource.url=jdbc:h2:file:./data/modcraft
```

---

## spring.datasource.driver-class-name

**Type:** string  
**Default:** `org.h2.Driver`  
**Description:** JDBC driver class. Change this if switching to a different database engine.

---

## spring.datasource.username

**Type:** string  
**Default:** `sa`  
**Description:** Database username.

---

## spring.datasource.password

**Type:** string  
**Default:** *(empty)*  
**Description:** Database password.

---

## spring.jpa.database-platform

**Type:** string  
**Default:** `org.hibernate.dialect.H2Dialect`  
**Description:** Hibernate dialect used to generate SQL. Must match the target database engine.

---

## spring.jpa.hibernate.ddl-auto

**Type:** string  
**Default:** `validate`  
**Description:** Controls how Hibernate manages the database schema on startup.

| Value | Behaviour |
|-------|-----------|
| `validate` | Validates the schema against the entities; fails if they do not match. Safe default for production. |
| `update` | Alters the schema to match the entities without dropping data. |
| `create-drop` | Creates the schema on startup and drops it on shutdown. Used in `dev` and `test` profiles. |
| `none` | Takes no action; the schema must be managed externally. |

**Example (dev profile override in `application-dev.properties`):**
```
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## spring.h2.console.enabled

**Type:** boolean  
**Default:** `false`  
**Description:** When `true`, enables the H2 web console at the path defined by `spring.h2.console.path`. Should only be enabled in local development environments.

**Example (dev profile override in `application-dev.properties`):**
```
spring.h2.console.enabled=true
```

---

## spring.h2.console.path

**Type:** string  
**Default:** `/h2-console`  
**Description:** URL path at which the H2 console is served when enabled.

---

## Profiles

| Profile | Purpose |
|---------|---------|
| *(default)* | Safe defaults for production-like environments (`ddl-auto=validate`, H2 console disabled). |
| `dev` | Local development: `ddl-auto=create-drop`, H2 console enabled. Activate with `--spring.profiles.active=dev`. |
| *(test)* | Applied automatically during `mvn test`: `ddl-auto=create-drop` via `src/test/resources/application.properties`. |
