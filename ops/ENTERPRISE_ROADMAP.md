# Trackademy Enterprise Roadmap

Roadmap detallado para llevar el backend de Trackademy a un flujo mas cercano a entorno empresarial, manteniendo el VPS actual en Oracle Cloud.

## Fase 1: Dockerizar Trackademy

Objetivo:

- mover el backend desde `systemd + java -jar` a un contenedor Docker

Tareas:

- crear `Dockerfile` para `trackademy-backend`
- crear `docker-compose.yml` de produccion para el backend
- conectar el backend contenedorizado a PostgreSQL
- mantener `nginx` apuntando al backend
- ordenar variables de entorno de produccion
- agregar limites de CPU y memoria al contenedor
- agregar `restart: unless-stopped`
- agregar `healthcheck`
- validar que `api.trackademy.trinitylabs.app` siga funcionando

Resultado esperado:

- backend portable y reproducible
- mejor historia para demo y entrevistas
- base mas limpia para rollback y despliegue profesional

## Fase 2: Mejorar CI/CD

Objetivo:

- pasar de deploy automatico basico a deploy mas robusto

Tareas:

- mantener tests automaticos en GitHub Actions
- construir el backend o la imagen Docker en pipeline
- desplegar usando Docker en el VPS
- ejecutar health check despues del deploy
- hacer rollback si falla el health check
- guardar logs de deploy
- opcional: separar workflow de `CI` y `CD`
- opcional: agregar aprobacion manual para produccion

Resultado esperado:

- pipeline mas robusto
- menos riesgo en produccion
- mejor narrativa de CI/CD para presentacion

## Fase 3: Branching y control de cambios

Objetivo:

- adoptar un flujo mas cercano al de empresa

Tareas:

- proteger rama `main`
- obligar Pull Request
- exigir CI verde antes de merge
- opcional: exigir aprobacion
- usar ramas `feature/*`
- desplegar produccion solo desde `main`

Resultado esperado:

- menos cambios directos a produccion
- mejor control de calidad

## Fase 4: Entorno staging

Objetivo:

- tener un entorno previo a produccion

Tareas:

- crear un subdominio de staging
- levantar una instancia staging del backend
- opcional: usar base de datos separada
- configurar `nginx` para staging
- definir flujo de despliegue a staging

Resultado esperado:

- validar antes de tocar produccion
- flujo mas cercano a empresa

## Fase 6: Seguridad y calidad

Objetivo:

- agregar controles mas empresariales de seguridad y calidad

Tareas:

- agregar `CodeQL`
- evaluar despues `SonarQube` o `SonarCloud`
- agregar `Dependabot`
- revisar secretos y variables sensibles
- agregar `Fail2ban`

Resultado esperado:

- mejor postura de seguridad
- mejor valor curricular

## Fase 7: Nginx mas pro

Objetivo:

- aprovechar mejor `nginx` como gateway

Tareas:

- mantener proxy para API, `n8n` y `Netdata`
- agregar headers de seguridad
- agregar auth basica a paneles sensibles si conviene
- opcional: rate limiting
- opcional: cache o compresion segun necesidad

Resultado esperado:

- capa frontal mas profesional
- mejor seguridad y control

## Fase 8: Presentacion para universidad

Objetivo:

- mostrar una arquitectura clara, moderna y entendible

Que mostrar:

- GitHub como origen de cambios
- GitHub Actions como CI/CD
- Docker para backend
- PostgreSQL
- `nginx` como reverse proxy
- dominio y HTTPS
- monitoreo
- rollback y health checks

Flujo ideal para explicar:

1. Se hace push a GitHub
2. CI corre tests
3. Se construye el backend o la imagen
4. CD despliega al VPS
5. `nginx` sigue exponiendo la API
6. Se verifica salud del servicio
7. Si falla, se puede revertir

## Fase 5: Observabilidad

Objetivo:

- monitorear el backend y el VPS de forma operativa y luego empresarial

Tareas:

- seguir usando `Netdata`
- aprender CPU, RAM, disco, red y procesos
- monitorear contenedor del backend cuando pase a Docker
- instalar despues `Uptime Kuma`
- migrar luego a `Prometheus + Grafana`
- definir metricas clave:
  - CPU
  - RAM
  - latencia
  - reinicios
  - disco
  - estado de contenedores

Resultado esperado:

- mejor visibilidad operativa
- mejor demo y mejor operacion

## Orden recomendado

1. Dockerizar `Trackademy`
2. Adaptar deploy a Docker
3. Agregar health check y rollback
4. Proteger `main`
5. Agregar `CodeQL`
6. Instalar `Fail2ban`
7. Instalar `Uptime Kuma`
8. Pasar a `Prometheus + Grafana`
9. Crear staging
10. Evaluar `SonarQube`

## Estado actual

Ya hecho:

- `n8n` con dominio y HTTPS
- `Netdata` publicado con `nginx` y HTTPS
- documentacion base del VPS
- CI/CD basico funcionando para `Trackademy`
- tests automaticos
- deploy automatico al VPS
- acceso SSH dedicado para GitHub Actions

Siguiente foco:

- Fase 1: Dockerizar `Trackademy`
