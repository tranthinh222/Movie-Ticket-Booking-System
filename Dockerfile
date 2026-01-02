# ========== STAGE 1: BUILD ==========
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY . .

RUN gradle clean bootJar -x test

# ========== STAGE 2: RUN ==========
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port Spring Boot
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
