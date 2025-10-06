package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import com.liadkoren.nonogram.core.ports.Solver;
import com.liadkoren.nonogram.solver.LineFillIterator.DeductionResult;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * A stateful solver instance that uses multiple threads to deduce lines in parallel.
 * Not thread-safe: one instance may only be used for a single solve at a time.
 * For multiple concurrent solves, create separate instances.
 */
public final class ParallelSolver implements Solver {

	private final ExecutorService pool;

	private int[][] grid;
	int rows, cols;

	private Deque<LineFillIterator> lineIterators;
	private ArrayList<LineFillIterator> activeLines;
	private List<Callable<DeductionResult>> deductionTasks;

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

		this.lineIterators = LineFillIterator.getLineIterators(puzzle, grid);
		this.activeLines = new ArrayList<>(lineIterators.size());
		this.deductionTasks = new ArrayList<>(lineIterators.size());


	}


	public SolveResult solve(Puzzle puzzle, Duration budget) {
		initializeSolver(puzzle, budget);

		try {
			return trySolve();
		}catch (IllegalStateException e) {
			return SolveResult.unsolvable("Puzzle is unsolvable: " + e.getMessage(), elapsedSinceStart());
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return SolveResult.error("Some Thread Interrupted", elapsedSinceStart());
		} catch (ExecutionException e) {
			return SolveResult.error("Execution error: " + e.getCause(), elapsedSinceStart());
		}

	}

	private SolveResult trySolve() throws InterruptedException, ExecutionException {

		boolean deducingRows = true;

		while (System.nanoTime() < solveDeadline && !lineIterators.isEmpty()) {

			submitAllTasks(deducingRows);
			List<Future<DeductionResult>> futures = pool.invokeAll(deductionTasks); //  wait for all to finish
			processAllTaskResults(futures);

			deducingRows = !deducingRows; // switch rows/cols for next iteration
		}

		if (lineIterators.isEmpty()) {
			return SolveResult.success(grid, elapsedSinceStart()); // solved
		}
		return SolveResult.timeout(elapsedSinceStart());        // budget exceeded

	}

	private void submitAllTasks(boolean deducingRows) {
		activeLines.clear();
		deductionTasks.clear();
		int lineIteratorsCount = lineIterators.size();
		for (int i = 0; i < lineIteratorsCount; i++) {
			var lineIt = lineIterators.removeFirst();

			if (deducingRows == lineIt.getIsRow()) {
				// submit for parallel deduction
				activeLines.add(lineIt);
				deductionTasks.add(lineIt::parallelDeduce);
			} else {
				// re-add to the end of the queue
				lineIterators.addLast(lineIt);
			}

		}
	}

	/**
	 * Process results of all deduction tasks.
	 * For each line that was not fully deduced, re-add its iterator to the end of the queue.
	 */
	private void processAllTaskResults(List<Future<DeductionResult>> futures) throws InterruptedException, ExecutionException {
		int activeIndex = 0;
		for (Future<DeductionResult> f : futures) {
			boolean certain = f.get().certain(); // blocking get

			LineFillIterator r = activeLines.get(activeIndex++);

			if (!certain) {
				// re-add to the end of the queue
				lineIterators.addLast(r);
			}

		}
	}



	private Duration elapsedSinceStart() {
		return Duration.ofNanos(System.nanoTime() - solveStartTime);
	}
}