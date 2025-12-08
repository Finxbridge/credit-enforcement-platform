# ===============================
# Stage 1: Build the application
# ===============================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copy parent POM and service POM for dependency resolution
COPY pom.xml ./
COPY strategy-engine-service/pom.xml ./strategy-engine-service/

# Download dependencies (cached layer)
RUN --mount=type=cache,target=/root/.m2 \
    cd strategy-engine-service && \
    mvn dependency:go-offline -B

# Copy full source code for this service
COPY strategy-engine-service ./strategy-engine-service

# Build this specific service (skip tests for faster image build)
RUN --mount=type=cache,target=/root/.m2 \
    cd strategy-engine-service && \
    mvn clean package -DskipTests

# ===============================
# Stage 2: Runtime image
# ===============================
FROM eclipse-temurin:21-jre-jammy

# Create a non-root user for security
RUN useradd -ms /bin/bash springuser

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy only the JAR of the specific service
COPY --from=builder /workspace/strategy-engine-service/target/strategy-engine-service-*.jar app.jar

# Change ownership to non-root user
RUN chown springuser:springuser /app/app.jar
USER springuser

# Expose service port
EXPOSE 8086

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8086/actuator/health || exit 1

# Container-optimized JVM options
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
