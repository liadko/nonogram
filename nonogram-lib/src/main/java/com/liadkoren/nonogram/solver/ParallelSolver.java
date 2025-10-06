package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import com.liadkoren.nonogram.core.ports.Solver;
import com.liadkoren.nonogram.solver.LineFillIterator.DeductionResult;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

/**
 * A stateful solver instance that uses multiple threads to deduce lines in parallel.
 * Not thread-safe: one instance may only be used for a single solve at a time.
 * For multiple concurrent solves, create separate instances.
 */
public final class ParallelSolver implements Solver {

	private final ExecutorService pool;

	private int[][] grid;
	int rows, cols;

	private ConcurrentLinkedDeque<LineFillIterator> lineIterators;

	private long solveStartTime, solveDeadline;

	public ParallelSolver(ExecutorService pool) {
		this.pool = pool;
	}

	private void initializeSolver(Puzzle puzzle, Duration budget) {
		this.solveStartTime = System.nanoTime();
		this.solveDeadline = solveStartTime + budget.toNanos();

		this.rows = puzzle.rows().size();
		this.cols = puzzle.cols().size();
		this.grid = new int[rows][cols];

		this.lineIterators = new ConcurrentLinkedDeque<>();
		LineFillIterator.populateWithLineIterators(puzzle, grid, lineIterators);

	}


	public SolveResult solve(Puzzle puzzle, Duration budget) {
		initializeSolver(puzzle, budget);

		boolean deducingRows = true;

		while (withinTimeBudget() && !lineIterators.isEmpty()) {

			List<CompletableFuture<Void>> futures = deduceAllLines(deducingRows);

			try {
				CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			} catch (CompletionException e) {
				if(e.getCause() instanceof IllegalStateException ise) {
					return SolveResult.unsolvable("Puzzle is unsolvable: " + ise.getMessage(), elapsedSinceStart());
				}
				return SolveResult.error("Execution error: " + e.getCause(), elapsedSinceStart());
			}

			deducingRows = !deducingRows; // switch rows/cols for next iteration
		}

		if (lineIterators.isEmpty()) {
			return SolveResult.success(grid, elapsedSinceStart()); // solved
		}
		return SolveResult.timeout(elapsedSinceStart()); // budget exceeded

	}

	private List<CompletableFuture<Void>> deduceAllLines(boolean deducingRows) {
		List<CompletableFuture<Void>> futures = new ArrayList<>(lineIterators.size());

		int n = lineIterators.size();
		for (int i = 0; i < n; i++) {
			var lineIt = lineIterators.removeFirst();

			if (deducingRows == lineIt.getIsRow()) {
				var future = CompletableFuture.supplyAsync(lineIt::parallelDeduce, pool).thenAccept(result -> {
					if (!result.certain()) {
						lineIterators.addLast(lineIt); // re-add
					}
				});
				futures.add(future);
			} else {
				lineIterators.addLast(lineIt); // re-add
			}

		}

		return futures;
	}

	private boolean withinTimeBudget() {
		return System.nanoTime() < solveDeadline;
	}
	private Duration elapsedSinceStart() {
		return Duration.ofNanos(System.nanoTime() - solveStartTime);
	}
}