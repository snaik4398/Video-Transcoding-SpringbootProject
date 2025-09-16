I'll create a new markdown file with the comprehensive Postman collection documentation for all the microservices.


[1 tool called]

I'll create a new markdown file with the comprehensive Postman collection documentation:

```markdown:postman-collection.md
# Video Transcoding Service - Postman Collection

This document provides comprehensive curl commands and API documentation for all microservices in the Video Transcoding Service. The service consists of four microservices running on different ports with JWT-based authentication.

## Table of Contents

1. [Service Overview](#service-overview)
2. [Authentication Service (Port 8081)](#authentication-service-port-8081)
3. [Transcoding Service (Port 8082)](#transcoding-service-port-8082)
4. [File Management Service (Port 8083)](#file-management-service-port-8083)
5. [Notification Service (Port 8084)](#notification-service-port-8084)
6. [Environment Variables](#environment-variables)
7. [Common Headers](#common-headers)
8. [Error Handling](#error-handling)
9. [Testing Workflow](#testing-workflow)

## Service Overview

| Service | Port | Base URL | Description |
|---------|------|----------|-------------|
| Authentication Service | 8081 | `http://localhost:8081/api/auth` | User registration, login, JWT token management |
| Transcoding Service | 8082 | `http://localhost:8082/api/transcode` | Video transcoding with FFmpeg integration |
| File Management Service | 8083 | `http://localhost:8083/api/files` | File upload, download, and storage management |
| Notification Service | 8084 | `http://localhost:8084/api/notifications` | User notifications and alerts |

## Authentication Service (Port 8081)

### Base URL: `http://localhost:8081/api/auth`

#### 1. User Registration

**Endpoint:** `POST /register`

**Description:** Register a new user account

**Request Body:**
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

**Validation Rules:**
- Username: 3-50 characters, required
- Email: Valid email format, required
- Password: Minimum 6 characters, required

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "user-uuid",
    "username": "testuser",
    "email": "test@example.com",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00",
    "lastLogin": null,
    "isActive": true
  },
  "message": "User registered successfully"
}
```

#### 2. User Login

**Endpoint:** `POST /login`

**Description:** Authenticate user and get JWT token

**Request Body:**
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**cURL Command:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "user-uuid",
    "username": "testuser",
    "email": "test@example.com",
    "role": "USER",
    "createdAt": "2024-01-01T10:00:00",
    "lastLogin": "2024-01-01T10:30:00",
    "isActive": true
  },
  "message": "Login successful"
}
```

#### 3. Health Check

**Endpoint:** `GET /health`

**Description:** Check if authentication service is running

**cURL Command:**
```bash
curl -X GET http://localhost:8081/api/auth/health
```

**Response:**
```
Auth Service is running
```

## Transcoding Service (Port 8082)

### Base URL: `http://localhost:8082/api/transcode`

**Note:** All endpoints require JWT authentication. Include the token in the Authorization header.

#### 1. Get System Information

**Endpoint:** `GET /system-info`

**Description:** Get detailed system information including CPU, GPU, memory, and FFmpeg versions

**cURL Command:**
```bash
curl -X GET http://localhost:8082/api/transcode/system-info \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "cpuInfo": "Intel(R) Core(TM) i7-10700K CPU @ 3.80GHz",
  "gpuInfo": "NVIDIA GeForce RTX 3070",
  "totalMemory": 17179869184,
  "availableMemory": 8589934592,
  "cpuCores": 8,
  "cpuUsage": 25.5,
  "memoryUsage": 45.2,
  "ffmpegVersion": "4.4.2",
  "ffprobeVersion": "4.4.2",
  "gpuAccelerationEnabled": false
}
```

#### 2. Create Transcoding Job

**Endpoint:** `POST /jobs`

**Description:** Create a new video transcoding job

**Request Body:**
```json
{
  "inputFileId": "file-uuid-here",
  "outputFilename": "output_video.mp4",
  "outputSettings": {
    "videoCodec": "libx264",
    "audioCodec": "aac",
    "outputFormat": "mp4",
    "videoBitrate": "1500k",
    "audioBitrate": "128k",
    "resolution": "1280x720",
    "frameRate": 30,
    "processingMode": "CPU"
  },
  "priority": "NORMAL"
}
```

