package com.liadkoren.nonogram.core.service;

import com.liadkoren.nonogram.core.model.Job;
import com.liadkoren.nonogram.core.model.SolveResult;
import com.liadkoren.nonogram.core.ports.out.JobStore;
import com.liadkoren.nonogram.solver.SolverEngine;

import java.time.Duration;

public class PuzzleService {
	private final SolverEngine solver;      // from solver/
	private final JobStore jobStore;        // out-port

	public PuzzleService(SolverEngine solver, JobStore jobStore) {
		this.solver = solver;
		this.jobStore = jobStore;
	}

	public void solveJob(Job job) {
		jobStore.markRunning(job.id());
		try {
			SolveResult result = solver.solve(job.puzzle(), Duration.ofSeconds(5));
			if (result.solution().isPresent()) {
				jobStore.markSolved(job.id(), result.solution().get(), result.duration());
			} else {
				jobStore.markUnsolvable(job.id(), result.duration());
			}
		} catch (Exception e) {
			jobStore.markFailed(job.id(), e.getMessage());
		}
	}
}
