# ===============================
# Stage 1: Build the application
# ===============================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /workspace

# Copy Maven descriptors first (for dependency caching)
COPY pom.xml ./
COPY api-gateway/pom.xml ./api-gateway/
COPY config-server/pom.xml ./config-server/
COPY eureka-server/pom.xml ./eureka-server/
COPY access-management-service/pom.xml ./access-management-service/
COPY communication-service/pom.xml ./communication-service/
COPY master-data-service/pom.xml ./master-data-service/

# Download dependencies (cached layer)
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# Copy full source code
COPY . .

# Build all modules (skip tests for faster image build)
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

# ===============================
# Stage 2: Runtime image
# ===============================
FROM eclipse-temurin:21-jre-jammy

# Create a non-root user for security
RUN useradd -ms /bin/bash springuser

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
ARG SERVICE_NAME

# Copy only the JAR of the specific service
COPY --from=builder /workspace/${SERVICE_NAME}/target/${SERVICE_NAME}-*.jar app.jar

# Change ownership to non-root user
RUN chown springuser:springuser /app/app.jar
USER springuser

# Expose Spring Boot default port
EXPOSE 8080

# Container-optimized JVM options
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
