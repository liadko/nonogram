package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.core.model.SolveResult;
import com.liadkoren.nonogram.core.ports.Scraper;
import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
// Your types:
import com.liadkoren.nonogram.core.model.Puzzle;
//import com.liadkoren.nonogram.scraper.Scraper;
import com.liadkoren.nonogram.solver.ParallelSolver;

@Slf4j(topic = "jobs.worker")
@Service
@RequiredArgsConstructor
public class Worker {

	private final JobQueue jobQueue;
	private final JobStore jobStore;

	@Autowired(required = false)
	private final Scraper scraper;                // from nonogram-lib

	@Async("workerExecutor")
	@EventListener(ApplicationReadyEvent.class) // kick off after context is ready
	public void boot() {
		log.info("Worker boot() starting run loop");
		runLoop();
	}

	void runLoop() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				UUID id = jobQueue.take();
				runOnce(id);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				break;
			} catch (Throwable t) {
				// LAST resort: log and continue so the consumer thread stays alive
				log.error("Fatal error in worker loop; continuing", t);
			}
		}
	}

	// Single-job pipeline: decode payload → scrape if needed → solve → persist result
	void runOnce(UUID jobId) {
		JobEntity jobEntity = jobStore.find(jobId)
				.orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

		log.info("Job {} starting (source={} budgetMs={})", jobEntity.getId(),
				jobEntity.getSourceType() == JobEntity.JobSourceType.URL ? jobEntity.getSourceUrl() : "inline puzzle",
				jobEntity.getBudgetMs());

		jobStore.markRunning(jobEntity.getId());

		if (jobEntity.getSourceType() == JobEntity.JobSourceType.URL)
			throw new UnsupportedOperationException("Source type not supported");

		// 1) Build Puzzle: inline or scrape-from-URL
		var solver = new ParallelSolver(solverPool);
		try {
			SolveResult result = solver.solve(jobEntity.getPuzzle(), Duration.ofMillis(jobEntity.getBudgetMs()));
			// TODO ADD LOGS ADD LOGS ADD LOGS
			switch (result.status()) {
				case SUCCESS -> jobStore.markCompleted(jobEntity.getId(), result.grid());
				case TIMEOUT -> jobStore.markFailed(jobEntity.getId(), "TIMEOUT");
				case UNSOLVABLE -> jobStore.markFailed(jobEntity.getId(), "UNSOLVABLE: " + result.reason());
				case ERROR -> jobStore.markFailed(jobEntity.getId(), "ERROR: " + result.reason());
			}

		} catch (Exception e) {
			jobStore.markFailed(jobEntity.getId(), "Solver Error: " + e.getMessage());
		}

		// 2) Solve (fresh ParallelSolver per job, reuse shared pool)
		// var result = solver.solve(puzzle, Duration.ofMillis(jobEntity.getBudgetMs()));

//			// 3) Persist outcome
//			if (result.solutionGrid().isPresent())
//				jobStore.markCompleted(jobEntity.getId(), Json.encode(result));   // serialize SolveResult
//			else
//				jobStore.markFailed(jobEntity.getId(), "TIMEOUT_OR_UNSOLVED");


		//jobStore.markFailed(jobEntity.getId(), e.getMessage());

	}


}
