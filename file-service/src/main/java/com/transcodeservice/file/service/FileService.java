package com.transcodeservice.file.service;

import com.transcodeservice.common.entity.User;
import com.transcodeservice.common.entity.VideoFile;
import com.transcodeservice.file.repository.VideoFileRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    
    private final VideoFileRepository fileRepository;
    private final MinioClient minioClient;
    
    @Value("${storage.minio.bucket-name:video-files}")
    private String bucketName;
    
    public VideoFile uploadFile(User user, MultipartFile file, String description) {
        try {
            log.info("Uploading file: {} for user: {}", file.getOriginalFilename(), user.getUsername());
            
            // Generate unique object key
            String objectKey = generateObjectKey(file.getOriginalFilename());
            
            // Upload to MinIO
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            // Save file metadata to database
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
            log.info("File uploaded successfully: {}", savedFile.getId());
            
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
            // Mark as deleted in database
            file.setDeleted(true);
            fileRepository.save(file);
            
            // Optionally delete from MinIO (or keep for backup)
            // minioClient.removeObject(RemoveObjectArgs.builder()
            //     .bucket(file.getBucketName())
            //     .object(file.getObjectKey())
            //     .build());
            
            log.info("File marked as deleted: {}", fileId);
            
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
    
    private String generateObjectKey(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
    
    public void ensureBucketExists() {
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
            log.error("Error ensuring bucket exists: {}", e.getMessage(), e);
        }
    }
}
