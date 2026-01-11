FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install FFmpeg and necessary packages
RUN apk add --no-cache \
    ffmpeg \
    curl

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
