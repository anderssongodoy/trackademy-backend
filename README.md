# Trackademy Backend

Backend de Trackademy construido con **Quarkus + Java 21 + PostgreSQL**, siguiendo una base de arquitectura hexagonal (puertos y adaptadores) y orientado a onboarding académico, catálogo de cursos/sílabos y evolución a analítica.

## Stack

- Java 21
- Quarkus 3.x
- Maven Wrapper
- PostgreSQL
- Hibernate ORM + Panache
- OpenAPI / Swagger UI

## Estado Actual

### Ya implementado

- Health check: `GET /health`
- Catálogo de cursos:
  - `GET /api/v1/catalog/cursos`
  - `GET /api/v1/catalog/cursos/{codigo}`
  - `GET /api/v1/catalog/cursos/{codigo}/detalle`
- Catálogos académicos:
  - `GET /api/v1/catalog/campuses?universidadId=...`
  - `GET /api/v1/catalog/carreras?universidadId=...`
  - `GET /api/v1/catalog/periodos?universidadId=...`
  - `GET /api/v1/catalog/periodos/{periodoId}/eventos`
- Onboarding básico/avanzado en un solo request:
  - `POST /api/v1/onboarding/basic`
- Datos del usuario (temporal con email por query):
  - `GET /api/v1/me/periodo-actual?email=...`
  - `GET /api/v1/me/cursos?email=...`

### Pendiente (siguiente fase)

- Autenticación/autorización real con Microsoft JWT (eliminar `email` por query)
- Endpoints de notas, tareas y calendario combinado
- OpenAPI más formal para contrato frontend

## Estructura (Hexagonal Base)

- `adapter/in/rest`: controladores HTTP y DTOs
- `application/port/in`: casos de uso (input ports)
- `application/port/out`: contratos hacia persistencia/externos (output ports)
- `application/service`: implementación de casos de uso
- `adapter/out/persistence`: adaptadores PostgreSQL
- `adapter/out/persistence/entity|repository`: entidades JPA y repositorios
- `domain/model`: modelos de dominio

## Requisitos

- Java 21 instalado
- PostgreSQL corriendo (local o remoto)
- Base creada: `trackademy_bd`

## Configuración

Archivo: `src/main/resources/application.properties`

Ejemplo local:

```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://127.0.0.1:5432/trackademy_bd
quarkus.datasource.username=postgres
quarkus.datasource.password=123

quarkus.hibernate-orm.database.generation=none
quarkus.flyway.migrate-at-start=false
quarkus.swagger-ui.always-include=true
```

## Ejecutar en local

Desde la carpeta `trackademy-backend`:

```bash
.\mvnw.cmd -DskipTests compile
.\mvnw.cmd quarkus:dev
```

App: `http://localhost:8080`

Swagger UI: `http://localhost:8080/q/swagger-ui`

## Integración con el proyecto de extracción

El esquema principal y la ingesta de sílabos viven en `extraer-pdf`.

Flujo recomendado:

1. Ejecutar `db_setup.py` y `load_json_to_db.py` en `extraer-pdf`.
2. Levantar este backend apuntando al mismo Postgres.
3. Consumir endpoints de catálogo y onboarding desde frontend.

## Notas

- `GET /api/v1/catalog/periodos` actualmente devuelve solo periodos con `fecha_inicio` y `fecha_fin` no nulos.
- `periodo_evento` almacena hitos institucionales (inicio de clases, finales, rezagados, retiros, etc.).
- El uso de `email` en endpoints `/me` es temporal hasta incorporar JWT.
