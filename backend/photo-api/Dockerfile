# Multi-stage build for optimal image size
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy parent pom
COPY pom.xml .
COPY photo-common/pom.xml photo-common/
COPY storage-provider/pom.xml storage-provider/
COPY event-bus/pom.xml event-bus/
COPY photo-service/pom.xml photo-service/
COPY photo-api/pom.xml photo-api/

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY photo-common/src photo-common/src
COPY storage-provider/src storage-provider/src
COPY event-bus/src event-bus/src
COPY photo-service/src photo-service/src
COPY photo-api/src photo-api/src

# Build application
RUN mvn clean package -DskipTests -pl photo-api -am

# Production image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser

# Copy JAR from build stage
COPY --from=build /app/photo-api/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appuser /app

USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]

