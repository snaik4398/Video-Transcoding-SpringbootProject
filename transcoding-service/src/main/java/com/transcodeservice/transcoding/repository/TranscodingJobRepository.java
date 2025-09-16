package com.transcodeservice.transcoding.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.transcodeservice.common.entity.TranscodingJob;

@Repository
public interface TranscodingJobRepository extends JpaRepository<TranscodingJob, String> {

	Page<TranscodingJob> findByUserId(String userId, Pageable pageable);

	Page<TranscodingJob> findByUserIdAndStatus(String userId, TranscodingJob.TranscodingStatus status,
			Pageable pageable);

	@Query("SELECT j FROM TranscodingJob j WHERE j.status = 'QUEUED' ORDER BY " + "CASE j.priority "
			+ "WHEN 'URGENT' THEN 1 " + "WHEN 'HIGH' THEN 2 " + "WHEN 'NORMAL' THEN 3 " + "WHEN 'LOW' THEN 4 "
			+ "END, j.createdAt ASC")
	List<TranscodingJob> findQueuedJobsOrderByPriorityAndCreatedAt();

	List<TranscodingJob> findByStatus(TranscodingJob.TranscodingStatus status);

	@Query("SELECT COUNT(j) FROM TranscodingJob j WHERE j.status = 'PROCESSING'")
	Long countActiveJobs();
}
