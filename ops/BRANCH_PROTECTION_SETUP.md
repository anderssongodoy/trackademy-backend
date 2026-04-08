# Branch Protection Setup

Configuracion recomendada para `main` en un escenario solo developer, pero con flujo cercano a empresa.

## Objetivo

- trabajar en ramas `feature/*`
- abrir Pull Request hacia `main`
- ejecutar CI en el Pull Request
- desplegar solo cuando el cambio llegue a `main`

## Estado del workflow

El workflow actual ya quedo preparado para esto:

- en `pull_request` hacia `main`: corre `test`
- en `push` a `main`: corre `test` y luego `deploy`
- en `workflow_dispatch`: permite ejecucion manual

## Configuracion recomendada en GitHub

Ruta:

- `Settings`
- `Branches`
- `Add rule`

Branch name pattern:

- `main`

Activa estas opciones:

- `Require a pull request before merging`
- `Require status checks to pass before merging`
- seleccionar el check del workflow de CI
- `Require branches to be up to date before merging`
- `Do not allow bypassing the above settings`

Opcional por ahora:

- `Require approvals`

Recomendacion si trabajas solo:

- no obligar approvals por ahora
- si usar Pull Request y checks obligatorios

## Flujo diario recomendado

1. crear rama nueva

```bash
git checkout -b feature/nombre-del-cambio
```

2. trabajar y hacer commits

3. subir la rama

```bash
git push -u origin feature/nombre-del-cambio
```

4. abrir Pull Request a `main`

5. esperar CI verde

6. hacer merge a `main`

7. GitHub Actions despliega automaticamente

## Notas

- evita trabajar directo en `main`
- `main` debe representar produccion estable
- aunque trabajes solo, este flujo te ayuda a no romper produccion por accidente
