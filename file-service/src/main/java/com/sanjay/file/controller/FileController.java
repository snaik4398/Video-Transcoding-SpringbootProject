package com.sanjay.file.controller;

import com.sanjay.common.entity.User;
import com.sanjay.common.entity.VideoFile;
import com.sanjay.common.util.JwtTokenHelper;
import com.sanjay.file.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FileController {
    
    private final FileService fileService;
    private final JwtTokenHelper jwtTokenHelper;
    
    @PostMapping("/upload")
    public ResponseEntity<VideoFile> uploadFile(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {
        
        User user = jwtTokenHelper.createUserFromToken(request);
        if (user == null || user.getId() == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("File upload request received for user: {} (userId: {})", user.getUsername(), user.getId());
        VideoFile uploadedFile = fileService.uploadFile(user, file, description);
        return ResponseEntity.ok(uploadedFile);
    }
    
    @GetMapping("/{fileId}")
    public ResponseEntity<VideoFile> getFile(
            HttpServletRequest request,
            @PathVariable String fileId) {
        
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("File info request received for file: {} and user: {} (userId: {})", fileId, username, userId);
        VideoFile file = fileService.getFile(fileId, userId);
        return ResponseEntity.ok(file);
    }
    
    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> downloadFile(
            HttpServletRequest request,
            @PathVariable String fileId) {
        
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("File download request received for file: {} and user: {} (userId: {})", fileId, username, userId);
        
        VideoFile file = fileService.getFile(fileId, userId);
        var inputStream = fileService.downloadFile(fileId, userId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", file.getOriginalFilename());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));
    }
    
    @GetMapping
    public ResponseEntity<Page<VideoFile>> getUserFiles(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("Getting files for user: {} (userId: {}) with page: {}, size: {}", username, userId, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<VideoFile> files = fileService.getUserFiles(userId, pageable);
        
        return ResponseEntity.ok(files);
    }
    
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            HttpServletRequest request,
            @PathVariable String fileId) {
        
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("File deletion request received for file: {} and user: {} (userId: {})", fileId, username, userId);
        fileService.deleteFile(fileId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{fileId}/description")
    public ResponseEntity<VideoFile> updateFileDescription(
            HttpServletRequest request,
            @PathVariable String fileId,
            @RequestBody String description) {
        
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("File description update request received for file: {} and user: {} (userId: {})", fileId, username, userId);
        VideoFile updatedFile = fileService.updateFileDescription(fileId, userId, description);
        return ResponseEntity.ok(updatedFile);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("File Service is running");
    }
}
