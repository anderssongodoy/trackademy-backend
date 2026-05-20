# Trackademy Backend

Backend de Trackademy construido con Quarkus, Java 21 y PostgreSQL bajo arquitectura hexagonal.

## Stack

- Java 21
- Quarkus 3.32
- Maven Wrapper
- PostgreSQL + Flyway + Hibernate ORM (Panache)
- JWT propio + validacion de tokens Google y Microsoft
- SmallRye OpenAPI
- Quarkus Mailer (Hostinger SMTP)
- Docker (produccion)

## Arquitectura

Capas (hexagonal):

- `adapter/in/rest`: recursos HTTP, DTOs y filtros
- `application/port/in` y `application/port/out`: contratos de casos de uso y de salida
- `application/service`: implementacion de casos de uso
- `adapter/out/persistence`: PostgreSQL via Panache
- `adapter/out/auth`: Google, Microsoft, JWT
- `adapter/out/whatsapp`: Meta Graph API
- `adapter/out/mail`: envio de correos
- `domain/model`: modelos de dominio

Los resources HTTP no contienen logica de negocio; orquestan via los servicios de aplicacion.

## Requisitos

- Java 21
- PostgreSQL disponible
- Variables de entorno del datasource configuradas

## Configuracion

Archivo base: `src/main/resources/application.properties` (defaults locales).

Variables relevantes:

```text
QUARKUS_DATASOURCE_JDBC_URL
QUARKUS_DATASOURCE_USERNAME
QUARKUS_DATASOURCE_PASSWORD
QUARKUS_HTTP_CORS
QUARKUS_HTTP_CORS_ORIGINS
APP_AUTH_JWT_SECRET
APP_AUTH_MICROSOFT_FRONTEND_CLIENT_ID
APP_AUTH_GOOGLE_FRONTEND_CLIENT_ID
APP_WHATSAPP_META_ACCESS_TOKEN
APP_WHATSAPP_META_PHONE_NUMBER_ID
APP_WHATSAPP_META_WEBHOOK_VERIFY_TOKEN
APP_WHATSAPP_META_APP_SECRET
APP_WHATSAPP_OFFICIAL_NUMBER
APP_UPLOAD_IMGBB_API_KEY
```

Ver `.env.example` para el listado completo.

## Ejecucion local

```powershell
cd C:\Users\uu\Desktop\trackademy-proyecto\trackademy-backend
$env:QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://127.0.0.1:5432/trackademy_bd"
$env:QUARKUS_DATASOURCE_USERNAME="postgres"
$env:QUARKUS_DATASOURCE_PASSWORD="123"
.\mvnw.cmd quarkus:dev
```

Compilacion rapida sin tests:

```powershell
.\mvnw.cmd -DskipTests compile
```

URLs:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/q/swagger-ui`
- Health: `http://localhost:8080/health`

## Endpoints

### Health
- `GET /health`

### Auth (`/api/v1/auth`)
- `POST /microsoft` — login con id_token de Microsoft
- `POST /google` — login con id_token de Google
- `GET /google/oauth-url` — genera URL de autorizacion server-side
- `GET /google/callback` — callback server-side de Google OAuth
- `GET /session` — devuelve la sesion actual

### Onboarding (`/api/v1/onboarding`)
- `POST /basic` — guarda datos basicos del alumno
- `POST /preview-pdf` — preview de PDF de matricula

### Catalogo academico (`/api/v1/catalog`)
- `GET /campuses`
- `GET /carreras`
- `GET /periodos`
- `GET /periodos/{periodoId}/eventos`

### Catalogo de cursos y silabos (`/api/v1/catalog/cursos`)
- `GET /` — lista (filtros: `carreraId`, `q`)
- `GET /{codigo}` y `GET /public/{publicId}`
- `GET /{codigo}/detalle` y `GET /public/{publicId}/detalle`
- `GET /{codigo}/silabos` y `GET /public/{publicId}/silabos`
- `GET /{codigo}/silabo-vigente` y `GET /public/{publicId}/silabo-vigente`
- `GET /silabos/{silaboId}/pdf` — descarga el PDF

Detalle completo: [ops/FRONTEND_SYLLABUS_API_CONTRACT.md](ops/FRONTEND_SYLLABUS_API_CONTRACT.md).

### Mi cuenta (`/api/v1/me`) — autenticado
Periodo actual y perfil:
- `GET /periodo-actual`, `PUT /periodo-actual`
- `PUT /periodo-actual/personal`
- `PUT /periodo-actual/configuracion`

