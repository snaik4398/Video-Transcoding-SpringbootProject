package com.sanjay.notification.controller;

import com.sanjay.common.entity.Notification;
import com.sanjay.common.util.JwtTokenHelper;
import com.sanjay.notification.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final JwtTokenHelper jwtTokenHelper;
    
    @GetMapping
    public ResponseEntity<Page<Notification>> getUserNotifications(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            log.error("Cannot extract userId from token");
            return ResponseEntity.status(401).build();
        }
        
        log.info("Getting notifications for user: {} (userId: {}) with page: {}, size: {}, status: {}", 
                username, userId, page, size, status);
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Notification> notifications;
        if (status != null) {
            try {
                Notification.NotificationStatus notificationStatus = Notification.NotificationStatus.valueOf(status.toUpperCase());
                notifications = notificationService.getUserNotificationsByStatus(userId, notificationStatus, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            notifications = notificationService.getUserNotifications(userId, pageable);
        }
        
        return ResponseEntity.ok(notifications);
    }
    
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(
            HttpServletRequest request,
            @PathVariable String notificationId) {
        
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("Marking notification as read: {} for user: {} (userId: {})", notificationId, username, userId);
        Notification notification = notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok(notification);
    }
    
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(HttpServletRequest request) {
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("Marking all notifications as read for user: {} (userId: {})", username, userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(HttpServletRequest request) {
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("Getting unread count for user: {} (userId: {})", username, userId);
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }
    
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            HttpServletRequest request,
            @PathVariable String notificationId) {
        
        String userId = jwtTokenHelper.getUserIdFromToken(request);
        String username = jwtTokenHelper.getUsername();
        
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        
        log.info("Deleting notification: {} for user: {} (userId: {})", notificationId, username, userId);
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
