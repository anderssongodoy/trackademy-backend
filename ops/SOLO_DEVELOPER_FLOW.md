# Solo Developer Flow

Guia recomendada para trabajar solo en Trackademy sin complicarte demasiado, pero manteniendo un flujo profesional.

## Idea principal

- `main` representa produccion
- no trabajar directo sobre `main`
- trabajar en ramas `feature/*`
- abrir Pull Request hacia `main`
- dejar que CI valide el cambio
- hacer merge a `main`
- GitHub Actions despliega automaticamente

## Flujo recomendado

### 1. Crear una rama para el cambio

```bash
git checkout main
git pull origin main
git checkout -b feature/nombre-del-cambio
```

Ejemplos:

- `feature/auth-google-fix`
- `feature/calendar-endpoint`
- `feature/docker-healthcheck`

### 2. Trabajar normalmente

Haz tus cambios, prueba localmente y commitea.

```bash
git add .
git commit -m "Describe el cambio"
```

### 3. Subir la rama

```bash
git push -u origin feature/nombre-del-cambio
```

### 4. Abrir Pull Request a `main`

En GitHub:

- base: `main`
- compare: tu rama `feature/*`

### 5. Esperar CI verde

El Pull Request debe correr:

- tests

Si falla:

- corriges en tu rama
- haces push otra vez

### 6. Hacer merge a `main`

Cuando CI este verde:

- haces merge del PR

### 7. Deploy automatico

Al llegar a `main`, GitHub Actions hace:

- test
- deploy a produccion

## Lo que no recomiendo para ti ahora

- approvals obligatorios
- varios ambientes complejos
- burocracia extra si sigues siendo solo developer

## Lo que si recomiendo

Ver [BRANCH_PROTECTION_SETUP.md](./BRANCH_PROTECTION_SETUP.md) para la configuracion detallada de branch protection en GitHub.

## Cuándo si trabajar directo en `main`

Solo en casos puntuales:

- hotfix urgente
- cambios muy pequenos y controlados
- mantenimiento operativo

Incluso ahi, sigue siendo mejor usar una rama corta y mergear.

## Resumen practico

Tu flujo ideal ahora es:

1. `feature/*`
2. Pull Request
3. CI verde
4. merge a `main`
5. deploy automatico

- usa este documento como referencia rapida al abrir PRs y para validar branch protection ver [BRANCH_PROTECTION_SETUP.md](./BRANCH_PROTECTION_SETUP.md).
