package com.liadkoren.nonogram.app.jobs.persistence;

import com.liadkoren.nonogram.app.jobs.model.JobEntity;
import com.liadkoren.nonogram.app.jobs.model.JobEntity.JobStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "jobs.store")
public class JobStore {
	private final JobRepository repo;

	public JobEntity save(JobEntity jobEntity) {
		return repo.save(jobEntity);
	}

	public Optional<JobEntity> find(UUID id) {
		return repo.findById(id);
	}

	@Transactional
	public JobEntity markRunning(UUID id) {
		JobEntity jobEntity = repo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
		log.debug("markRunning start id={} prevStatus={}", id, jobEntity.getStatus());
		jobEntity.setStatus(JobStatus.RUNNING);
		jobEntity.setStartedAt(Instant.now());
		log.info("markRunning saving id={} status={} startedAt={}", id, jobEntity.getStatus(), jobEntity.getStartedAt());
		return jobEntity;
	}

	@Transactional
	public JobEntity markSuccess(UUID id, boolean[][] resultGrid, long timeMs) {
		if (resultGrid == null)
			throw new IllegalArgumentException("Result grid cannot be null for success");

		JobEntity jobEntity = repo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

		jobEntity.setStatus(JobStatus.SUCCESS);
		jobEntity.setCompletedAt(Instant.now());
		jobEntity.setSolutionTimeMs(timeMs);
		jobEntity.setResultGrid(resultGrid);
		log.info("markSuccess saving id={} status={} completedAt={} timeMs={} size={}x{}",
				id, jobEntity.getStatus(), jobEntity.getCompletedAt(), timeMs,
				jobEntity.getResultGrid().length,
				jobEntity.getResultGrid().length > 0 ? jobEntity.getResultGrid()[0].length : 0);

		return jobEntity;
	}

	@Transactional
	public JobEntity markFailed(UUID id, String errorMessage) {
		JobEntity jobEntity = repo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));

		log.warn("markFailed start id={} prevStatus={} errorMessage={}", id, jobEntity.getStatus(), truncateString(errorMessage, 500));
		jobEntity.setStatus(JobStatus.FAIL);
		jobEntity.setCompletedAt(Instant.now());
		jobEntity.setErrorMessage(errorMessage);
		log.info("markFailed saving id={} status={} completedAt={}", id, jobEntity.getStatus(), jobEntity.getCompletedAt());
		return jobEntity;
	}

	public static String truncateString(String originalString, int maxLength) {
		if (originalString == null) {
			return null; // Handle null input gracefully
		}
		if (originalString.length() <= maxLength) {
			return originalString; // No truncation needed
		} else {
			return originalString.substring(0, maxLength); // Truncate to maxLength
		}
	}
}
