package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import com.liadkoren.nonogram.service.jobs.model.JobEntity.JobStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobStore {
	private final JobRepository repo;

	public JobEntity save(JobEntity jobEntity) { return repo.save(jobEntity); }
	public Optional<JobEntity> find(UUID id) { return repo.findById(id); }

	public JobEntity markRunning(UUID id) {
		JobEntity jobEntity = repo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
		jobEntity.setStatus(JobStatus.RUNNING);
		jobEntity.setStartedAt(Instant.now());
		return repo.save(jobEntity);
	}

	public JobEntity markSuccess(UUID id, boolean[][] resultGrid, long timeMs) {
		JobEntity jobEntity = repo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
		jobEntity.setStatus(JobStatus.SUCCESS);
		jobEntity.setCompletedAt(Instant.now());
		jobEntity.setSolutionTimeMs(timeMs);
		jobEntity.setResultGrid(resultGrid);
		return repo.save(jobEntity);
	}

	public JobEntity markFailed(UUID id, String errorMessage) {
		JobEntity jobEntity = repo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
		jobEntity.setStatus(JobStatus.FAIL);
		jobEntity.setCompletedAt(Instant.now());
		jobEntity.setErrorMessage(errorMessage);
		return repo.save(jobEntity);
	}
}
