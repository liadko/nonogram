package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "jobs.app")
public class JobSubmissionService {
	private final JobStore jobStore;
	private final JobQueue jobQueue;

	//@Transactional
	public UUID submitUrlJob(URI url, long budgetMs) {
		requireBudget(budgetMs);

		JobEntity jobEntity = JobEntity.forUrl(url, budgetMs);
		jobStore.save(jobEntity);
		jobQueue.submit(jobEntity.getId());
		log.info("job.queued type=url jobId={} host={} budgetMs={}",
				jobEntity.getId(), url.getHost(), budgetMs);
		return jobEntity.getId();
	}

	//@Transactional
	public UUID submitPuzzleJob(Puzzle puzzle, long budgetMs) {
		requireBudget(budgetMs);

		JobEntity jobEntity = JobEntity.forPuzzle(puzzle, budgetMs);
		jobStore.save(jobEntity);
		jobQueue.submit(jobEntity.getId());
		log.info("job.queued type=puzzle jobId={} rows={} cols={} budgetMs={}",
				jobEntity.getId(), puzzle.rows().size(), puzzle.cols().size(), budgetMs);
		return jobEntity.getId();
	}

	private static void requireBudget(long ms) {
		if (ms < 0) throw new IllegalArgumentException("budgetMs must be >= 0");
	}
}