package com.transcodeservice.file.controller;

import com.transcodeservice.common.entity.User;
import com.transcodeservice.common.entity.VideoFile;
import com.transcodeservice.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FileController {
    
    private final FileService fileService;
    
    @PostMapping("/upload")
    public ResponseEntity<VideoFile> uploadFile(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        
        log.info("File upload request received for user: {}", user.getUsername());
        VideoFile uploadedFile = fileService.uploadFile(user, file, description);
        return ResponseEntity.ok(uploadedFile);
    }
    
    @GetMapping("/{fileId}")
    public ResponseEntity<VideoFile> getFile(
            @AuthenticationPrincipal User user,
            @PathVariable String fileId) {
        
        log.info("File info request received for file: {} and user: {}", fileId, user.getUsername());
        VideoFile file = fileService.getFile(fileId, user.getId());
        return ResponseEntity.ok(file);
    }
    
    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            @AuthenticationPrincipal User user,
            @PathVariable String fileId) {
        
        log.info("File download request received for file: {} and user: {}", fileId, user.getUsername());
        
        VideoFile file = fileService.getFile(fileId, user.getId());
        var inputStream = fileService.downloadFile(fileId, user.getId());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", file.getOriginalFilename());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));
    }
    
    @GetMapping
    public ResponseEntity<Page<VideoFile>> getUserFiles(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Getting files for user: {} with page: {}, size: {}", user.getUsername(), page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoFile> files = fileService.getUserFiles(user.getId(), pageable);
        
        return ResponseEntity.ok(files);
    }
    
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @AuthenticationPrincipal User user,
            @PathVariable String fileId) {
        
        log.info("File deletion request received for file: {} and user: {}", fileId, user.getUsername());
        fileService.deleteFile(fileId, user.getId());
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{fileId}/description")
    public ResponseEntity<VideoFile> updateFileDescription(
            @AuthenticationPrincipal User user,
            @PathVariable String fileId,
            @RequestBody String description) {
        
        log.info("File description update request received for file: {} and user: {}", fileId, user.getUsername());
        VideoFile updatedFile = fileService.updateFileDescription(fileId, user.getId(), description);
        return ResponseEntity.ok(updatedFile);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("File Service is running");
    }
}
