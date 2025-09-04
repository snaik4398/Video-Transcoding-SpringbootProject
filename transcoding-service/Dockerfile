FROM openjdk:11-jre-slim

WORKDIR /app

# Install FFmpeg and necessary packages
RUN apt-get update && apt-get install -y \
    ffmpeg \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create directories
RUN mkdir -p /tmp/transcode /app/output

# Copy the JAR file
COPY target/transcoding-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8082/api/transcode/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
