package com.sanjay.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanjay.common.entity.Notification;
import com.sanjay.common.entity.User;
import com.sanjay.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public Notification createNotification(User user, String title, String message,
                                           Notification.NotificationType type,
                                           String relatedEntityId, String relatedEntityType) {
        log.info("Creating notification for user: {} with title: {}", user.getId(), title);

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

    public Page<Notification> getUserNotificationsByStatus(String userId,
                                                           Notification.NotificationStatus status,
                                                           Pageable pageable) {
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

    @KafkaListener(topics = "transcoding-status", groupId = "notification-service-group")
    public void handleTranscodingStatusEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message,
                    new TypeReference<Map<String, Object>>() {});

            String userId = (String) event.get("userId");
            String jobId = (String) event.get("jobId");
            String status = (String) event.get("status");
            String outputFilename = (String) event.getOrDefault("outputFilename", "");

            if (userId == null || jobId == null || status == null) {
                log.warn("Incomplete transcoding status event, skipping: {}", event);
                return;
            }

            User user = User.builder().id(userId).build();

            if ("COMPLETED".equals(status)) {
                createNotification(user,
                        "Transcoding Complete",
                        "Your video has been transcoded successfully. Output: " + outputFilename,
                        Notification.NotificationType.TRANSCODING_COMPLETE,
                        jobId, "TRANSCODING_JOB");
                log.info("Created TRANSCODING_COMPLETE notification for user {} job {}", userId, jobId);

            } else if ("FAILED".equals(status)) {
                createNotification(user,
                        "Transcoding Failed",
                        "Your transcoding job has failed. Please check the job details and try again.",
                        Notification.NotificationType.TRANSCODING_FAILED,
                        jobId, "TRANSCODING_JOB");
                log.info("Created TRANSCODING_FAILED notification for user {} job {}", userId, jobId);
            }

        } catch (Exception e) {
            log.error("Error handling transcoding status event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "file-uploads", groupId = "notification-service-group")
    public void handleFileUploadEvent(String message) {
        try {
            Map<String, Object> event = objectMapper.readValue(message,
                    new TypeReference<Map<String, Object>>() {});

            String userId = (String) event.get("userId");
            String fileId = (String) event.get("fileId");
            String filename = (String) event.get("filename");

            if (userId == null || fileId == null) {
                log.warn("Incomplete file upload event, skipping: {}", event);
                return;
            }

            User user = User.builder().id(userId).build();

            createNotification(user,
                    "File Uploaded",
                    "Your file '" + filename + "' has been uploaded successfully and is ready for transcoding.",
                    Notification.NotificationType.FILE_UPLOADED,
                    fileId, "VIDEO_FILE");
            log.info("Created FILE_UPLOADED notification for user {} file {}", userId, fileId);

        } catch (Exception e) {
            log.error("Error handling file upload event: {}", e.getMessage(), e);
        }
    }
}
