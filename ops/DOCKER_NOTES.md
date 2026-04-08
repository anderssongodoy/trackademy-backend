# Docker Notes

Archivos base para mover Trackademy Backend a contenedor.

## Archivos

- `Dockerfile`
- `ops/docker-compose.prod.yml`

## Estrategia

- el backend seguira expuesto en `127.0.0.1:8080`
- `nginx` puede seguir apuntando al mismo puerto
- el contenedor usa `.env.prod`
- la conexion a PostgreSQL se hace por `host.docker.internal`
- se agregan limites de CPU y memoria
- se agrega health check a `/health`

## Cambio operativo esperado

Antes:

- `systemd`
- `java -jar`

Despues:

- `docker compose`
- backend en contenedor

## Pendiente antes del cambio definitivo

- compilar `target/quarkus-app`
- construir imagen
- probar `docker compose up`
- ajustar deploy para usar contenedor
- definir rollback del contenedor
