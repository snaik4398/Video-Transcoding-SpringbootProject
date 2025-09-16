# Video Transcoding Service

A comprehensive Java Spring Boot microservices application for video transcoding with support for various codecs, formats, and quality settings. The service provides RESTful APIs for video conversion with configurable parameters including bitrate, frame rate, resolution, and processing mode (CPU/GPU).

## Architecture Overview

The application consists of four microservices:

1. **Authentication Service** (Port: 8081) - User registration, login, and JWT token management
2. **Transcoding Service** (Port: 8082) - Video transcoding with FFmpeg integration
3. **File Management Service** (Port: 8083) - File upload, download, and storage management
4. **Notification Service** (Port: 8084) - User notifications and alerts

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.5.5
- **Spring Security**: JWT-based authentication
- **Spring Data JPA**: PostgreSQL database
- **Spring Kafka**: Message queuing
- **Redis**: Caching and session management
- **MinIO**: S3-compatible object storage
- **FFmpeg**: Video transcoding engine
- **Docker**: Containerization
- **PostgreSQL**: Primary database

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- FFmpeg (for local development)

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd video-transcoding-service

# Build all services
mvn clean install
```

### 2. Start with Docker Compose

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### 3. Access Services

- **Auth Service**: http://localhost:8081
- **Transcoding Service**: http://localhost:8082
- **File Service**: http://localhost:8083
- **Notification Service**: http://localhost:8084
- **MinIO Console**: http://localhost:9001 (minioadmin/minioadmin123)

## API Usage Examples

### 1. User Registration

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### 2. User Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 3. Upload Video File

```bash
curl -X POST http://localhost:8083/api/files/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/video.mp4" \
  -F "description=Test video upload"
```

### 4. Start Transcoding Job

```bash
curl -X POST http://localhost:8082/api/transcode/jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inputFileId": "file-uuid-123",
    "outputSettings": {
      "videoCodec": "libx264",
      "audioCodec": "aac",
      "outputFormat": "mp4",
      "videoBitrate": "2000k",
      "audioBitrate": "128k",
      "resolution": "1920x1080",
      "frameRate": 30,
      "processingMode": "CPU"
    },
    "priority": "NORMAL"
  }'
```

### 5. Get System Information

```bash
curl -X GET http://localhost:8082/api/transcode/system-info \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Configuration

### Environment Variables

Create a `.env` file in the root directory:

```bash
# Database Configuration
POSTGRES_DB=transcode_db
POSTGRES_USER=transcode_user
POSTGRES_PASSWORD=secure_password_123
POSTGRES_HOST=postgres
POSTGRES_PORT=5432

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=redis_password_123

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=kafka:29092
KAFKA_GROUP_ID=transcode-service-group

# MinIO Configuration
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123
MINIO_BUCKET_NAME=video-files

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRATION=86400000

# FFmpeg Configuration
FFMPEG_PATH=/usr/local/bin/ffmpeg
FFPROBE_PATH=/usr/local/bin/ffprobe

# Processing Configuration
MAX_CONCURRENT_JOBS=4
TEMP_DIR=/tmp/transcode
OUTPUT_DIR=/app/output
```

## Development

### Local Development Setup

1. **Start Infrastructure Services**:
   ```bash
   docker-compose up -d postgres redis kafka minio
   ```

2. **Run Services Locally**:
   ```bash
   # Auth Service
   cd auth-service && mvn spring-boot:run
   
   # Transcoding Service
   cd transcoding-service && mvn spring-boot:run
   
   # File Service
   cd file-service && mvn spring-boot:run
   
   # Notification Service
   cd notification-service && mvn spring-boot:run
   ```

### Building Individual Services

```bash
# Build specific service
mvn clean install -pl auth-service
mvn clean install -pl transcoding-service
mvn clean install -pl file-service
mvn clean install -pl notification-service

# Build all services
mvn clean install
```

## Testing

### Run Tests

```bash
# Run all tests
mvn test

# Run tests for specific service
mvn test -pl auth-service
mvn test -pl transcoding-service
mvn test -pl file-service
mvn test -pl notification-service
```

## Monitoring

### Health Checks

- **Auth Service**: http://localhost:8081/actuator/health
- **Transcoding Service**: http://localhost:8082/actuator/health
- **File Service**: http://localhost:8083/actuator/health
- **Notification Service**: http://localhost:8084/actuator/health

### Metrics

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)

## Troubleshooting

### Common Issues

1. **Port Conflicts**: Ensure ports 8081-8084, 5432, 6379, 9092, 9000-9001 are available
2. **FFmpeg Not Found**: The transcoding service Docker image includes FFmpeg
3. **Database Connection**: Wait for PostgreSQL to be healthy before starting services
4. **MinIO Access**: Use minioadmin/minioadmin123 for initial access

### Logs

```bash
# View service logs
docker-compose logs -f auth-service
docker-compose logs -f transcoding-service
docker-compose logs -f file-service
docker-compose logs -f notification-service

# View infrastructure logs
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f kafka
docker-compose logs -f minio
```

## Security

- JWT-based authentication
- Role-based access control
- Secure file upload validation
- Environment variable configuration
- HTTPS support (configure in production)

## Performance

- Redis caching for session management
- Kafka for async job processing
- Configurable concurrent job limits
- GPU acceleration support (when available)
- MinIO for scalable object storage

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the logs for error details
