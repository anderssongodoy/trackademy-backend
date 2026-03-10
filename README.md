# Trackademy Backend

Backend de Trackademy construido con **Quarkus + Java 21 + PostgreSQL** y arquitectura **hexagonal** (puertos y adaptadores). El objetivo es servir onboarding académico, catálogo de cursos/sílabos y autenticación básica, con una base sólida para evolucionar a analítica.

## Arquitectura

### Hexagonal (código)

- `adapter/in/rest`: controladores HTTP y DTOs.
- `application/port/in`: casos de uso (input ports).
- `application/port/out`: contratos hacia persistencia o servicios externos.
- `application/service`: implementación de casos de uso.
- `adapter/out/persistence`: adaptadores a PostgreSQL (JPA/Panache).
- `adapter/out/auth`: adaptadores de autenticación (JWT/Google/Microsoft).
- `domain/model`: modelos puros de dominio.

### Despliegue (infra)

```
Frontend -> Nginx -> Quarkus (systemd) -> PostgreSQL (Docker, localhost)
```

- **Nginx** expone HTTPS.
- **Quarkus** escucha en `127.0.0.1:8080`.
- **PostgreSQL** solo en `127.0.0.1:5432`.

## Stack

- Java 21
- Quarkus 3.x
- Maven Wrapper
- PostgreSQL 16
- Hibernate ORM + Panache
- OpenAPI / Swagger UI

## Endpoints (API)

### Health
- `GET /health`

### Catálogo de cursos
- `GET /api/v1/catalog/cursos`
- `GET /api/v1/catalog/cursos/{codigo}`
- `GET /api/v1/catalog/cursos/{codigo}/detalle`

### Catálogo académico
- `GET /api/v1/catalog/campuses?universidadId=...`
- `GET /api/v1/catalog/carreras?universidadId=...`
- `GET /api/v1/catalog/periodos?universidadId=...`
- `GET /api/v1/catalog/periodos/{periodoId}/eventos`

### Onboarding
- `POST /api/v1/onboarding/basic`

### Mi cuenta
- `GET /api/v1/me/periodo-actual` (requiere `Authorization: Bearer ...`)
- `GET /api/v1/me/cursos` (requiere `Authorization: Bearer ...`)

### Auth
- `POST /api/v1/auth/google`
- `POST /api/v1/auth/microsoft`
- `GET /api/v1/auth/session`

## Configuración

Archivo base: `src/main/resources/application.properties`.

Variables de entorno principales:

- `QUARKUS_DATASOURCE_JDBC_URL`
- `QUARKUS_DATASOURCE_USERNAME`
- `QUARKUS_DATASOURCE_PASSWORD`
- `QUARKUS_HTTP_CORS`
- `QUARKUS_HTTP_CORS_ORIGINS`
- `APP_AUTH_MICROSOFT_FRONTEND_CLIENT_ID`
- `APP_AUTH_GOOGLE_FRONTEND_CLIENT_ID`
- `APP_AUTH_JWT_SECRET`

Ejemplo local (PowerShell):

```powershell
$env:QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://127.0.0.1:5432/trackademy_bd"
$env:QUARKUS_DATASOURCE_USERNAME="postgres"
$env:QUARKUS_DATASOURCE_PASSWORD="123"
.\mvnw.cmd quarkus:dev
```

## Ejecución local

```bash
./mvnw -DskipTests compile
./mvnw quarkus:dev
```

App: `http://localhost:8080`  
Swagger: `http://localhost:8080/q/swagger-ui`

## Producción (VPS)

### Build

```bash
cd /opt/trackademy/trackademy-backend
set -a
source .env.prod
set +a
./mvnw -Dmaven.repo.local=/opt/trackademy/.m2/repository clean package -DskipTests
```

### Servicio (systemd)

```bash
sudo systemctl restart trackademy-backend
sudo systemctl status trackademy-backend --no-pager
```

### Logs

```bash
journalctl -u trackademy-backend -n 100 --no-pager
journalctl -u trackademy-backend -f
```

## CORS

El backend controla CORS. Configura:

```
QUARKUS_HTTP_CORS=true
QUARKUS_HTTP_CORS_ORIGINS=https://trackademy.trinitylabs.app
QUARKUS_HTTP_CORS_HEADERS=accept,authorization,content-type,x-requested-with
QUARKUS_HTTP_CORS_METHODS=GET,POST,PUT,DELETE,OPTIONS
```

Prueba preflight:

```bash
curl -i -X OPTIONS https://api.trackademy.trinitylabs.app/api/v1/auth/google \
  -H "Origin: https://trackademy.trinitylabs.app" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: content-type,authorization"
```

## Base de datos

Postgres corre en Docker y solo expone `127.0.0.1:5432`.

Conectar por psql:

```bash
sudo docker exec -it trackademy-postgres psql -U trackademy -d trackademy_bd
```

## pgAdmin desde PC (túnel SSH)

```bash
ssh -L 55432:127.0.0.1:5432 oracle-trackademy
```

En pgAdmin:
- Host: `127.0.0.1`
- Port: `55432`
- Database: `trackademy_bd`
- User: `trackademy`

## Backups

Script:

```
/opt/trackademy/scripts/backup_pg.sh
```

Ejecutar manual:

```bash
/opt/trackademy/scripts/backup_pg.sh
ls -lh /opt/trackademy/backups
```

## Checklist antes de modificar

- Ver estado de servicios:
  ```bash
  sudo systemctl status trackademy-backend --no-pager
  sudo systemctl status nginx --no-pager
  sudo systemctl status certbot.timer --no-pager
  ```
- Verificar backups recientes:
  ```bash
  ls -lh /opt/trackademy/backups | tail -n 3
  ```
- Revisar puertos expuestos:
  ```bash
  sudo ss -tulpn | rg ':5432|:8080|:80|:443'
  ```

## Notas

- `GET /api/v1/catalog/periodos` devuelve solo periodos con `fecha_inicio` y `fecha_fin` no nulos.
- `periodo_evento` almacena hitos institucionales (inicio de clases, finales, rezagados, retiros, etc.).