**Available Options:**
- **Video Codecs:** `libx264`, `libx265`, `libvpx-vp9`, `libaom-av1`
- **Audio Codecs:** `aac`, `mp3`, `opus`, `vorbis`
- **Output Formats:** `mp4`, `webm`, `avi`, `mkv`
- **Processing Modes:** `CPU`, `GPU` (if available)
- **Priority:** `LOW`, `NORMAL`, `HIGH`, `URGENT`

**cURL Command:**
```bash
curl -X POST http://localhost:8082/api/transcode/jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inputFileId": "file-uuid-here",
    "outputFilename": "output_video.mp4",
    "outputSettings": {
      "videoCodec": "libx264",
      "audioCodec": "aac",
      "outputFormat": "mp4",
      "videoBitrate": "1500k",
      "audioBitrate": "128k",
      "resolution": "1280x720",
      "frameRate": 30,
      "processingMode": "CPU"
    },
    "priority": "NORMAL"
  }'
```

**Response:**
```json
{
  "id": "job-uuid",
  "userId": "user-uuid",
  "inputFileId": "file-uuid-here",
  "outputFilename": "output_video.mp4",
  "status": "PENDING",
  "priority": "NORMAL",
  "outputSettings": {
    "videoCodec": "libx264",
    "audioCodec": "aac",
    "outputFormat": "mp4",
    "videoBitrate": "1500k",
    "audioBitrate": "128k",
    "resolution": "1280x720",
    "frameRate": 30,
    "processingMode": "CPU"
  },
  "progress": 0,
  "createdAt": "2024-01-01T10:00:00",
  "startedAt": null,
  "completedAt": null,
  "errorMessage": null
}
```

#### 3. Get Transcoding Job

**Endpoint:** `GET /jobs/{jobId}`

**Description:** Get details of a specific transcoding job

**cURL Command:**
```bash
curl -X GET http://localhost:8082/api/transcode/jobs/job-uuid-here \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": "job-uuid",
  "userId": "user-uuid",
  "inputFileId": "file-uuid-here",
  "outputFilename": "output_video.mp4",
  "status": "PROCESSING",
  "priority": "NORMAL",
  "outputSettings": {
    "videoCodec": "libx264",
    "audioCodec": "aac",
    "outputFormat": "mp4",
    "videoBitrate": "1500k",
    "audioBitrate": "128k",
    "resolution": "1280x720",
    "frameRate": 30,
    "processingMode": "CPU"
  },
  "progress": 45,
  "createdAt": "2024-01-01T10:00:00",
  "startedAt": "2024-01-01T10:01:00",
  "completedAt": null,
  "errorMessage": null
}
```

#### 4. Get User Jobs

**Endpoint:** `GET /jobs`

**Description:** Get paginated list of user's transcoding jobs

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)
- `status` (optional): Filter by status (`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, `CANCELLED`)

**cURL Commands:**

Get all jobs:
```bash
curl -X GET "http://localhost:8082/api/transcode/jobs?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Get jobs by status:
```bash
curl -X GET "http://localhost:8082/api/transcode/jobs?status=PROCESSING&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "content": [
    {
      "id": "job-uuid-1",
      "userId": "user-uuid",
      "inputFileId": "file-uuid-here",
      "outputFilename": "output_video.mp4",
      "status": "COMPLETED",
      "priority": "NORMAL",
      "progress": 100,
      "createdAt": "2024-01-01T10:00:00",
      "startedAt": "2024-01-01T10:01:00",
      "completedAt": "2024-01-01T10:05:00",
      "errorMessage": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": false,
      "unsorted": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1
}
```

#### 5. Cancel Transcoding Job

**Endpoint:** `DELETE /jobs/{jobId}`

**Description:** Cancel a pending or processing transcoding job

**cURL Command:**
```bash
curl -X DELETE http://localhost:8082/api/transcode/jobs/job-uuid-here \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```
HTTP 204 No Content
```

#### 6. Bulk Transcoding (Placeholder)

**Endpoint:** `POST /bulk`

**Description:** Bulk transcoding feature (not implemented yet)

**cURL Command:**
```bash
curl -X POST http://localhost:8082/api/transcode/bulk \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Response:**
```
Bulk transcoding feature not implemented yet
```

#### 7. Health Check

**Endpoint:** `GET /health`

**Description:** Check if transcoding service is running

**cURL Command:**
```bash
curl -X GET http://localhost:8082/api/transcode/health
```

**Response:**
```
Transcoding Service is running
```

## File Management Service (Port 8083)

### Base URL: `http://localhost:8083/api/files`

**Note:** All endpoints require JWT authentication. Include the token in the Authorization header.

#### 1. Upload Video File

**Endpoint:** `POST /upload`

**Description:** Upload a video file for transcoding

**Request Type:** `multipart/form-data`

**Form Data:**
- `file`: Video file (required)
- `description`: File description (optional)

**cURL Command:**
```bash
curl -X POST http://localhost:8083/api/files/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/your/video.mp4" \
  -F "description=My test video file"
```

**Response:**
```json
{
  "id": "file-uuid",
  "userId": "user-uuid",
  "originalFilename": "video.mp4",
  "storedFilename": "stored-uuid.mp4",
  "fileSize": 52428800,
  "contentType": "video/mp4",
  "description": "My test video file",
  "uploadedAt": "2024-01-01T10:00:00",
  "isProcessed": false
}
```

#### 2. Get File Information

**Endpoint:** `GET /{fileId}`

**Description:** Get information about a specific file

**cURL Command:**
```bash
curl -X GET http://localhost:8083/api/files/file-uuid-here \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": "file-uuid",
  "userId": "user-uuid",
  "originalFilename": "video.mp4",
  "storedFilename": "stored-uuid.mp4",
  "fileSize": 52428800,
  "contentType": "video/mp4",
  "description": "My test video file",
  "uploadedAt": "2024-01-01T10:00:00",
  "isProcessed": false
}
```

#### 3. Download File

**Endpoint:** `GET /{fileId}/download`

**Description:** Download a file

**cURL Command:**
```bash
curl -X GET http://localhost:8083/api/files/file-uuid-here/download \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -o downloaded_video.mp4
```

**Response:** Binary file content with appropriate headers

#### 4. Get User Files

**Endpoint:** `GET /`

**Description:** Get paginated list of user's files

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)

