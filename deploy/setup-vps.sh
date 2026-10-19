#!/usr/bin/env bash
# One-time setup of the Ubuntu VPS for the backend:
#   - service user "ticketbooking" (the app no longer runs as root), systemd unit, /opt/ticket-booking layout
#   - deploy user that GitHub Actions logs in as (may only restart the service via sudo)
#   - SSH hardening (key-only, no root login), ufw, fail2ban
#
# Adopts an existing install (ticket-booking.service running as root from /opt/ticket-booking):
# reuses its .env and JAR and backs up the old unit. It does NOT restart the running app;
# the switch to the new unit happens on the next `systemctl restart ticket-booking`.
#
# Usage (from a sudo-capable admin account that already logs in with an SSH key):
#   sudo DEPLOY_PUBKEY="ssh-ed25519 AAAA... github-actions" bash setup-vps.sh
set -euo pipefail

DEPLOY_USER="${DEPLOY_USER:-deploy}"
DEPLOY_PUBKEY="${DEPLOY_PUBKEY:?set DEPLOY_PUBKEY to the public key matching the VPS_SSH_KEY secret}"
APP_USER=ticketbooking
APP_DIR=/opt/ticket-booking
ENV_DIR=/etc/ticket-booking
SERVICE=ticket-booking
# Port sshd already listens on (this script does not change it)
SSH_PORT="${SSH_PORT:-22}"
# nginx proxies to the app, so 8081 stays closed unless OPEN_APP_PORT=1
OPEN_APP_PORT="${OPEN_APP_PORT:-0}"
APP_PORT="${APP_PORT:-8081}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

[ "$(id -u)" -eq 0 ] || { echo "Run with sudo" >&2; exit 1; }

# Refuse to disable password login if the admin running this has no SSH key yet
ADMIN_USER="${SUDO_USER:-}"
if [ -z "$ADMIN_USER" ] || [ "$ADMIN_USER" = root ] \
   || [ ! -s "$(getent passwd "$ADMIN_USER" | cut -d: -f6)/.ssh/authorized_keys" ]; then
  echo "Run this via sudo from a non-root admin user that has ~/.ssh/authorized_keys," >&2
  echo "otherwise disabling password and root login would lock you out." >&2
  exit 1
fi

echo ">> Installing packages"
apt-get update
apt-get install -y --no-install-recommends openjdk-17-jre-headless curl ufw fail2ban

echo ">> Creating users"
id "$APP_USER" >/dev/null 2>&1 || useradd --system --home-dir "$APP_DIR" --shell /usr/sbin/nologin "$APP_USER"
id "$DEPLOY_USER" >/dev/null 2>&1 || useradd --create-home --shell /bin/bash "$DEPLOY_USER"
# Lets deploy.sh print the service logs when a release fails
usermod -aG systemd-journal "$DEPLOY_USER"

DEPLOY_HOME="$(getent passwd "$DEPLOY_USER" | cut -d: -f6)"
install -d -m 700 -o "$DEPLOY_USER" -g "$DEPLOY_USER" "$DEPLOY_HOME/.ssh"
# "restrict": no port/agent/X11 forwarding and no PTY, so a leaked key can't be used as a tunnel
KEY_LINE="restrict $DEPLOY_PUBKEY"
grep -qxF "$KEY_LINE" "$DEPLOY_HOME/.ssh/authorized_keys" 2>/dev/null \
  || echo "$KEY_LINE" >> "$DEPLOY_HOME/.ssh/authorized_keys"
chown "$DEPLOY_USER:$DEPLOY_USER" "$DEPLOY_HOME/.ssh/authorized_keys"
chmod 600 "$DEPLOY_HOME/.ssh/authorized_keys"

echo ">> Preparing $APP_DIR"
# deploy owns the releases, the service user only reads them
install -d "$APP_DIR" "$APP_DIR/releases"
chown "$DEPLOY_USER:$APP_USER" "$APP_DIR" "$APP_DIR/releases"
chmod 755 "$APP_DIR" "$APP_DIR/releases"

install -d -m 750 -o root -g "$APP_USER" "$ENV_DIR"
if [ ! -f "$ENV_DIR/app.env" ]; then
  if [ -f "$APP_DIR/.env" ]; then
    install -m 640 -o root -g "$APP_USER" "$APP_DIR/.env" "$ENV_DIR/app.env"
    echo ">> Copied $APP_DIR/.env to $ENV_DIR/app.env"
  else
    install -m 640 -o root -g "$APP_USER" "$SCRIPT_DIR/.env.example" "$ENV_DIR/app.env"
    echo ">> Fill in $ENV_DIR/app.env before starting the service"
  fi
fi
# The old copy stays for rollback, but only root may read it
[ -f "$APP_DIR/.env" ] && chown root:root "$APP_DIR/.env" && chmod 600 "$APP_DIR/.env"

# Start the release history with the JAR that is running now
if [ ! -e "$APP_DIR/app.jar" ]; then
  OLD_JAR="$(ls -1t "$APP_DIR"/*.jar 2>/dev/null | head -n 1 || true)"
  if [ -n "$OLD_JAR" ]; then
    install -m 644 -o "$DEPLOY_USER" -g "$APP_USER" "$OLD_JAR" "$APP_DIR/releases/app-initial.jar"
    ln -sfn "$APP_DIR/releases/app-initial.jar" "$APP_DIR/app.jar"
    chown -h "$DEPLOY_USER:$APP_USER" "$APP_DIR/app.jar"
  fi
fi

echo ">> Installing systemd unit (not restarting)"
UNIT=/etc/systemd/system/$SERVICE.service
[ -f "$UNIT" ] && [ ! -f "/root/$SERVICE.service.bak" ] && cp "$UNIT" "/root/$SERVICE.service.bak"
install -m 644 "$SCRIPT_DIR/$SERVICE.service" "$UNIT"
systemctl daemon-reload
systemctl enable "$SERVICE"

# The only root command the deploy user may run
cat > /etc/sudoers.d/ticket-booking-deploy <<EOF
$DEPLOY_USER ALL=(root) NOPASSWD: /usr/bin/systemctl restart $SERVICE
EOF
chmod 440 /etc/sudoers.d/ticket-booking-deploy
visudo -cf /etc/sudoers.d/ticket-booking-deploy

echo ">> Hardening SSH"
cat > /etc/ssh/sshd_config.d/10-hardening.conf <<EOF
PermitRootLogin no
PasswordAuthentication no
KbdInteractiveAuthentication no
PubkeyAuthentication yes
MaxAuthTries 3
EOF
sshd -t
systemctl reload ssh

echo ">> Configuring ufw"
ufw default deny incoming
ufw default allow outgoing
ufw allow "$SSH_PORT/tcp"
ufw allow 80/tcp
ufw allow 443/tcp
[ "$OPEN_APP_PORT" = 1 ] && ufw allow "$APP_PORT/tcp"
ufw --force enable

echo ">> Configuring fail2ban"
cat > /etc/fail2ban/jail.d/sshd.local <<EOF
[sshd]
enabled  = true
port     = $SSH_PORT
backend  = systemd
maxretry = 5
findtime = 10m
bantime  = 1h
EOF
systemctl enable --now fail2ban
systemctl restart fail2ban

echo
echo ">> Done. The app is still running under the old unit (as root)."
echo "   Switch:    sudo systemctl restart $SERVICE && curl -fsS http://127.0.0.1:$APP_PORT/actuator/health"
echo "   Roll back: sudo cp /root/$SERVICE.service.bak $UNIT && sudo systemctl daemon-reload && sudo systemctl restart $SERVICE"
