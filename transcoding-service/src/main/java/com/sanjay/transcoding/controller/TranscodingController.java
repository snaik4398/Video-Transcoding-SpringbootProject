package com.sanjay.transcoding.controller;

import com.sanjay.common.dto.TranscodingJobDto;
import com.sanjay.common.entity.TranscodingJob;
import com.sanjay.common.entity.User;
import com.sanjay.common.util.JwtTokenHelper;
import com.sanjay.transcoding.dto.SystemInfo;
import com.sanjay.transcoding.service.TranscodingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transcode")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TranscodingController {
    
    private final TranscodingService transcodingService;
    private final JwtTokenHelper jwtTokenHelper;
    
    @GetMapping("/system-info")
    public ResponseEntity<SystemInfo> getSystemInfo() {
        log.info("System info request received");
        SystemInfo systemInfo = transcodingService.getSystemInfo();
        return ResponseEntity.ok(systemInfo);
    }
    
    @PostMapping("/jobs")
    public ResponseEntity<TranscodingJob> createTranscodingJob(
            HttpServletRequest request,
            @Valid @RequestBody TranscodingJobDto jobDto) {
        User user = jwtTokenHelper.createUserFromToken(request);
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("Creating transcoding job for user: {} (userId: {})", user.getUsername(), user.getId());
        TranscodingJob job = transcodingService.createTranscodingJob(user, jobDto);
        return ResponseEntity.ok(job);
    }
    
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<TranscodingJob> getTranscodingJob(
            HttpServletRequest request,
            @PathVariable String jobId) {
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("Getting transcoding job: {} for user: {} (userId: {})", jobId, username, userId);
        TranscodingJob job = transcodingService.getTranscodingJob(jobId, userId);
        return ResponseEntity.ok(job);
    }
    
    @GetMapping("/jobs")
    public ResponseEntity<Page<TranscodingJob>> getUserJobs(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("Getting jobs for user: {} (userId: {}) with page: {}, size: {}, status: {}", 
                username, userId, page, size, status);
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<TranscodingJob> jobs;
        if (status != null) {
            try {
                TranscodingJob.TranscodingStatus jobStatus = TranscodingJob.TranscodingStatus.valueOf(status.toUpperCase());
                jobs = transcodingService.getUserJobsByStatus(userId, jobStatus, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            jobs = transcodingService.getUserJobs(userId, pageable);
        }
        
        return ResponseEntity.ok(jobs);
    }
    
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Void> cancelTranscodingJob(
            HttpServletRequest request,
            @PathVariable String jobId) {
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("Cancelling transcoding job: {} for user: {} (userId: {})", jobId, username, userId);
        transcodingService.cancelTranscodingJob(jobId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<String> bulkTranscode(
            HttpServletRequest request,
            @RequestBody Object bulkRequest) {
        String username = jwtTokenHelper.getUsername();
        
        log.info("Bulk transcoding request received for user: {}", username);
        // Implementation for bulk transcoding would go here
        return ResponseEntity.ok("Bulk transcoding feature not implemented yet");
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Transcoding Service is running");
    }
}
