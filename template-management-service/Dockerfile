# ===============================
# Stage 1: Build the application
# ===============================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /workspace

# Copy parent POM first
COPY pom.xml ./

# Copy this service's POM
COPY template-management-service/pom.xml ./template-management-service/

# Download dependencies (cached layer)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B -pl template-management-service -am

# Copy source code for this service
COPY template-management-service/src ./template-management-service/src

# Build this service (skip tests for faster image build)
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -pl template-management-service -am

# ===============================
# Stage 2: Runtime image
# ===============================
FROM eclipse-temurin:21-jre-jammy

# Create a non-root user for security
RUN useradd -ms /bin/bash springuser

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /workspace/template-management-service/target/template-management-service-*.jar app.jar

# Change ownership to non-root user
RUN chown springuser:springuser /app/app.jar
USER springuser

# Expose default port for this service
EXPOSE 8087

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=60s \
  CMD curl -f http://localhost:8087/actuator/health || exit 1

# Container-optimized JVM options
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
