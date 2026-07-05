#!/bin/bash
# ══════════════════════════════════════════════════════════
# SKILLORA — Deployment Script
# Usage: ./scripts/deploy.sh [local|docker|jar]
# ══════════════════════════════════════════════════════════

set -e  # Exit on any error

MODE=${1:-local}
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "🚀 Skillora Deploy — Mode: $MODE"
echo "   Project: $PROJECT_ROOT"
echo ""

# ── Helper functions ──────────────────────────────────────
info()    { echo "  ✅ $1"; }
warn()    { echo "  ⚠️  $1"; }
section() { echo ""; echo "── $1 ──────────────────────────────"; }

# ── Validate environment ──────────────────────────────────
check_env() {
  section "Environment Check"
  [ -z "$JWT_SECRET" ]   && warn "JWT_SECRET not set"   || info "JWT_SECRET set"
  [ -z "$GROQ_API_KEY" ] && warn "GROQ_API_KEY not set" || info "GROQ_API_KEY set"
  [ -z "$DB_PASSWORD" ]  && warn "DB_PASSWORD not set"  || info "DB_PASSWORD set"
}

# ── Build backend ─────────────────────────────────────────
build_backend() {
  section "Building Backend"
  cd "$PROJECT_ROOT"
  ./gradlew clean bootJar --no-daemon -x test --quiet
  JAR=$(ls build/libs/*.jar | grep -v plain)
  info "JAR built: $JAR ($(du -sh "$JAR" | cut -f1))"
}

# ── Build frontend ────────────────────────────────────────
build_frontend() {
  section "Building Frontend"
  cd "$PROJECT_ROOT/frontend"
  npm ci --quiet
  npm run build
  info "Frontend built: dist/ ($(du -sh dist | cut -f1))"
}

# ── Run tests ─────────────────────────────────────────────
run_tests() {
  section "Running Tests"
  cd "$PROJECT_ROOT"
  ./gradlew test --no-daemon --quiet
  info "All tests passed"
}

# ── Deploy modes ──────────────────────────────────────────
case "$MODE" in
  "local")
    section "Local Development"
    info "Starting backend on :8080"
    cd "$PROJECT_ROOT"
    ./gradlew bootRun --no-daemon &
    BACKEND_PID=$!
    info "Starting frontend on :5173"
    cd "$PROJECT_ROOT/frontend"
    npm run dev &
    FRONTEND_PID=$!
    echo ""
    echo "  🌐 Frontend: http://localhost:5173"
    echo "  🔧 API:      http://localhost:8080"
    echo "  Press Ctrl+C to stop both"
    wait
    ;;

  "docker")
    check_env
    run_tests
    section "Docker Deploy"
    cd "$PROJECT_ROOT"
    docker-compose down --remove-orphans
    docker-compose up --build -d
    info "Containers started"
    docker-compose ps
    echo ""
    echo "  🌐 Frontend: http://localhost:3000"
    echo "  🔧 API:      http://localhost:8080"
    ;;

  "jar")
    check_env
    run_tests
    build_backend
    build_frontend
    section "JAR Deploy"
    JAR=$(ls "$PROJECT_ROOT/build/libs"/*.jar | grep -v plain)
    echo "  Run: java -jar $JAR --spring.profiles.active=prod"
    echo "  Env vars needed: JWT_SECRET, DB_PASSWORD, DB_USERNAME, GROQ_API_KEY, CORS_ALLOWED_ORIGINS"
    ;;

  *)
    echo "Unknown mode: $MODE"
    echo "Usage: $0 [local|docker|jar]"
    exit 1
    ;;
esac

echo ""
echo "✅ Skillora deployment complete!"
