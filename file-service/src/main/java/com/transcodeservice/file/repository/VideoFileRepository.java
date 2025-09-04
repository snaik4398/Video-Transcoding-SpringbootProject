package com.transcodeservice.file.repository;

import com.transcodeservice.common.entity.VideoFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoFileRepository extends JpaRepository<VideoFile, String> {
    
    Page<VideoFile> findByUserId(String userId, Pageable pageable);
    
    Page<VideoFile> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);
    
    List<VideoFile> findByUserIdAndIsDeletedFalse(String userId);
    
    boolean existsByObjectKeyAndBucketName(String objectKey, String bucketName);
}
