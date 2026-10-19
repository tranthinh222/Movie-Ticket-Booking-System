# ========== STAGE 1: BUILD ==========
FROM gradle:8.14.3-jdk17 AS builder
WORKDIR /app

# Copy build scripts trước để cache dependency
COPY settings.gradle.kts build.gradle.kts ./
RUN gradle dependencies --no-daemon > /dev/null

COPY src ./src

# Test đã chạy ở CI trước khi build image
RUN gradle bootJar -x test --no-daemon

# ========== STAGE 2: RUN ==========
FROM eclipse-temurin:17-jre
WORKDIR /app

# Không chạy app bằng root
RUN groupadd --system app && useradd --system --gid app app

COPY --from=builder /app/build/libs/*.jar app.jar

USER app

# Giới hạn heap theo RAM của container
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

# Expose port Spring Boot
EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --start-period=120s --start-interval=5s --retries=3 \
  CMD curl -fsS "http://localhost:${PORT:-8081}/actuator/health/readiness" || exit 1

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
