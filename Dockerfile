# ══════════════════════════════════════════════════════
# SKILLORA — Multi-stage Docker Build
# Stage 1: Build the React frontend
# Stage 2: Build the Spring Boot JAR (with frontend bundled)
# Stage 3: Minimal runtime image
# ══════════════════════════════════════════════════════

# ── Stage 1: Frontend Builder ────────────────────────────────
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend

# Cache npm dependencies
COPY frontend/package*.json ./
RUN npm ci --quiet

# Copy source and build
# Pass the backend URL at build time (defaults to same origin = relative URL)
ARG VITE_API_URL=""
ENV VITE_API_URL=$VITE_API_URL

COPY frontend/ ./
RUN npm run build

# ── Stage 2: Backend Builder ─────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS backend-builder
WORKDIR /app

# Cache Gradle wrapper and dependencies first (layer caching)
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon --quiet

# Copy backend source
COPY src/ src/

# Copy the built React app into Spring Boot's static resources
# Spring Boot auto-serves anything in src/main/resources/static/
COPY --from=frontend-builder /frontend/dist/ src/main/resources/static/

# Build the fat JAR (skip tests — run tests in CI pipeline)
RUN ./gradlew bootJar --no-daemon -x test --quiet

# ── Stage 3: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Security: non-root user
RUN addgroup -S skillora && adduser -S skillora -G skillora

# Copy JAR from builder
COPY --from=backend-builder /app/build/libs/*.jar app.jar
RUN chown skillora:skillora app.jar

# Runtime user
USER skillora

# Port
EXPOSE 8080

# Health check (uses actuator health endpoint)
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# JVM tuning for containers
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar -Dspring.profiles.active=prod app.jar"]
