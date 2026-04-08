#!/usr/bin/env bash

set -euo pipefail

REPO_DIR="/opt/trackademy/trackademy-backend"
BRANCH="${1:-main}"
MAVEN_REPO="/opt/trackademy/.m2/repository"
SYSTEMD_SERVICE_NAME="trackademy-backend"
COMPOSE_FILE="ops/docker-compose.prod.yml"
DOCKER_SERVICE_NAME="trackademy-backend"
HEALTH_URL="http://127.0.0.1:8080/health"
HEALTH_RETRIES=20
HEALTH_SLEEP_SECONDS=3

cd "$REPO_DIR"

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Deploy aborted: the repository has uncommitted changes."
  exit 1
fi

CURRENT_BRANCH="$(git rev-parse --abbrev-ref HEAD)"
if [[ "$CURRENT_BRANCH" != "$BRANCH" ]]; then
  echo "Deploy aborted: current branch is '$CURRENT_BRANCH', expected '$BRANCH'."
  exit 1
fi

echo "Fetching latest changes from origin/$BRANCH..."
git fetch origin "$BRANCH"

LOCAL_HEAD="$(git rev-parse HEAD)"
REMOTE_HEAD="$(git rev-parse "origin/$BRANCH")"

if [[ "$LOCAL_HEAD" == "$REMOTE_HEAD" ]]; then
  echo "Repository already up to date."
else
  echo "Pulling latest changes..."
  git pull --ff-only origin "$BRANCH"
fi

echo "Building application..."
./mvnw -Dmaven.repo.local="$MAVEN_REPO" clean package -DskipTests

echo "Validating Docker Compose configuration..."
docker compose -f "$COMPOSE_FILE" config >/dev/null

PREVIOUS_IMAGE_ID="$(docker image inspect trackademy-backend:prod --format '{{.Id}}' 2>/dev/null || true)"
SYSTEMD_WAS_ACTIVE="false"

if sudo systemctl is-active --quiet "$SYSTEMD_SERVICE_NAME"; then
  SYSTEMD_WAS_ACTIVE="true"
fi

if [[ -n "$PREVIOUS_IMAGE_ID" ]]; then
  echo "Saving rollback image tag..."
  docker image tag "$PREVIOUS_IMAGE_ID" trackademy-backend:rollback
fi

wait_for_health() {
  local url="$1"
  local retries="$2"
  local delay="$3"

  for attempt in $(seq 1 "$retries"); do
    if curl -fsS "$url" >/dev/null; then
      return 0
    fi
    echo "Health check attempt $attempt/$retries failed, retrying in ${delay}s..."
    sleep "$delay"
  done

  return 1
}

rollback() {
  echo "Deployment failed. Starting rollback..."
  docker compose -f "$COMPOSE_FILE" logs --tail 100 || true
  docker compose -f "$COMPOSE_FILE" down || true

  if docker image inspect trackademy-backend:rollback >/dev/null 2>&1; then
    echo "Restoring previous Docker image..."
    docker image tag trackademy-backend:rollback trackademy-backend:prod
    docker compose -f "$COMPOSE_FILE" up -d --force-recreate "$DOCKER_SERVICE_NAME"

    if wait_for_health "$HEALTH_URL" 10 "$HEALTH_SLEEP_SECONDS"; then
      echo "Rollback to previous Docker image succeeded."
    else
      echo "Rollback Docker image did not become healthy."
    fi
  elif [[ "$SYSTEMD_WAS_ACTIVE" == "true" ]]; then
    echo "Restoring previous systemd service..."
    sudo systemctl start "$SYSTEMD_SERVICE_NAME"
    sudo systemctl enable "$SYSTEMD_SERVICE_NAME" >/dev/null 2>&1 || true
  else
    echo "No Docker rollback image and no previous systemd service state to restore."
  fi
}

trap 'rollback' ERR

echo "Building Docker image..."
docker compose -f "$COMPOSE_FILE" build "$DOCKER_SERVICE_NAME"

if [[ "$SYSTEMD_WAS_ACTIVE" == "true" ]]; then
  echo "Stopping previous systemd service..."
  sudo systemctl stop "$SYSTEMD_SERVICE_NAME"
fi

echo "Starting backend container..."
docker compose -f "$COMPOSE_FILE" up -d --force-recreate "$DOCKER_SERVICE_NAME"

echo "Waiting for application health check..."
wait_for_health "$HEALTH_URL" "$HEALTH_RETRIES" "$HEALTH_SLEEP_SECONDS"

echo "Container is healthy."
docker compose -f "$COMPOSE_FILE" ps

if [[ "$SYSTEMD_WAS_ACTIVE" == "true" ]]; then
  echo "Disabling previous systemd service to avoid future port conflicts..."
  sudo systemctl disable "$SYSTEMD_SERVICE_NAME"
fi

trap - ERR

echo "Deploy finished successfully."
