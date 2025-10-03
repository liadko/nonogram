package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.service.jobs.model.Job;
import com.liadkoren.nonogram.service.jobs.model.JobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobStore {
	private final JobRepository repo;

	public Job save(Job job) { return repo.save(job); }
	public Optional<Job> find(UUID id) { return repo.findById(id); }

	public Job markRunning(UUID id) {
		Job job = repo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
		job.setStatus(JobStatus.RUNNING);
		job.setStartedAt(Instant.now());
		return repo.save(job);
	}

	public Job markCompleted(UUID id, String resultPayload) {
		Job job = repo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
		job.setStatus(JobStatus.SUCCESS);
		job.setCompletedAt(Instant.now());
		job.setResultJson(resultPayload);
		return repo.save(job);
	}

	public Job markFailed(UUID id, String errorMessage) {
		Job job = repo.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + id));
		job.setStatus(JobStatus.FAIL);
		job.setCompletedAt(Instant.now());
		job.setErrorMessage(errorMessage);
		return repo.save(job);
	}
}