**cURL Command:**
```bash
curl -X GET "http://localhost:8083/api/files?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "content": [
    {
      "id": "file-uuid",
      "userId": "user-uuid",
      "originalFilename": "video.mp4",
      "storedFilename": "stored-uuid.mp4",
      "fileSize": 52428800,
      "contentType": "video/mp4",
      "description": "My test video file",
      "uploadedAt": "2024-01-01T10:00:00",
      "isProcessed": false
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": false,
      "unsorted": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1
}
```

#### 5. Delete File

**Endpoint:** `DELETE /{fileId}`

**Description:** Delete a file

**cURL Command:**
```bash
curl -X DELETE http://localhost:8083/api/files/file-uuid-here \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```
HTTP 204 No Content
```

#### 6. Update File Description

**Endpoint:** `PUT /{fileId}/description`

**Description:** Update file description

**Request Body:** Plain text string

**cURL Command:**
```bash
curl -X PUT http://localhost:8083/api/files/file-uuid-here/description \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: text/plain" \
  -d "Updated description for my video file"
```

**Response:**
```json
{
  "id": "file-uuid",
  "userId": "user-uuid",
  "originalFilename": "video.mp4",
  "storedFilename": "stored-uuid.mp4",
  "fileSize": 52428800,
  "contentType": "video/mp4",
  "description": "Updated description for my video file",
  "uploadedAt": "2024-01-01T10:00:00",
  "isProcessed": false
}
```

#### 7. Health Check

**Endpoint:** `GET /health`

**Description:** Check if file service is running

**cURL Command:**
```bash
curl -X GET http://localhost:8083/api/files/health
```

**Response:**
```
File Service is running
```

## Notification Service (Port 8084)

### Base URL: `http://localhost:8084/api/notifications`

**Note:** All endpoints require JWT authentication. Include the token in the Authorization header.

#### 1. Get User Notifications

**Endpoint:** `GET /`

**Description:** Get paginated list of user notifications

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)
- `status` (optional): Filter by status (`UNREAD`, `READ`)

**cURL Commands:**

