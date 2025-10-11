package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import com.liadkoren.nonogram.core.ports.Scraper;
import com.liadkoren.nonogram.core.ports.SolverFactory;
import com.liadkoren.nonogram.scraper.ScraperRouter;
import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j(topic = "jobs.processor")
public class JobExecutor {

	private final JobStore jobStore;
	private final SolverFactory solverFactory;
	private final ScraperRouter scraperRouter;

	public JobExecutor(JobStore jobStore, @Qualifier(value = "parallelSolverFactory") SolverFactory solverFactory, ScraperRouter scraperRouter) {
		this.jobStore = jobStore;
		this.solverFactory = solverFactory;
		this.scraperRouter = scraperRouter;
	}

	/**
	 * Process a single job from the store, updating its status as it goes.
	 * This method is synchronous and blocking; it should be called from a worker thread.
	 *
	 * @param jobId the ID of the job to process
	 */
	@Transactional
	public void processJob(UUID jobId) {
		JobEntity jobEntity = jobStore.find(jobId)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

		log.info("Job {} starting (source={} budgetMs={})", jobEntity.getId(),
				jobEntity.getSourceType() == JobEntity.JobSourceType.URL ? jobEntity.getSourceUrl() : "inline puzzle",
				jobEntity.getBudgetMs());
		jobStore.markRunning(jobEntity.getId());

		Puzzle puzzle;
		try {
			puzzle = getPuzzleForJob(jobEntity);
		} catch (Exception e) {
			log.error("Job {} failed during scraping. Reason: {}", jobEntity.getId(), e.getMessage());
			jobStore.markFailed(jobEntity.getId(), e.getMessage());
			return;
		}

		// Solve the puzzle
		try {
			SolveResult result = solverFactory.create(puzzle, Duration.ofMillis(jobEntity.getBudgetMs())).solve();

			switch (result.status()) {
				case SUCCESS -> jobStore.markSuccess(jobEntity.getId(), result.grid(), result.duration().toMillis());
				case TIMEOUT -> jobStore.markFailed(jobEntity.getId(), "TIMEOUT");
				case UNSOLVABLE -> jobStore.markFailed(jobEntity.getId(), "UNSOLVABLE: " + result.reason());
				case ERROR -> jobStore.markFailed(jobEntity.getId(), "ERROR: " + result.reason());
			}

		} catch (Exception e) {
			jobStore.markFailed(jobEntity.getId(), "Solver Error: " + e.getMessage());
		}

		log.info("Job {} completed", jobEntity.getId());
	}

	private Puzzle getPuzzleForJob(JobEntity jobEntity) {
		if (jobEntity.getSourceType() == JobEntity.JobSourceType.INLINE_PUZZLE) {
			return jobEntity.getPuzzle();
		}

		URI uri = URI.create(jobEntity.getSourceUrl());

		// Find and apply the scraper
		Scraper scraper = scraperRouter.route(uri)
				.orElseThrow(() -> new RuntimeException("No scraper available for URL: " + uri));


		log.info("Job {} scraping from {}", jobEntity.getId(), uri);
		return scraper.apply(uri);

	}


}
