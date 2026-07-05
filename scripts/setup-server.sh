#!/bin/bash
# ══════════════════════════════════════════════════════════
# SKILLORA — Server Setup Script (Ubuntu/Debian)
# Run once on a fresh server as root
# Usage: sudo bash scripts/setup-server.sh
# ══════════════════════════════════════════════════════════

set -e

echo "🛠️  Skillora Server Setup"
echo ""

# ── System update ─────────────────────────────────────────
apt-get update -qq
apt-get upgrade -y -qq

# ── Install dependencies ──────────────────────────────────
apt-get install -y -qq \
  openjdk-17-jre-headless \
  nginx \
  mysql-server \
  certbot \
  python3-certbot-nginx \
  curl \
  wget \
  unzip \
  git \
  ufw

# ── Node.js 20 ────────────────────────────────────────────
curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
apt-get install -y nodejs

# ── Create app user ───────────────────────────────────────
useradd -r -s /bin/false skillora 2>/dev/null || true
mkdir -p /opt/skillora/{backend,frontend/dist,uploads,logs}
chown -R skillora:skillora /opt/skillora

# ── Firewall ──────────────────────────────────────────────
ufw --force enable
ufw allow ssh
ufw allow 80/tcp
ufw allow 443/tcp
# ufw deny 8080  # Block direct backend access from internet

# ── MySQL secure setup ────────────────────────────────────
systemctl enable mysql
systemctl start mysql
echo "Run: mysql -u root -p < scripts/setup-mysql.sql"

# ── Nginx setup ───────────────────────────────────────────
cp /opt/skillora/scripts/nginx.conf /etc/nginx/sites-available/skillora
ln -sf /etc/nginx/sites-available/skillora /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t && systemctl reload nginx

# ── Systemd service for backend ───────────────────────────
cat > /etc/systemd/system/skillora.service << 'EOF'
[Unit]
Description=Skillora Backend
After=network.target mysql.service
Wants=mysql.service

[Service]
Type=simple
User=skillora
WorkingDirectory=/opt/skillora/backend
EnvironmentFile=/opt/skillora/.env
ExecStart=/usr/bin/java \
  -Xms256m -Xmx512m \
  -XX:+UseContainerSupport \
  -jar app.jar \
  --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=append:/opt/skillora/logs/app.log
StandardError=append:/opt/skillora/logs/app-error.log

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable skillora

echo ""
echo "✅ Server setup complete!"
echo ""
echo "Next steps:"
echo "  1. Copy your .env to /opt/skillora/.env"
echo "  2. Run: mysql -u root -p < scripts/setup-mysql.sql"
echo "  3. Copy backend JAR to /opt/skillora/backend/app.jar"
echo "  4. Copy frontend dist/ to /opt/skillora/frontend/dist/"
echo "  5. Run: certbot --nginx -d skillora.com -d www.skillora.com"
echo "  6. Run: systemctl start skillora"
