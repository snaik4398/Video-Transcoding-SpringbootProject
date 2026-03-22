package com.sanjay.file.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanjay.common.entity.User;
import com.sanjay.common.entity.VideoFile;
import com.sanjay.file.repository.VideoFileRepository;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final VideoFileRepository fileRepository;
    private final MinioClient minioClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${storage.minio.bucket-name:video-files}")
    private String bucketName;

    @PostConstruct
    public void init() {
        ensureBucketExists();
    }

    public VideoFile uploadFile(User user, MultipartFile file, String description) {
        try {
            log.info("Uploading file: {} ({} bytes) for user: {}",
                    file.getOriginalFilename(), file.getSize(), user.getUsername());

            String objectKey = generateObjectKey(file.getOriginalFilename());

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            VideoFile videoFile = VideoFile.builder()
                    .filename(file.getOriginalFilename())
                    .originalFilename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .filePath("/" + bucketName + "/" + objectKey)
                    .bucketName(bucketName)
                    .objectKey(objectKey)
                    .description(description)
                    .user(user)
                    .build();

            VideoFile savedFile = fileRepository.save(videoFile);
            log.info("File uploaded successfully: id={}, objectKey={}", savedFile.getId(), objectKey);

            publishFileUploadEvent(savedFile);

            return savedFile;

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    public VideoFile getFile(String fileId, String userId) {
        return fileRepository.findById(fileId)
                .filter(file -> file.getUser().getId().equals(userId) && !file.isDeleted())
                .orElseThrow(() -> new RuntimeException("File not found"));
    }

    public Page<VideoFile> getUserFiles(String userId, Pageable pageable) {
        return fileRepository.findByUserIdAndIsDeletedFalse(userId, pageable);
    }

    public InputStream downloadFile(String fileId, String userId) {
        VideoFile file = getFile(fileId, userId);

        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(file.getBucketName())
                    .object(file.getObjectKey())
                    .build());
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public void deleteFile(String fileId, String userId) {
        VideoFile file = getFile(fileId, userId);

        try {
            file.setDeleted(true);
            fileRepository.save(file);

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(file.getBucketName())
                    .object(file.getObjectKey())
                    .build());

            log.info("File deleted: {}", fileId);

        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    public VideoFile updateFileDescription(String fileId, String userId, String description) {
        VideoFile file = getFile(fileId, userId);
        file.setDescription(description);
        return fileRepository.save(file);
    }

    private void publishFileUploadEvent(VideoFile videoFile) {
        try {
            Map<String, Object> event = Map.of(
                    "fileId", videoFile.getId(),
                    "userId", videoFile.getUser().getId(),
                    "filename", videoFile.getOriginalFilename(),
                    "fileSize", videoFile.getFileSize(),
                    "contentType", videoFile.getContentType(),
                    "objectKey", videoFile.getObjectKey(),
                    "bucketName", videoFile.getBucketName()
            );
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("file-uploads", videoFile.getId(), json);
            log.info("Published file upload event for file: {}", videoFile.getId());
        } catch (Exception e) {
            log.warn("Failed to publish file upload event: {}", e.getMessage());
        }
    }

    private String generateObjectKey(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private void ensureBucketExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("Could not verify/create bucket '{}': {}. Will retry on first upload.", bucketName, e.getMessage());
        }
    }
}
