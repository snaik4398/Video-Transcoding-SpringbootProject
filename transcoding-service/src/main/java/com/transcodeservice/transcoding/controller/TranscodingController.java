package com.transcodeservice.transcoding.controller;

import com.transcodeservice.common.dto.TranscodingJobDto;
import com.transcodeservice.common.entity.TranscodingJob;
import com.transcodeservice.common.entity.User;
import com.transcodeservice.transcoding.dto.SystemInfo;
import com.transcodeservice.transcoding.service.TranscodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transcode")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TranscodingController {
    
    private final TranscodingService transcodingService;
    
    @GetMapping("/system-info")
    public ResponseEntity<SystemInfo> getSystemInfo() {
        log.info("System info request received");
        SystemInfo systemInfo = transcodingService.getSystemInfo();
        return ResponseEntity.ok(systemInfo);
    }
    
    @PostMapping("/jobs")
    public ResponseEntity<TranscodingJob> createTranscodingJob(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TranscodingJobDto jobDto) {
        log.info("Creating transcoding job for user: {}", user.getUsername());
        TranscodingJob job = transcodingService.createTranscodingJob(user, jobDto);
        return ResponseEntity.ok(job);
    }
    
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<TranscodingJob> getTranscodingJob(
            @AuthenticationPrincipal User user,
            @PathVariable String jobId) {
        log.info("Getting transcoding job: {} for user: {}", jobId, user.getUsername());
        TranscodingJob job = transcodingService.getTranscodingJob(jobId, user.getId());
        return ResponseEntity.ok(job);
    }
    
    @GetMapping("/jobs")
    public ResponseEntity<Page<TranscodingJob>> getUserJobs(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        log.info("Getting jobs for user: {} with page: {}, size: {}, status: {}", 
                user.getUsername(), page, size, status);
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<TranscodingJob> jobs;
        if (status != null) {
            try {
                TranscodingJob.TranscodingStatus jobStatus = TranscodingJob.TranscodingStatus.valueOf(status.toUpperCase());
                jobs = transcodingService.getUserJobsByStatus(user.getId(), jobStatus, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            jobs = transcodingService.getUserJobs(user.getId(), pageable);
        }
        
        return ResponseEntity.ok(jobs);
    }
    
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Void> cancelTranscodingJob(
            @AuthenticationPrincipal User user,
            @PathVariable String jobId) {
        log.info("Cancelling transcoding job: {} for user: {}", jobId, user.getUsername());
        transcodingService.cancelTranscodingJob(jobId, user.getId());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<String> bulkTranscode(
            @AuthenticationPrincipal User user,
            @RequestBody Object bulkRequest) {
        log.info("Bulk transcoding request received for user: {}", user.getUsername());
        // Implementation for bulk transcoding would go here
        return ResponseEntity.ok("Bulk transcoding feature not implemented yet");
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Transcoding Service is running");
    }
}
