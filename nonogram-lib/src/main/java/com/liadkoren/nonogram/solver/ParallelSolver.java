package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import com.liadkoren.nonogram.core.ports.Solver;
import com.liadkoren.nonogram.solver.LineFillIterator.DeductionResult;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * A stateful solver instance that uses multiple threads to deduce lines in parallel.
 * Not thread-safe: one instance may only be used for a single solve at a time.
 * For multiple concurrent solves, create separate instances.
 */
public final class ParallelSolver implements Solver {

	private final int[][] grid;
	int rows, cols;

	private final ConcurrentLinkedDeque<LineFillIterator> lineIterators;

	private Duration budget;
	private long solveStartTime, solveDeadline;


	public static SolveResult solve(Puzzle puzzle, Duration budget) {
		return new ParallelSolver(puzzle, budget).get();
	}

	public ParallelSolver(Puzzle puzzle, Duration budget) {

		this.rows = puzzle.rows().size();
		this.cols = puzzle.cols().size();
		this.grid = new int[rows][cols];

		this.lineIterators = new ConcurrentLinkedDeque<>();
		LineFillIterator.populateWithLineIterators(puzzle, grid, lineIterators);

		this.budget = budget;
	}


	public SolveResult get() {
		this.solveStartTime = System.nanoTime();
		this.solveDeadline = solveStartTime + budget.toNanos();

		boolean deducingRows = true;

		while (withinTimeBudget() && !lineIterators.isEmpty()) {

			List<CompletableFuture<Void>> futures = deduceAllLines(deducingRows);

			try {
				CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			} catch (CompletionException e) {
				if (e.getCause() instanceof IllegalStateException ise)
					return SolveResult.unsolvable("Puzzle is unsolvable: " + ise.getMessage(), elapsedSinceStart());

				return SolveResult.error("Execution error: " + e.getCause(), elapsedSinceStart());
			}

			deducingRows = !deducingRows; // switch rows/cols for next iteration
		}


		if (lineIterators.isEmpty()) return SolveResult.success(grid, elapsedSinceStart()); // solved
		return SolveResult.timeout(elapsedSinceStart()); // budget exceeded

	}



	private List<CompletableFuture<Void>> deduceAllLines(boolean deducingRows) {
		List<CompletableFuture<Void>> futures = new ArrayList<>(lineIterators.size());

		int n = lineIterators.size();
		for (int i = 0; i < n; i++) {
			var currentLine = lineIterators.removeFirst();

			if (deducingRows == currentLine.getIsRow())
				futures.add(CompletableFuture.supplyAsync(currentLine::parallelDeduce).thenAccept(this::enqueueIfUncertain));
			else
				lineIterators.addLast(currentLine); // re-add

		}

		return futures;
	}

	private void enqueueIfUncertain(DeductionResult result) {
		if (!result.certain()) {
			lineIterators.addLast(result.lineIterator());
		}
	}

	private boolean withinTimeBudget() {
		return System.nanoTime() < solveDeadline;
	}

	private Duration elapsedSinceStart() {
		return Duration.ofNanos(System.nanoTime() - solveStartTime);
	}
}