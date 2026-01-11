FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install necessary packages
RUN apk add --no-cache curl

# Copy the JAR file
COPY target/notification-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8084/api/notifications/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