Get all notifications:
```bash
curl -X GET "http://localhost:8084/api/notifications?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

Get unread notifications:
```bash
curl -X GET "http://localhost:8084/api/notifications?status=UNREAD&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "content": [
    {
      "id": "notification-uuid",
      "userId": "user-uuid",
      "title": "Transcoding Job Completed",
      "message": "Your video 'output_video.mp4' has been successfully transcoded.",
      "status": "UNREAD",
      "type": "JOB_COMPLETED",
      "createdAt": "2024-01-01T10:05:00",
      "readAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": false,
      "unsorted": true
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1
}
```

#### 2. Mark Notification as Read

**Endpoint:** `PATCH /{notificationId}/read`

**Description:** Mark a specific notification as read

**cURL Command:**
```bash
curl -X PATCH http://localhost:8084/api/notifications/notification-uuid-here/read \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "id": "notification-uuid",
  "userId": "user-uuid",
  "title": "Transcoding Job Completed",
  "message": "Your video 'output_video.mp4' has been successfully transcoded.",
  "status": "READ",
  "type": "JOB_COMPLETED",
  "createdAt": "2024-01-01T10:05:00",
  "readAt": "2024-01-01T10:10:00"
}
```

#### 3. Mark All Notifications as Read

**Endpoint:** `PATCH /read-all`

**Description:** Mark all user notifications as read

**cURL Command:**
```bash
curl -X PATCH http://localhost:8084/api/notifications/read-all \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```
HTTP 204 No Content
```

#### 4. Get Unread Count

**Endpoint:** `GET /unread-count`

**Description:** Get count of unread notifications

**cURL Command:**
```bash
curl -X GET http://localhost:8084/api/notifications/unread-count \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
5
```

#### 5. Delete Notification

**Endpoint:** `DELETE /{notificationId}`

**Description:** Delete a specific notification

**cURL Command:**
```bash
curl -X DELETE http://localhost:8084/api/notifications/notification-uuid-here \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```
HTTP 204 No Content
```

#### 6. Health Check

**Endpoint:** `GET /health`

**Description:** Check if notification service is running

**cURL Command:**
```bash
curl -X GET http://localhost:8084/api/notifications/health
```

**Response:**
```
Notification Service is running
```

## Environment Variables

### Docker Compose Environment

The services are configured with the following environment variables in `docker-compose.yml`:

```yaml
# Database
POSTGRES_HOST: postgres
POSTGRES_PORT: 5432
POSTGRES_DB: transcode_db
POSTGRES_USER: transcode_user
POSTGRES_PASSWORD: secure_password_123

# Redis
REDIS_HOST: redis
REDIS_PORT: 6379
REDIS_PASSWORD: redis_password_123

# JWT
JWT_SECRET: your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRATION: 86400000

# MinIO
MINIO_ENDPOINT: http://minio:9000
MINIO_ACCESS_KEY: minioadmin
MINIO_SECRET_KEY: minioadmin123
MINIO_BUCKET_NAME: video-files

# Kafka
KAFKA_BOOTSTRAP_SERVERS: kafka:29092

# FFmpeg
FFMPEG_PATH: /usr/local/bin/ffmpeg
FFPROBE_PATH: /usr/local/bin/ffprobe
MAX_CONCURRENT_JOBS: 4
ENABLE_GPU_ACCELERATION: false
```

## Common Headers

### Required Headers

```bash
# For JSON requests
Content-Type: application/json

# For file uploads
Content-Type: multipart/form-data

# For authentication (all protected endpoints)
Authorization: Bearer YOUR_JWT_TOKEN
```

### Optional Headers

```bash
# For file downloads
Accept: application/octet-stream

# For pagination
X-Page: 0
X-Size: 10
```

## Error Handling

### Common HTTP Status Codes

| Status Code | Description | Common Causes |
|-------------|-------------|---------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 204 | No Content | Request successful, no content returned |
| 400 | Bad Request | Invalid request data or parameters |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 422 | Unprocessable Entity | Validation errors |
| 500 | Internal Server Error | Server-side error |

### Error Response Format

```json
{
  "timestamp": "2024-01-01T10:00:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "details": [
    {
      "field": "username",
      "message": "Username is required"
    },
    {
      "field": "email",
      "message": "Email should be valid"
    }
  ]
}
```

## Testing Workflow

### Complete Testing Sequence

1. **Start Services**
   ```bash
   docker-compose up -d
   ```

2. **Register User**
   ```bash
   curl -X POST http://localhost:8081/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "email": "test@example.com",
       "password": "password123"
     }'
   ```

3. **Login and Get Token**
   ```bash
   curl -X POST http://localhost:8081/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "password": "password123"
     }'
   ```

4. **Upload Video File**
   ```bash
   curl -X POST http://localhost:8083/api/files/upload \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -F "file=@/path/to/video.mp4" \
     -F "description=Test video"
   ```

5. **Create Transcoding Job**
   ```bash
   curl -X POST http://localhost:8082/api/transcode/jobs \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{
       "inputFileId": "file-uuid-from-upload",
       "outputFilename": "output.mp4",
       "outputSettings": {
         "videoCodec": "libx264",
         "audioCodec": "aac",
         "outputFormat": "mp4",
         "videoBitrate": "1500k",
         "audioBitrate": "128k",
         "resolution": "1280x720",
         "frameRate": 30,
         "processingMode": "CPU"
       }
     }'
   ```

6. **Check Job Status**
   ```bash
   curl -X GET http://localhost:8082/api/transcode/jobs/job-uuid \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

