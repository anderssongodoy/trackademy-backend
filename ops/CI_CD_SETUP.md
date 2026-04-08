# Trackademy Backend CI/CD

Este proyecto ahora queda preparado para despliegue continuo con GitHub Actions.

## Flujo

1. Haces push a `main`
2. GitHub Actions ejecuta tests con Java 21
3. Si los tests pasan, GitHub entra por SSH al VPS
4. El VPS ejecuta `ops/deploy-prod.sh`
5. El script:
   - valida que el repo local este limpio
   - hace `git fetch`
   - hace `git pull --ff-only`
   - compila con Maven
   - reinicia `trackademy-backend`

## Secrets que debes crear en GitHub

En el repositorio `trackademy-backend`, agrega estos secrets en:
`Settings -> Secrets and variables -> Actions`

- `TRACKADEMY_VPS_HOST`
  - ejemplo: `168.129.177.156`

- `TRACKADEMY_VPS_USER`
  - ejemplo: `ubuntu`

- `TRACKADEMY_VPS_SSH_KEY`
  - la clave privada SSH que GitHub Actions usara para entrar al VPS
  - en este servidor se genero una clave dedicada en:
    - privada: `/home/ubuntu/trinitylabs-infra/keys/trackademy_github_actions`
    - publica: `/home/ubuntu/trinitylabs-infra/keys/trackademy_github_actions.pub`
  - debes copiar el contenido completo de la clave privada al secret

## Requisito importante

El usuario usado por GitHub Actions debe poder ejecutar:

```bash
sudo systemctl restart trackademy-backend
sudo systemctl status trackademy-backend --no-pager
```

Si pide password, el workflow va a fallar.

## Comandos utiles

Ver la clave privada para copiarla a GitHub Secrets:

```bash
cat /home/ubuntu/trinitylabs-infra/keys/trackademy_github_actions
```

## Ruta del workflow

`/.github/workflows/deploy-production.yml`

## Script de despliegue

`/ops/deploy-prod.sh`
