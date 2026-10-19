#!/usr/bin/env bash
# One-time setup of a fresh Ubuntu VPS for the backend:
#   - JRE 17, service user, /opt/cinema layout, systemd unit
#   - deploy user that GitHub Actions logs in as (may only restart the service via sudo)
#   - SSH hardening (key-only, no root login), ufw, fail2ban
#
# Usage (from a sudo-capable admin account that already logs in with an SSH key):
#   sudo DEPLOY_PUBKEY="ssh-ed25519 AAAA... github-actions" bash setup-vps.sh
set -euo pipefail

DEPLOY_USER="${DEPLOY_USER:-deploy}"
DEPLOY_PUBKEY="${DEPLOY_PUBKEY:?set DEPLOY_PUBKEY to the public key matching the VPS_SSH_KEY secret}"
# Port sshd already listens on (this script does not change it)
SSH_PORT="${SSH_PORT:-22}"
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

echo ">> Creating users and directories"
id cinema >/dev/null 2>&1 || useradd --system --home-dir /opt/cinema --shell /usr/sbin/nologin cinema
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

# deploy owns the releases, the service user only reads them
install -d -m 755 -o "$DEPLOY_USER" -g cinema /opt/cinema /opt/cinema/releases
install -d -m 750 -o cinema -g cinema /opt/cinema/logs

install -d -m 750 -o root -g cinema /etc/cinema
if [ ! -f /etc/cinema/cinema.env ]; then
  install -m 640 -o root -g cinema "$SCRIPT_DIR/.env.example" /etc/cinema/cinema.env
  echo ">> Fill in /etc/cinema/cinema.env before the first deploy"
fi

echo ">> Installing systemd unit"
install -m 644 "$SCRIPT_DIR/cinema-api.service" /etc/systemd/system/cinema-api.service
systemctl daemon-reload
systemctl enable cinema-api

# The only root command the deploy user may run
cat > /etc/sudoers.d/cinema-deploy <<EOF
$DEPLOY_USER ALL=(root) NOPASSWD: /usr/bin/systemctl restart cinema-api
EOF
chmod 440 /etc/sudoers.d/cinema-deploy
visudo -cf /etc/sudoers.d/cinema-deploy

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
ufw allow "$APP_PORT/tcp"
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

echo ">> Done. Add these GitHub secrets: VPS_HOST, VPS_USER=$DEPLOY_USER, VPS_SSH_KEY, VPS_KNOWN_HOSTS"
echo "   VPS_KNOWN_HOSTS: run 'ssh-keyscan -p $SSH_PORT <host>' from your machine"
