package com.transcodeservice.notification.repository;

import com.transcodeservice.common.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, Notification.NotificationStatus status, Pageable pageable);
    
    List<Notification> findByUserIdAndStatus(String userId, Notification.NotificationStatus status);
    
    Long countByUserIdAndStatus(String userId, Notification.NotificationStatus status);
}
