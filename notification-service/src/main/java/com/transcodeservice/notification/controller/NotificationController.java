package com.transcodeservice.notification.controller;

import com.transcodeservice.common.entity.Notification;
import com.transcodeservice.common.entity.User;
import com.transcodeservice.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<Page<Notification>> getUserNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        log.info("Getting notifications for user: {} with page: {}, size: {}, status: {}", 
                user.getUsername(), page, size, status);
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Notification> notifications;
        if (status != null) {
            try {
                Notification.NotificationStatus notificationStatus = Notification.NotificationStatus.valueOf(status.toUpperCase());
                notifications = notificationService.getUserNotificationsByStatus(user.getId(), notificationStatus, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            notifications = notificationService.getUserNotifications(user.getId(), pageable);
        }
        
        return ResponseEntity.ok(notifications);
    }
    
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable String notificationId) {
        
        log.info("Marking notification as read: {} for user: {}", notificationId, user.getUsername());
        Notification notification = notificationService.markAsRead(notificationId, user.getId());
        return ResponseEntity.ok(notification);
    }
    
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        log.info("Marking all notifications as read for user: {}", user.getUsername());
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal User user) {
        log.info("Getting unread count for user: {}", user.getUsername());
        Long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(count);
    }
    
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal User user,
            @PathVariable String notificationId) {
        
        log.info("Deleting notification: {} for user: {}", notificationId, user.getUsername());
        notificationService.deleteNotification(notificationId, user.getId());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
