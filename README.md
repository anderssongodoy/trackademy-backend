# Trackademy Backend

Backend de Trackademy construido con Quarkus, Java 21 y PostgreSQL bajo una arquitectura hexagonal orientada a mantener clara la separación entre HTTP, aplicación, dominio y persistencia.

## Resumen

El backend expone la base funcional del producto:

- autenticación y sesión
- onboarding académico
- catálogo académico
- periodo actual del usuario
- dashboard
- cursos, horarios y calendario
- evaluaciones y registro de notas
- actualización de perfil académico
- reconfiguración del ciclo actual

El objetivo no es solo servir pantallas, sino mantener una base consistente para evolución posterior hacia automatización, analítica y sincronizaciones externas.

## Stack

- Java 21
- Quarkus 3.32.x
- Maven Wrapper
- PostgreSQL
- Hibernate ORM + Panache
- Flyway
- SmallRye OpenAPI

## Arquitectura

Capas principales:

- `adapter/in/rest`: recursos HTTP y DTOs
- `application/port/in`: casos de uso expuestos
- `application/port/out`: contratos hacia persistencia o servicios externos
- `application/service`: implementación de casos de uso
- `adapter/out/persistence`: acceso a PostgreSQL
- `adapter/out/auth`: autenticación e identidades
- `domain/model`: modelos de dominio

Principio base:

- los recursos HTTP no contienen lógica de negocio
- la aplicación orquesta
- el dominio define contratos y modelos
- persistencia y auth quedan encapsulados como adaptadores

## Requisitos

- Java 21
- PostgreSQL disponible
- variables de entorno del datasource configuradas

## Configuración

Archivo base:

- `src/main/resources/application.properties`

Variables importantes:

- `QUARKUS_DATASOURCE_JDBC_URL`
- `QUARKUS_DATASOURCE_USERNAME`
- `QUARKUS_DATASOURCE_PASSWORD`
- `QUARKUS_HTTP_CORS`
- `QUARKUS_HTTP_CORS_ORIGINS`
- `APP_AUTH_MICROSOFT_FRONTEND_CLIENT_ID`
- `APP_AUTH_GOOGLE_FRONTEND_CLIENT_ID`
- `APP_AUTH_JWT_SECRET`

Ejemplo local en PowerShell:

```powershell
$env:QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://127.0.0.1:5432/trackademy_bd"
$env:QUARKUS_DATASOURCE_USERNAME="postgres"
$env:QUARKUS_DATASOURCE_PASSWORD="123"
.\mvnw.cmd quarkus:dev
```

## Ejecución local

Compilación rápida:

```powershell
cd C:\Users\uu\Desktop\trackademy\trackademy-backend
.\mvnw.cmd -DskipTests compile
```

Modo desarrollo:

```powershell
cd C:\Users\uu\Desktop\trackademy\trackademy-backend
.\mvnw.cmd quarkus:dev
```

URLs:

- app: `http://localhost:8080`
- swagger: `http://localhost:8080/q/swagger-ui`

## Endpoints principales

### Health

- `GET /health`

### Auth

- `POST /api/v1/auth/google`
- `POST /api/v1/auth/microsoft`
- `GET /api/v1/auth/session`

### Catálogo

- `GET /api/v1/catalog/campuses`
- `GET /api/v1/catalog/carreras`
- `GET /api/v1/catalog/periodos`
- `GET /api/v1/catalog/periodos/{periodoId}/eventos`
- `GET /api/v1/catalog/cursos`
- `GET /api/v1/catalog/cursos/{codigo}`
- `GET /api/v1/catalog/cursos/{codigo}/detalle`

### Onboarding

- `POST /api/v1/onboarding/basic`

### Me

