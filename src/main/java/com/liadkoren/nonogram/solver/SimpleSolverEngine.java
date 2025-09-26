package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;

import java.time.Duration;
import java.util.Optional;

public class SimpleSolverEngine implements SolverEngine {
	@Override
	public SolveResult solve(Puzzle puzzle, Duration timeout) {
//		SolverContext ctx = new SolverContext(puzzle);
//		long start = System.nanoTime();
//
//		boolean success = runStrategies(ctx, budget);
//
//		Duration took = Duration.ofNanos(System.nanoTime() - start);
		return new SolveResult(Optional.empty(), Duration.ofNanos(1));

	}
}
