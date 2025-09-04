package com.transcodeservice.transcoding.service;

import com.transcodeservice.common.dto.TranscodingJobDto;
import com.transcodeservice.common.entity.TranscodingJob;
import com.transcodeservice.common.entity.User;
import com.transcodeservice.common.entity.VideoFile;
import com.transcodeservice.transcoding.dto.SystemInfo;
import com.transcodeservice.transcoding.repository.TranscodingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscodingService {
    
    private final TranscodingJobRepository jobRepository;
    private final FFmpegService ffmpegService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${transcoding.processing.max-concurrent-jobs:4}")
    private int maxConcurrentJobs;
    
    @Value("${transcoding.processing.temp-directory:/tmp/transcode}")
    private String tempDirectory;
    
    @Value("${transcoding.processing.output-directory:/app/output}")
    private String outputDirectory;
    
    public TranscodingJob createTranscodingJob(User user, TranscodingJobDto jobDto) {
        log.info("Creating transcoding job for user: {} and file: {}", user.getUsername(), jobDto.getInputFileId());
        
        // Create the job entity
        TranscodingJob job = TranscodingJob.builder()
                .user(user)
                .inputFile(VideoFile.builder().id(jobDto.getInputFileId()).build()) // Simplified for demo
                .outputFilename(generateOutputFilename(jobDto))
                .videoCodec(jobDto.getOutputSettings().getVideoCodec())
                .audioCodec(jobDto.getOutputSettings().getAudioCodec())
                .outputFormat(jobDto.getOutputSettings().getOutputFormat())
                .videoBitrate(jobDto.getOutputSettings().getVideoBitrate())
                .audioBitrate(jobDto.getOutputSettings().getAudioBitrate())
                .resolution(jobDto.getOutputSettings().getResolution())
                .frameRate(jobDto.getOutputSettings().getFrameRate())
                .processingMode(jobDto.getOutputSettings().getProcessingMode())
                .priority(jobDto.getPriority())
                .status(TranscodingJob.TranscodingStatus.QUEUED)
                .progressPercentage(0)
                .build();
        
        TranscodingJob savedJob = jobRepository.save(job);
        
        // Send to Kafka for processing
        sendToKafka(savedJob);
        
        return savedJob;
    }
    
    public TranscodingJob getTranscodingJob(String jobId, String userId) {
        return jobRepository.findById(jobId)
                .filter(job -> job.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }
    
    public Page<TranscodingJob> getUserJobs(String userId, Pageable pageable) {
        return jobRepository.findByUserId(userId, pageable);
    }
    
    public Page<TranscodingJob> getUserJobsByStatus(String userId, TranscodingJob.TranscodingStatus status, Pageable pageable) {
        return jobRepository.findByUserIdAndStatus(userId, status, pageable);
    }
    
    public void cancelTranscodingJob(String jobId, String userId) {
        TranscodingJob job = getTranscodingJob(jobId, userId);
        
        if (job.getStatus() == TranscodingJob.TranscodingStatus.QUEUED) {
            job.setStatus(TranscodingJob.TranscodingStatus.CANCELLED);
            jobRepository.save(job);
            log.info("Job cancelled: {}", jobId);
        } else {
            throw new RuntimeException("Cannot cancel job in status: " + job.getStatus());
        }
    }
    
    public SystemInfo getSystemInfo() {
        return SystemInfo.builder()
                .cpuInfo(System.getProperty("os.arch"))
                .gpuInfo("NVIDIA GPU (if available)")
                .totalMemory(Runtime.getRuntime().totalMemory())
                .availableMemory(Runtime.getRuntime().freeMemory())
                .cpuCores(Runtime.getRuntime().availableProcessors())
                .ffmpegVersion(ffmpegService.getFFmpegVersion())
                .ffprobeVersion(ffmpegService.getFFprobeVersion())
                .gpuAccelerationEnabled(false) // Simplified for demo
                .build();
    }
    
    public List<TranscodingJob> getQueuedJobs() {
        return jobRepository.findQueuedJobsOrderByPriorityAndCreatedAt();
    }
    
    public void processTranscodingJob(TranscodingJob job) {
        try {
            log.info("Processing transcoding job: {}", job.getId());
            
            job.setStatus(TranscodingJob.TranscodingStatus.PROCESSING);
            job.setStartedAt(LocalDateTime.now());
            jobRepository.save(job);
            
            // Simulate file processing (in real implementation, download from MinIO)
            File inputFile = new File(tempDirectory, "input_" + job.getId() + ".mp4");
            File outputFile = new File(outputDirectory, job.getOutputFilename());
            
            // Ensure directories exist
            outputFile.getParentFile().mkdirs();
            
            // Perform transcoding
            boolean success = ffmpegService.transcode(inputFile, outputFile, 
                TranscodingJobDto.OutputSettingsDto.builder()
                    .videoCodec(job.getVideoCodec())
                    .audioCodec(job.getAudioCodec())
                    .outputFormat(job.getOutputFormat())
                    .videoBitrate(job.getVideoBitrate())
                    .audioBitrate(job.getAudioBitrate())
                    .resolution(job.getResolution())
                    .frameRate(job.getFrameRate())
                    .processingMode(job.getProcessingMode())
                    .build());
            
            if (success) {
                job.setStatus(TranscodingJob.TranscodingStatus.COMPLETED);
                job.setProgressPercentage(100);
                job.setCompletedAt(LocalDateTime.now());
                log.info("Job completed successfully: {}", job.getId());
            } else {
                job.setStatus(TranscodingJob.TranscodingStatus.FAILED);
                job.setErrorMessage("Transcoding failed");
                log.error("Job failed: {}", job.getId());
            }
            
            jobRepository.save(job);
            
        } catch (Exception e) {
            log.error("Error processing job: {}", job.getId(), e);
            job.setStatus(TranscodingJob.TranscodingStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            jobRepository.save(job);
        }
    }
    
    private String generateOutputFilename(TranscodingJobDto jobDto) {
        String baseName = UUID.randomUUID().toString();
        String extension = jobDto.getOutputSettings().getOutputFormat();
        return baseName + "." + extension;
    }
    
    private void sendToKafka(TranscodingJob job) {
        try {
            kafkaTemplate.send("transcoding-jobs", job);
            log.info("Job sent to Kafka: {}", job.getId());
        } catch (Exception e) {
            log.error("Failed to send job to Kafka: {}", job.getId(), e);
        }
    }
}
