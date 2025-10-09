package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.core.model.SolveResult;
import com.liadkoren.nonogram.core.ports.SolverFactory;
import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "jobs.processor")
public class JobProcessingService {
	private final JobStore jobStore;

	private final SolverFactory solverFactory;
	/**
	 * Process a single job from the store, updating its status as it goes.
	 * This method is synchronous and blocking; it should be called from a worker thread.
	 * @param jobId the ID of the job to process
	 */
	@Transactional
	public void processJob(UUID jobId){
		JobEntity jobEntity = jobStore.find(jobId)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

		log.info("Job {} starting (source={} budgetMs={})", jobEntity.getId(),
				jobEntity.getSourceType() == JobEntity.JobSourceType.URL ? jobEntity.getSourceUrl() : "inline puzzle",
				jobEntity.getBudgetMs());
		jobStore.markRunning(jobEntity.getId());

		// URL source type not supported in this version
		if (jobEntity.getSourceType() == JobEntity.JobSourceType.URL)
		{
			log.warn("Job {} failed: URL source type not supported in this version", jobEntity.getId());
			jobStore.markFailed(jobEntity.getId(), "URL source type not supported in this version");
			return;
		}

		// Solve the puzzle
		try {
			SolveResult result = solverFactory.create(jobEntity.getPuzzle(), Duration.ofMillis(jobEntity.getBudgetMs())).solve();

			switch (result.status()) {
				case SUCCESS -> jobStore.markSuccess(jobEntity.getId(), result.grid());
				case TIMEOUT -> jobStore.markFailed(jobEntity.getId(), "TIMEOUT");
				case UNSOLVABLE -> jobStore.markFailed(jobEntity.getId(), "UNSOLVABLE: " + result.reason());
				case ERROR -> jobStore.markFailed(jobEntity.getId(), "ERROR: " + result.reason());
			}

		} catch (Exception e) {
			jobStore.markFailed(jobEntity.getId(), "Solver Error: " + e.getMessage());
		}

		log.info("Job {} completed", jobEntity.getId());
	}


}
