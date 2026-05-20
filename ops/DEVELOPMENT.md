# Development Workflow

Flujo de trabajo para Trackademy operado como solo developer pero con disciplina de empresa: ramas `feature/*`, Pull Request hacia `main`, CI obligatoria, deploy automatico al VPS.

## Idea principal

- `main` representa produccion
- no se trabaja directo en `main`
- cada cambio entra por una rama `feature/*` y un Pull Request
- CI valida en cada PR
- al hacer merge a `main`, GitHub Actions despliega solo

## Flujo diario

1. Crear rama nueva desde `main`:

   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/nombre-del-cambio
   ```

   Ejemplos: `feature/auth-google-fix`, `feature/calendar-endpoint`, `feature/docker-healthcheck`.

2. Trabajar y commitear:

   ```bash
   git add .
   git commit -m "Describe el cambio"
   ```

3. Subir la rama:

   ```bash
   git push -u origin feature/nombre-del-cambio
   ```

4. Abrir Pull Request a `main` (base: `main`, compare: tu rama).

5. Esperar CI verde. Si falla, corrige en la rama y vuelve a empujar.

6. Hacer merge.

7. GitHub Actions ejecuta el deploy a produccion (ver [RUNBOOK.md](./RUNBOOK.md) para detalle operativo).

## Branch protection en GitHub

Ruta: `Settings` -> `Branches` -> `Add rule`.

Branch name pattern: `main`.

Reglas activas:

- Require a pull request before merging
- Require status checks to pass before merging
  - seleccionar el check del workflow CI (`.github/workflows/ci.yml`)
- Require branches to be up to date before merging
- Do not allow bypassing the above settings

Opcional segun necesidad:

- Require approvals (no obligatorio en solo developer)

## Estado del workflow

- `pull_request` hacia `main`: corre `test` (`.github/workflows/ci.yml`)
- `push` a `main`: corre `test` y luego `deploy` (`.github/workflows/deploy-production.yml`, que invoca `ops/deploy-prod.sh`)
- `workflow_dispatch`: permite ejecucion manual

## Cuando trabajar directo en `main`

Solo en casos puntuales y bajo tu propia responsabilidad:

- hotfix urgente con riesgo controlado
- cambios triviales (typos en docs)

Incluso ahi, una rama corta + merge es mas seguro.