Vistas agregadas:
- `GET /dashboard`
- `GET /academic-radar`
- `GET /evaluaciones`, `GET /evaluaciones/resumen`
- `GET /horarios`, `GET /calendario`

Cursos del periodo:
- `GET /cursos`
- `PUT /cursos/{usuarioPeriodoCursoId}` — seccion y profesor
- `PUT /cursos/{usuarioPeriodoCursoId}/horarios`
- `PUT /cursos/{usuarioPeriodoCursoId}/evaluaciones/{evaluacionCodigo}/nota`
- `GET /cursos/{usuarioPeriodoCursoId}/silabo/analisis` — analisis IA del silabo

Tareas y recordatorios (CRUD manual):
- `GET /tareas`, `POST /tareas`
- `PUT /tareas/{tareaId}`, `DELETE /tareas/{tareaId}`
- `GET /recordatorios?from=...&to=...`

Calendar sync con Google:
- `GET /calendar-sync-accounts`
- `GET /calendar-sync/google/plan?from=...&to=...`
- `POST /calendar-sync/google/sync`
- `DELETE /calendar-sync/google`

### WhatsApp (`/api/v1/whatsapp`) — autenticado
- `POST /link-code` — genera codigo `TDK-######`
- `GET /link-status`
- `DELETE /link`

### Webhook WhatsApp (`/api/v1/webhooks/whatsapp`) — publico
- `GET` — verificacion de Meta (`hub.mode`, `hub.verify_token`, `hub.challenge`)
- `POST` — eventos entrantes

Comandos soportados: `menu`, `ayuda`, `resumen`, `pendientes`, `hoy`, `cursos`.

Detalle completo: [ops/WHATSAPP_API_CONTRACT.md](ops/WHATSAPP_API_CONTRACT.md).

### Feedback (`/api/v1/feedback`) — autenticado
- `POST /reportes` — recibe reportes de bugs/sugerencias y envia correo

## Base de datos

Migraciones en `src/main/resources/db/migration` (Flyway). El schema base esta en `V20260408_01__initial_schema.sql` mas migraciones incrementales.

Tablas en uso (resumen):

- `usuario`, `auth_identity`
- `campus`, `carrera`, `periodo`, `periodo_evento`
- `curso`, `silabo`, `silabo_unidad`, `silabo_tema`, `silabo_evaluacion`
- `usuario_periodo`, `usuario_periodo_curso`, `usuario_periodo_curso_horario`, `usuario_periodo_evaluacion`
- `usuario_preferencia_estudio`, `usuario_periodo_curso_confianza`
- `usuario_tarea`, `recordatorio_evento`
- `calendar_sync_account`, `calendar_sync_event`
- `whatsapp_link_codes`, `user_whatsapp_links`, `whatsapp_inbound_message`
- `feedback_report`

Tablas presentes en migracion pero **sin uso actual** (deuda):

- `agenda_evento`, `recordatorio_regla`, `usuario_nota_manual`

## Decisiones relevantes

- `tareas` y `recordatorios` son CRUD manuales (no derivados de evaluaciones).
- `recordatorio_evento` se enlaza a `usuario_tarea`; Google Calendar sync proyecta las tareas con `fechaVencimiento`, no crea eventos separados de recordatorio.
- El calendario combina tres origenes: `periodo_evento`, `usuario_periodo_evaluacion` y `usuario_periodo_curso_horario`.
- Reconfigurar ciclo puede eliminar cursos del periodo y con eso sus horarios y notas asociadas.
- `seccion` y `profesor` viven en `usuario_periodo_curso`; `ubicacion` y `url_virtual` en `usuario_periodo_curso_horario`.
- `publicId` (UUID) se debe usar como identificador estable de curso en el frontend; `codigo` es solo dato visible.
- `ProductionConfigValidator` falla en arranque productivo si falta `APP_AUTH_JWT_SECRET` fuerte o configuracion completa de WhatsApp cuando esta activado.
- CORS aplica tanto por propiedades Quarkus como por `CorsFilter`; ambos leen las mismas variables (idempotente).

## Produccion

VPS Oracle (Ubuntu ARM), backend en Docker, Postgres en Docker, Nginx como reverse proxy con HTTPS.

Deploy automatico al hacer push a `main` (GitHub Actions -> SSH -> `ops/deploy-prod.sh`).

Operaciones diarias: [ops/RUNBOOK.md](ops/RUNBOOK.md).
Flujo de desarrollo y branch protection: [ops/DEVELOPMENT.md](ops/DEVELOPMENT.md).
