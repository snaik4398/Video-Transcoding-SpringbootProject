package com.sanjay.transcoding.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanjay.common.dto.TranscodingJobDto;
import com.sanjay.common.entity.TranscodingJob;
import com.sanjay.common.entity.User;
import com.sanjay.common.entity.VideoFile;
import com.sanjay.transcoding.dto.SystemInfo;
import com.sanjay.transcoding.repository.TranscodingJobRepository;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscodingService {

    private final TranscodingJobRepository jobRepository;
    private final FFmpegService ffmpegService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MinioClient minioClient;
    private final ObjectMapper objectMapper;

    @Value("${transcoding.processing.max-concurrent-jobs:4}")
    private int maxConcurrentJobs;

    @Value("${transcoding.processing.temp-directory:/tmp/transcode}")
    private String tempDirectory;

    @Value("${transcoding.processing.output-directory:/app/output}")
    private String outputDirectory;

    @Value("${transcoding.processing.cleanup-temp-files:true}")
    private boolean cleanupTempFiles;

    @Value("${storage.minio.bucket-name:video-files}")
    private String inputBucketName;

    @Value("${storage.minio.output-bucket-name:transcoded-files}")
    private String outputBucketName;

    @Value("${WORKER_ID:default-worker}")
    private String workerId;

    private Semaphore concurrencyLimiter;
    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        concurrencyLimiter = new Semaphore(maxConcurrentJobs);
        executorService = Executors.newFixedThreadPool(maxConcurrentJobs);

        new File(tempDirectory).mkdirs();
        new File(outputDirectory).mkdirs();

        log.info("TranscodingService initialized: maxConcurrentJobs={}, tempDir={}, outputDir={}",
                maxConcurrentJobs, tempDirectory, outputDirectory);
    }

    public TranscodingJob createTranscodingJob(User user, TranscodingJobDto jobDto) {
        log.info("Creating transcoding job for user: {} and file: {}", user.getUsername(), jobDto.getInputFileId());

        TranscodingJob job = TranscodingJob.builder()
                .user(user)
                .inputFile(VideoFile.builder().id(jobDto.getInputFileId()).build())
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
        sendToKafka(savedJob);
        return savedJob;
    }

    @KafkaListener(topics = "transcoding-jobs", groupId = "transcode-service-group")
    public void onTranscodingJobReceived(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            String jobId = (String) payload.get("id");

            if (jobId == null) {
                log.warn("Received Kafka message without job ID, ignoring");
                return;
            }

            TranscodingJob job = jobRepository.findById(jobId).orElse(null);
            if (job == null) {
                log.warn("Job {} not found in database, ignoring", jobId);
                return;
            }

            if (job.getStatus() != TranscodingJob.TranscodingStatus.QUEUED) {
                log.info("Job {} is not in QUEUED status (current: {}), skipping", jobId, job.getStatus());
                return;
            }

            log.info("Received transcoding job from Kafka: {}", jobId);
            executorService.submit(() -> processTranscodingJob(job));

        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }

    public void processTranscodingJob(TranscodingJob job) {
        File inputFile = null;
        File outputFile = null;

        try {
            concurrencyLimiter.acquire();
            log.info("Processing transcoding job: {}", job.getId());

            job.setStatus(TranscodingJob.TranscodingStatus.PROCESSING);
            job.setStartedAt(LocalDateTime.now());
            job.setProgressPercentage(5);
            jobRepository.save(job);

            String objectKey = resolveObjectKey(job);
            String inputExtension = getExtension(objectKey);
            inputFile = new File(tempDirectory, "input_" + job.getId() + inputExtension);
            outputFile = new File(outputDirectory, job.getOutputFilename());
            outputFile.getParentFile().mkdirs();

            log.info("Downloading input file from MinIO: bucket={}, key={}", inputBucketName, objectKey);
            try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(inputBucketName)
                    .object(objectKey)
                    .build())) {
                Files.copy(stream, inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            job.setProgressPercentage(15);
            jobRepository.save(job);

            log.info("Starting FFmpeg transcoding: {} -> {}", inputFile.getName(), outputFile.getName());

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

            if (success && outputFile.exists() && outputFile.length() > 0) {
                job.setProgressPercentage(85);
                jobRepository.save(job);

                String outputObjectKey = "transcoded/" + job.getId() + "/" + job.getOutputFilename();
                log.info("Uploading transcoded file to MinIO: bucket={}, key={}", outputBucketName, outputObjectKey);

                try (FileInputStream fis = new FileInputStream(outputFile)) {
                    minioClient.putObject(PutObjectArgs.builder()
                            .bucket(outputBucketName)
                            .object(outputObjectKey)
                            .stream(fis, outputFile.length(), -1)
                            .contentType("video/" + job.getOutputFormat())
                            .build());
                }

                job.setOutputObjectKey(outputObjectKey);
                job.setOutputFilePath("/" + outputBucketName + "/" + outputObjectKey);
                job.setStatus(TranscodingJob.TranscodingStatus.COMPLETED);
                job.setProgressPercentage(100);
                job.setCompletedAt(LocalDateTime.now());

                Duration duration = Duration.between(job.getStartedAt(), job.getCompletedAt());
                job.setActualDuration(duration.getSeconds());

                log.info("Job completed successfully: {} (duration: {}s)", job.getId(), duration.getSeconds());

                publishJobStatusEvent(job, "COMPLETED");
            } else {
                job.setStatus(TranscodingJob.TranscodingStatus.FAILED);
                job.setErrorMessage("Transcoding failed: output file not created or empty");
                log.error("Job failed: {}", job.getId());
                publishJobStatusEvent(job, "FAILED");
            }

            jobRepository.save(job);

        } catch (Exception e) {
            log.error("Error processing job {}: {}", job.getId(), e.getMessage(), e);
            job.setStatus(TranscodingJob.TranscodingStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            publishJobStatusEvent(job, "FAILED");
        } finally {
            concurrencyLimiter.release();
            if (cleanupTempFiles) {
                cleanupFile(inputFile);
                cleanupFile(outputFile);
            }
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void processStaleJobs() {
        List<TranscodingJob> staleJobs = jobRepository.findByStatus(TranscodingJob.TranscodingStatus.PROCESSING);
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);

        for (TranscodingJob job : staleJobs) {
            if (job.getStartedAt() != null && job.getStartedAt().isBefore(threshold)) {
                log.warn("Marking stale job as FAILED: {} (started at {})", job.getId(), job.getStartedAt());
                job.setStatus(TranscodingJob.TranscodingStatus.FAILED);
                job.setErrorMessage("Job timed out after 2 hours");
                job.setCompletedAt(LocalDateTime.now());
                jobRepository.save(job);
            }
        }
    }

    public TranscodingJob getTranscodingJob(String jobId, String userId) {
        return jobRepository.findById(jobId)
                .filter(job -> job.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Job not found or access denied"));
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
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
            log.info("Job cancelled: {}", jobId);
        } else {
            throw new RuntimeException("Cannot cancel job in status: " + job.getStatus());
        }
    }

    public SystemInfo getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        Long activeJobs = jobRepository.countActiveJobs();
        boolean gpuAvailable = ffmpegService.isGpuAvailable();

        return SystemInfo.builder()
                .cpuInfo(System.getProperty("os.arch") + " " + System.getProperty("os.name"))
                .gpuInfo(gpuAvailable ? ffmpegService.getGpuInfo() : "No GPU detected")
                .totalMemory(maxMemory)
                .availableMemory(maxMemory - usedMemory)
                .cpuCores(runtime.availableProcessors())
                .cpuUsage(null)
                .memoryUsage((double) usedMemory / maxMemory * 100)
                .ffmpegVersion(ffmpegService.getFFmpegVersion())
                .ffprobeVersion(ffmpegService.getFFprobeVersion())
                .gpuAccelerationEnabled(gpuAvailable)
                .gpuType(ffmpegService.getGpuType())
                .activeJobs(activeJobs)
                .maxConcurrentJobs(maxConcurrentJobs)
                .workerId(workerId)
                .build();
    }

    public List<TranscodingJob> getQueuedJobs() {
        return jobRepository.findQueuedJobsOrderByPriorityAndCreatedAt();
    }

    private String resolveObjectKey(TranscodingJob job) {
        if (job.getInputFile() != null && job.getInputFile().getObjectKey() != null) {
            return job.getInputFile().getObjectKey();
        }
        return job.getInputFile().getId();
    }

    private String generateOutputFilename(TranscodingJobDto jobDto) {
        String baseName = UUID.randomUUID().toString();
        String extension = jobDto.getOutputSettings().getOutputFormat();
        return baseName + "." + extension;
    }

    private String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return ".mp4";
    }

    private void sendToKafka(TranscodingJob job) {
        try {
            String json = objectMapper.writeValueAsString(job);
            kafkaTemplate.send("transcoding-jobs", job.getId(), json);
            log.info("Job sent to Kafka: {}", job.getId());
        } catch (Exception e) {
            log.error("Failed to send job to Kafka: {}", job.getId(), e);
        }
    }

    private void publishJobStatusEvent(TranscodingJob job, String status) {
        try {
            Map<String, Object> event = Map.of(
                    "jobId", job.getId(),
                    "userId", job.getUser().getId(),
                    "status", status,
                    "outputFilename", job.getOutputFilename() != null ? job.getOutputFilename() : "",
                    "timestamp", LocalDateTime.now().toString()
            );
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("transcoding-status", job.getId(), json);
            log.info("Published job status event: jobId={}, status={}", job.getId(), status);
        } catch (Exception e) {
            log.error("Failed to publish job status event: {}", e.getMessage(), e);
        }
    }

    private void cleanupFile(File file) {
        if (file != null && file.exists()) {
            try {
                Files.deleteIfExists(file.toPath());
                log.debug("Cleaned up temp file: {}", file.getAbsolutePath());
            } catch (Exception e) {
                log.warn("Failed to cleanup temp file: {}", file.getAbsolutePath());
            }
        }
    }
}
