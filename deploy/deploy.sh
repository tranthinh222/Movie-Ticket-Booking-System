#!/usr/bin/env bash
# Runs on the VPS as the deploy user (GitHub Actions pipes it over SSH).
# Points /opt/ticket-booking/app.jar at the new release, restarts the systemd service,
# and rolls back to the previous JAR if the new one never becomes healthy.
# Usage: deploy.sh /opt/ticket-booking/releases/app-<sha>.jar
set -euo pipefail

NEW_JAR="${1:?usage: deploy.sh <path-to-jar>}"
APP_DIR=/opt/ticket-booking
CURRENT="$APP_DIR/app.jar"
SERVICE=ticket-booking
HEALTH_URL="${HEALTH_URL:-http://127.0.0.1:8081/actuator/health/readiness}"
KEEP_RELEASES=5

[ -f "$NEW_JAR" ] || { echo ">> $NEW_JAR not found" >&2; exit 1; }
PREV_JAR="$(readlink -f "$CURRENT" 2>/dev/null || true)"

wait_healthy() {
  for _ in $(seq 1 36); do
    if curl -fsS -o /dev/null "$HEALTH_URL"; then
      return 0
    fi
    # Stop waiting early if systemd has already given up on the service
    if systemctl is-failed --quiet "$SERVICE"; then
      return 1
    fi
    sleep 5
  done
  return 1
}

release() {
  ln -sfn "$1" "$CURRENT"
  sudo /usr/bin/systemctl restart "$SERVICE"
  wait_healthy
}

echo ">> Deploying $(basename "$NEW_JAR") (current: ${PREV_JAR:+$(basename "$PREV_JAR")})"

if release "$NEW_JAR"; then
  # Keep only the newest releases
  ls -1t "$APP_DIR"/releases/app-*.jar | tail -n +$((KEEP_RELEASES + 1)) | xargs -r rm -f --
  echo ">> $(basename "$NEW_JAR") is healthy"
  exit 0
fi

echo ">> $(basename "$NEW_JAR") failed its health check. Last logs:"
journalctl -u "$SERVICE" -n 100 --no-pager || true

if [ -n "$PREV_JAR" ] && [ -f "$PREV_JAR" ] && [ "$PREV_JAR" != "$NEW_JAR" ]; then
  echo ">> Rolling back to $(basename "$PREV_JAR")"
  release "$PREV_JAR"
  echo ">> Rolled back to $(basename "$PREV_JAR")"
fi

exit 1