7. **Get Notifications**
   ```bash
   curl -X GET http://localhost:8084/api/notifications \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"
   ```

8. **Download Processed File**
   ```bash
   curl -X GET http://localhost:8083/api/files/file-uuid/download \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -o processed_video.mp4
   ```

### Health Check All Services

```bash
# Check all services
curl http://localhost:8081/api/auth/health
curl http://localhost:8082/api/transcode/health
curl http://localhost:8083/api/files/health
curl http://localhost:8084/api/notifications/health
```

## Advanced Usage Examples

### High-Quality 4K Transcoding

```bash
curl -X POST http://localhost:8082/api/transcode/jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inputFileId": "file-uuid",
    "outputFilename": "4k_output.mp4",
    "outputSettings": {
      "videoCodec": "libx265",
      "audioCodec": "aac",
      "outputFormat": "mp4",
      "videoBitrate": "8000k",
      "audioBitrate": "256k",
      "resolution": "3840x2160",
      "frameRate": 60,
      "processingMode": "GPU"
    },
    "priority": "HIGH"
  }'
```

### WebM for Web Streaming

```bash
curl -X POST http://localhost:8082/api/transcode/jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inputFileId": "file-uuid",
    "outputFilename": "web_stream.webm",
    "outputSettings": {
      "videoCodec": "libvpx-vp9",
      "audioCodec": "opus",
      "outputFormat": "webm",
      "videoBitrate": "2000k",
      "audioBitrate": "128k",
      "resolution": "1920x1080",
      "frameRate": 30,
      "processingMode": "CPU"
    }
  }'
```

### Batch Processing Multiple Files

```bash
# Upload multiple files
for file in video1.mp4 video2.mp4 video3.mp4; do
  curl -X POST http://localhost:8083/api/files/upload \
    -H "Authorization: Bearer YOUR_JWT_TOKEN" \
    -F "file=@$file" \
    -F "description=Batch upload $file"
done

# Create jobs for each file
curl -X POST http://localhost:8082/api/transcode/jobs \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inputFileId": "file1-uuid",
    "outputFilename": "processed1.mp4",
    "outputSettings": {
      "videoCodec": "libx264",
      "audioCodec": "aac",
      "outputFormat": "mp4",
      "videoBitrate": "1500k",
      "audioBitrate": "128k",
      "resolution": "1280x720",
      "frameRate": 30,
      "processingMode": "CPU"
    }
  }'
```

This comprehensive Postman collection provides all the necessary curl commands and detailed documentation for testing and using the Video Transcoding Service microservices architecture.
```

I've created a comprehensive `postman-collection.md` file that includes:

## Key Features:

1. **Complete API Documentation** - All endpoints for all 4 microservices with detailed descriptions
2. **Detailed cURL Commands** - Ready-to-use curl commands for every endpoint
3. **Request/Response Examples** - Complete JSON examples for all requests and responses
4. **Validation Rules** - Field validation requirements and constraints
5. **Error Handling** - Common HTTP status codes and error response formats
6. **Environment Configuration** - Docker Compose environment variables
7. **Testing Workflow** - Complete step-by-step testing sequence
8. **Advanced Examples** - High-quality transcoding, WebM streaming, batch processing

## Services Covered:

- **Authentication Service (Port 8081)**: Registration, login, health check
- **Transcoding Service (Port 8082)**: Job creation, monitoring, system info, bulk operations
- **File Management Service (Port 8083)**: Upload, download, file management, metadata updates
- **Notification Service (Port 8084)**: Notifications, read status, unread counts

## Additional Features:

- JWT authentication examples
- Pagination support
- File upload with multipart/form-data
- Query parameter filtering
- Health check endpoints
- Complete error handling documentation
- Real-world usage examples for different video formats and quality settings

The documentation is production-ready and provides everything needed to test and integrate with the video transcoding microservices architecture.