- `GET /api/v1/me/periodo-actual`
- `PUT /api/v1/me/periodo-actual`
- `PUT /api/v1/me/periodo-actual/configuracion`
- `GET /api/v1/me/dashboard`
- `GET /api/v1/me/cursos`
- `PUT /api/v1/me/cursos/{usuarioPeriodoCursoId}`
- `GET /api/v1/me/horarios`
- `PUT /api/v1/me/cursos/{usuarioPeriodoCursoId}/horarios`
- `GET /api/v1/me/evaluaciones`
- `PUT /api/v1/me/cursos/{usuarioPeriodoCursoId}/evaluaciones/{evaluacionCodigo}/nota`
- `GET /api/v1/me/calendario`

## Estado actual del backend

Hoy el backend ya soporta:

- lectura del periodo actual
- dashboard resumido
- listado de cursos del periodo
- horarios del usuario
- calendario mezclando eventos institucionales, evaluaciones y clases
- evaluaciones del usuario
- registro y actualización de notas
- edición de sección y profesor del curso
- actualización de objetivos del perfil académico
- reconfiguración del ciclo actual con sincronización de cursos

## Validación recomendada

Compilación:

```powershell
cd C:\Users\uu\Desktop\trackademy\trackademy-backend
.\mvnw.cmd -DskipTests compile
```

Si quieres desarrollo interactivo:

```powershell
cd C:\Users\uu\Desktop\trackademy\trackademy-backend
.\mvnw.cmd quarkus:dev
```

## Base de datos

El modelo real está alineado con:

- usuario
- usuario_periodo
- usuario_periodo_curso
- usuario_periodo_curso_horario
- usuario_periodo_evaluacion
- periodo_evento
- tablas de sílabo y catálogo

Además existen tablas preparadas para evolución futura, por ejemplo:

- `usuario_tarea`
- `usuario_nota_manual`
- `agenda_evento`
- `recordatorio_regla`
- `recordatorio_evento`
- `calendar_sync_account`

## Decisiones relevantes

- reconfigurar ciclo puede eliminar cursos del periodo actual y con eso borrar sus horarios y notas asociadas
- el calendario mezcla tres orígenes: `periodo`, `evaluacion` y `horario`
- `seccion` y `profesor` se editan sobre `usuario_periodo_curso`
- `ubicacion` y `url_virtual` viven a nivel de sesión en `usuario_periodo_curso_horario`
- el backend devuelve data suficiente para que frontend no tenga que inventar contexto después de guardar notas

## Producción

Build:

```bash
cd /opt/trackademy/trackademy-backend
set -a
source .env.prod
set +a
./mvnw -Dmaven.repo.local=/opt/trackademy/.m2/repository clean package -DskipTests
```

Servicio:

```bash
sudo systemctl restart trackademy-backend
sudo systemctl status trackademy-backend --no-pager
```

Logs:

```bash
journalctl -u trackademy-backend -n 100 --no-pager
journalctl -u trackademy-backend -f
```

## CORS

Variables típicas:

```text
QUARKUS_HTTP_CORS=true
QUARKUS_HTTP_CORS_ORIGINS=https://trackademy.trinitylabs.app
QUARKUS_HTTP_CORS_HEADERS=accept,authorization,content-type,x-requested-with
QUARKUS_HTTP_CORS_METHODS=GET,POST,PUT,DELETE,OPTIONS
```

## Troubleshooting

### `compile` falla por dependencias o `.lastUpdated`

Eso suele ser cache local de Maven incompleta. La carpeta `.m2/` local del backend no forma parte del proyecto y está ignorada por Git.

### El backend compila con warnings

Hoy hay warnings conocidos no bloqueantes:

- advertencia de reubicación de `quarkus-junit5`
- uso deprecated en `GoogleIdentityAdapter`
- warning de generics en `CursoPanacheRepository`

No impiden el runtime, pero conviene resolverlos en una pasada técnica posterior.

### El frontend guarda una nota y la UI queda rara

Eso ya quedó corregido: el backend devuelve `codigoCurso` y `nombreCurso` al registrar notas.

## Próximas mejoras pensadas

- sincronización real con calendarios externos
- reglas de recordatorios persistentes
- soporte formal para tareas manuales del alumno
- soporte formal para notas manuales o proyecciones
- pruebas automáticas más completas
- limpieza de warnings no bloqueantes del build
