#!/usr/bin/env bash

set -euo pipefail

REPO_DIR="/opt/trackademy/trackademy-backend"
BRANCH="${1:-main}"
MAVEN_REPO="/opt/trackademy/.m2/repository"
SERVICE_NAME="trackademy-backend"

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

echo "Restarting systemd service..."
sudo systemctl restart "$SERVICE_NAME"

echo "Checking service status..."
sudo systemctl is-active --quiet "$SERVICE_NAME"
sudo systemctl status "$SERVICE_NAME" --no-pager

echo "Deploy finished successfully."
