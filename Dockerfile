# ── Stage 1: Build ──────────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
# Copy Maven wrapper and POM first (for Docker layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Download dependencies (cached unless pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:resolve -B
# Copy source code and build
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ── Stage 2: Run ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
# Security: Run as non-root user
RUN groupadd -r spiceflow && useradd -r -g spiceflow spiceflow
USER spiceflow
# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar
# Expose the application port
EXPOSE 8080
# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
# Run with production profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
