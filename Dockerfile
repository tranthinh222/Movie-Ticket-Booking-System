# ========== STAGE 1: BUILD ==========
FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

# Copy toàn bộ project vào container
COPY . .

# Build jar (bỏ test để build nhanh)
RUN gradle clean bootJar -x test

# ========== STAGE 2: RUN ==========
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy file jar từ stage build sang
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port Spring Boot (mặc định 8080)
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
