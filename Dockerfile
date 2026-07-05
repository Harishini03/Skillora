# ══════════════════════════════════════════════════════
# SKILLORA — Multi-stage Docker Build
# Stage 1: Build the JAR
# Stage 2: Minimal runtime image
# ══════════════════════════════════════════════════════

# ── Stage 1: Builder ────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Cache Gradle wrapper and dependencies first (layer caching)
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon --quiet

# Build the fat JAR (skip tests — run tests in CI pipeline)
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test --quiet

# ── Stage 2: Runtime ────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Security: non-root user
RUN addgroup -S skillora && adduser -S skillora -G skillora

# Copy JAR from builder
COPY --from=builder /app/build/libs/*.jar app.jar
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
