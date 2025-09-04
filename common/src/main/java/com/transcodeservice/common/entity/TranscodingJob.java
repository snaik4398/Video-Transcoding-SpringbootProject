package com.transcodeservice.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transcoding_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscodingJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_file_id", nullable = false)
    private VideoFile inputFile;
    
    @Column(name = "output_filename")
    private String outputFilename;
    
    @Column(name = "output_file_path")
    private String outputFilePath;
    
    @Column(name = "output_object_key")
    private String outputObjectKey;
    
    @Enumerated(EnumType.STRING)
    private TranscodingStatus status;
    
    @Enumerated(EnumType.STRING)
    private JobPriority priority;
    
    @Column(name = "video_codec")
    private String videoCodec;
    
    @Column(name = "audio_codec")
    private String audioCodec;
    
    @Column(name = "output_format")
    private String outputFormat;
    
    @Column(name = "video_bitrate")
    private String videoBitrate;
    
    @Column(name = "audio_bitrate")
    private String audioBitrate;
    
    @Column(name = "resolution")
    private String resolution;
    
    @Column(name = "frame_rate")
    private Integer frameRate;
    
    @Column(name = "processing_mode")
    private String processingMode;
    
    @Column(name = "progress_percentage")
    private Integer progressPercentage;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "estimated_duration")
    private Long estimatedDuration;
    
    @Column(name = "actual_duration")
    private Long actualDuration;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = TranscodingStatus.QUEUED;
        progressPercentage = 0;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum TranscodingStatus {
        QUEUED, PROCESSING, COMPLETED, FAILED, CANCELLED
    }
    
    public enum JobPriority {
        LOW, NORMAL, HIGH, URGENT
    }
}
