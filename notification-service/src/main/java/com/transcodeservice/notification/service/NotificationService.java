package com.transcodeservice.notification.service;

import com.transcodeservice.common.entity.Notification;
import com.transcodeservice.common.entity.User;
import com.transcodeservice.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    public Notification createNotification(User user, String title, String message, 
                                        Notification.NotificationType type, String relatedEntityId, String relatedEntityType) {
        log.info("Creating notification for user: {} with title: {}", user.getUsername(), title);
        
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .status(Notification.NotificationStatus.UNREAD)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .build();
        
        return notificationRepository.save(notification);
    }
    
    public Page<Notification> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    public Page<Notification> getUserNotificationsByStatus(String userId, Notification.NotificationStatus status, Pageable pageable) {
        return notificationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
    }
    
    public Notification markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setStatus(Notification.NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }
    
    public void markAllAsRead(String userId) {
        notificationRepository.findByUserIdAndStatus(userId, Notification.NotificationStatus.UNREAD)
                .forEach(notification -> {
                    notification.setStatus(Notification.NotificationStatus.READ);
                    notification.setReadAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                });
    }
    
    public Long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndStatus(userId, Notification.NotificationStatus.UNREAD);
    }
    
    public void deleteNotification(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notificationRepository.delete(notification);
    }
    
    // Kafka listener for transcoding job events
    @KafkaListener(topics = "transcoding-jobs", groupId = "notification-service-group")
    public void handleTranscodingJobEvent(Object event) {
        log.info("Received transcoding job event: {}", event);
        // In a real implementation, this would parse the event and create appropriate notifications
        // For now, we'll just log it
    }
    
    // Kafka listener for file upload events
    @KafkaListener(topics = "file-uploads", groupId = "notification-service-group")
    public void handleFileUploadEvent(Object event) {
        log.info("Received file upload event: {}", event);
        // In a real implementation, this would parse the event and create appropriate notifications
        // For now, we'll just log it
    }
}
