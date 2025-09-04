package com.transcodeservice.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String filename;
    
    @Column(nullable = false)
    private String originalFilename;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(nullable = false)
    private String filePath;
    
    @Column(nullable = false)
    private String bucketName;
    
    @Column(nullable = false)
    private String objectKey;
    
    @Column
    private String description;
    
    @Column(name = "duration_seconds")
    private Long durationSeconds;
    
    @Column(name = "video_codec")
    private String videoCodec;
    
    @Column(name = "audio_codec")
    private String audioCodec;
    
    @Column(name = "resolution")
    private String resolution;
    
    @Column(name = "frame_rate")
    private Double frameRate;
    
    @Column(name = "bitrate")
    private Long bitrate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_deleted")
    private boolean isDeleted = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
