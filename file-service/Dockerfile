FROM openjdk:11-jre-slim

WORKDIR /app

# Install necessary packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create directories
RUN mkdir -p /tmp/uploads

# Copy the JAR file
COPY target/file-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8083/api/files/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
