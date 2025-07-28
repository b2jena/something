# Multi-stage Dockerfile for production deployment
# Interview Points:
# 1. Multi-stage builds reduce final image size
# 2. Distroless images minimize attack surface
# 3. Non-root user improves security
# 4. JVM tuning for containerized environments

# Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached layer if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Runtime stage using distroless image
FROM gcr.io/distroless/java17-debian11:nonroot

# Create application user (already exists in distroless)
USER nonroot:nonroot

# Copy the built JAR from builder stage
COPY --from=builder --chown=nonroot:nonroot /app/target/*.jar /app/book-api.jar

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=prod"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/book-api.jar"]