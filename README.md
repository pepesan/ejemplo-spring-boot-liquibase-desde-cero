# ejemplo-spring-boot-liquibase-desde-cero

Proyecto de ejemplo que muestra cómo integrar **Liquibase** con **Spring Boot 4** para gestionar migraciones de base de datos de forma incremental y controlada.

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.6 |
| Liquibase | 5.0.2 |
| Hibernate ORM | 7.2.12 |
| H2 (desarrollo) | 2.4.240 |
| MySQL (producción) | conector 9.7.0 |

## Cómo funciona

Spring Boot arranca y, antes de que Hibernate valide el esquema, Liquibase aplica automáticamente todos los changesets pendientes en orden. Hibernate tiene `ddl-auto: validate`, lo que significa que **nunca crea ni modifica tablas**: delega todo el control del esquema a Liquibase.

```
Arranque
  └─ Liquibase lee db.changelog-master.yaml
       └─ Aplica changesets pendientes (en orden)
            └─ Hibernate valida que el esquema coincide con las entidades
                 └─ Aplicación lista
```

## Estructura del proyecto

```
src/main/resources/
├── application.yaml
└── db/
    └── changelog/
        ├── db.changelog-master.yaml          # Índice principal
        ├── 001-crear-tabla-usuarios.yaml
        ├── 002-anadir-columna-email.yaml
        ├── 003-crear-tabla-pedidos.yaml
        └── 004-crear-indice-email.yaml
```

## Migraciones incluidas

| Changeset | Descripción | Tag |
|---|---|---|
| `001` | Crea tabla `usuarios` (`id`, `nombre`) | |
| `002` | Añade columna `email` con constraint único | |
| — | Tag `v1.0` — esquema básico de usuarios | `v1.0` |
| `003` | Crea tabla `pedidos` con FK a `usuarios` (CASCADE) | |
| `004` | Crea índice en `usuarios.email` | |
| — | Tag `v2.0` — esquema completo | `v2.0` |

Cada changeset tiene su bloque `rollback` definido para poder revertir.

## Configuración (`application.yaml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/migracion-demo
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.yaml
  jpa:
    hibernate:
      ddl-auto: validate   # Hibernate solo valida; Liquibase gestiona el esquema
  h2:
    console:
      enabled: true
      path: /h2-console
```

La base de datos H2 se persiste en `./data/migracion-demo` (fichero en disco, no en memoria), por lo que las migraciones sobreviven a reinicios.

## Arrancar la aplicación

```bash
./gradlew build
./gradlew bootRun
```

Consola H2 disponible en: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/migracion-demo`
- Usuario: `sa` / Contraseña: _(vacía)_

## Tareas Liquibase desde Gradle

> **Importante:** estas tareas acceden directamente al fichero H2 en disco. Deben ejecutarse con la aplicación **parada**; si Spring Boot está arrancado, H2 tiene bloqueado el fichero y los comandos fallarán.

El proyecto incluye tareas Gradle para ejecutar la CLI de Liquibase directamente:

```bash
# Aplicar todos los changesets pendientes
./gradlew liquibaseUpdate

# Aplicar hasta un tag concreto
./gradlew liquibaseUpdateToTag -Ptag=v1.0

# Rollback hasta un tag concreto
./gradlew liquibaseRollback -Ptag=v1.0

# Ver changesets pendientes
./gradlew liquibaseStatus

# Ver historial de changesets aplicados
./gradlew liquibaseHistory
```

## Convención para nuevas migraciones

1. Crear un fichero `NNN-descripcion-corta.yaml` en `src/main/resources/db/changelog/`
2. Añadir un `include` al final de `db.changelog-master.yaml`
3. Definir siempre el bloque `rollback`
4. Nunca modificar un changeset ya aplicado — crear uno nuevo

```yaml
databaseChangeLog:
  - changeSet:
      id: 005-mi-cambio
      author: nombre.apellido
      changes:
        - ...
      rollback:
        - ...
```