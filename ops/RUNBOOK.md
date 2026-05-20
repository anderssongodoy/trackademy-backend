# Trackademy Runbook

Operaciones de produccion en VPS Oracle (Ubuntu ARM, Docker, Nginx).

## Servicios

Ver estado del backend:

```bash
docker ps | grep trackademy-backend
```

Ver estado de Nginx:

```bash
sudo systemctl status nginx --no-pager
```

Ver renovacion automatica de SSL:

```bash
sudo systemctl status certbot.timer --no-pager
```

## Reinicios

Reiniciar backend:

```bash
docker restart trackademy-backend
```

Reiniciar Nginx:

```bash
sudo systemctl restart nginx
```

Recargar Nginx sin cortar conexiones:

```bash
sudo systemctl reload nginx
```

## Logs

Logs recientes del backend:

```bash
docker logs trackademy-backend --tail 100
```

Seguir logs del backend en tiempo real:

```bash
docker logs trackademy-backend -f
```

Logs de Nginx:

```bash
sudo journalctl -u nginx -n 100 --no-pager
sudo tail -n 100 /var/log/nginx/access.log
sudo tail -n 100 /var/log/nginx/error.log
```

## Healthchecks

Health local del backend:

```bash
curl -I http://127.0.0.1:8080/health
```

Health publico:

```bash
curl -I https://api.trackademy.trinitylabs.app/health
```

Catalogo de periodos:

```bash
curl https://api.trackademy.trinitylabs.app/api/v1/catalog/periodos
```

## Deploy backend

Compilar y desplegar con Docker Compose:

```bash
cd /opt/trackademy
docker compose -f trackademy-backend/ops/docker-compose.prod.yml \
  --env-file trackademy-backend/.env.prod \
  up -d --build --force-recreate trackademy-backend
```

El despliegue tambien se hace solo cuando hay push a `main` (ver `.github/workflows/deploy-production.yml`, que invoca `ops/deploy-prod.sh`).

## Actualizar variables de entorno

1. Editar el archivo:

```bash
vim /opt/trackademy/trackademy-backend/.env.prod
```

2. Aplicar cambios (rebuild):

```bash
cd /opt/trackademy
docker compose -f trackademy-backend/ops/docker-compose.prod.yml \
  --env-file trackademy-backend/.env.prod \
  up -d --build --force-recreate trackademy-backend
```

3. O recarga rapida sin recompilar:

```bash
docker restart trackademy-backend
```

4. Verificar:

```bash
docker logs trackademy-backend -f
```

## Secretos

Permisos correctos para archivos de entorno:

```bash
chmod 600 /opt/trackademy/trackademy-backend/.env.prod
```

## Base de datos

Ver contenedor de Postgres:

```bash
docker ps
```

Entrar a psql:

```bash
sudo docker exec -it trackademy-postgres psql -U trackademy -d trackademy_bd
```

Conteos basicos:

```bash
sudo docker exec trackademy-postgres psql -U trackademy -d trackademy_bd -c "select count(*) from curso;"
sudo docker exec trackademy-postgres psql -U trackademy -d trackademy_bd -c "select count(*) from silabo;"
sudo docker exec trackademy-postgres psql -U trackademy -d trackademy_bd -c "select count(*) from silabo_cronograma_sesion;"
```

## pgAdmin por tunel SSH

Abrir tunel desde la maquina local:

```bash
ssh -L 55432:127.0.0.1:5432 oracle-trackademy
```

Configurar pgAdmin con:

```text
Host: 127.0.0.1
Port: 55432
Database: trackademy_bd
Username: trackademy
Password: usar la credencial real de Postgres
```

## Backups

Ejecutar backup manual:

```bash
/opt/trackademy/scripts/backup_pg.sh
```

Listar backups:

```bash
ls -lh /opt/trackademy/backups
```

Restore en base temporal:

```bash
gunzip -c /opt/trackademy/backups/trackademy_YYYYMMDD_HHMMSS.sql.gz | \
  sudo docker exec -i trackademy-postgres psql -U trackademy -d trackademy_bd
```

## Red y puertos

Verificar puertos expuestos:

```bash
sudo ss -tulpn | rg ':5432|:8080|:80|:443'
```

Esperado:

```text
127.0.0.1:5432  -> Postgres
127.0.0.1:8080  -> Quarkus
0.0.0.0:80      -> Nginx
0.0.0.0:443     -> Nginx
```
