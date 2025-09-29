package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import com.liadkoren.nonogram.solver.LineFillIterator.DeductionResult;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class ParallelSolver implements Solver {
	private Puzzle puzzle;
	private int[][] grid;
	int rows, cols;

	private ArrayDeque<LineFillIterator> lineIterators;
	private final ExecutorService pool;

	public ParallelSolver(ExecutorService pool) {
		this.pool = pool;
	}

	private void initializeSolver(Puzzle puzzle) {
		this.puzzle = puzzle;
		this.rows = puzzle.rows().size();
		this.cols = puzzle.cols().size();
		this.grid = new int[rows][cols];

		lineIterators = getLineIterators();
	}

	private ArrayDeque<LineFillIterator> getLineIterators() {
		ArrayDeque<LineFillIterator> deque = new ArrayDeque<>(rows + cols);
		// Add row iterators
		for (int r = 0; r < rows; r++) {
			int[] blockSizes = puzzle.rows().get(r);
			LineFillIterator rowIt = new LineFillIterator(blockSizes, grid, true, r);
			deque.addLast(rowIt);
		}

		// Add column iterators
		for (int c = 0; c < cols; c++) {
			int[] blockSizes = puzzle.cols().get(c);
			LineFillIterator colIt = new LineFillIterator(blockSizes, grid, false, c);
			deque.addLast(colIt);
		}

		return deque;
	}

	public SolveResult solve(Puzzle puzzle, Duration budget) {
		long start = System.nanoTime();

		try {
			return trySolve(puzzle, budget);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return new SolveResult(Optional.empty(), Duration.ofNanos(System.nanoTime() - start));
		} catch (ExecutionException e) {
			e.printStackTrace();
			return new SolveResult(Optional.empty(), Duration.ofNanos(System.nanoTime() - start));
		}

	}

	private SolveResult trySolve(Puzzle puzzle, Duration budget) throws InterruptedException, ExecutionException {
		long start = System.nanoTime();
		long deadline = start + budget.toNanos();
		initializeSolver(puzzle);

		ArrayList<LineFillIterator> active = new ArrayList<>(lineIterators.size());
		while (System.nanoTime() < deadline && !lineIterators.isEmpty()) {
			active.clear();

			// 1. submit all jobs
			List<Callable<DeductionResult>> tasks = new ArrayList<>();
			while (!lineIterators.isEmpty()) {
				var lineIt = lineIterators.removeFirst();
				active.add(lineIt);
				tasks.add(lineIt::parallelDeduce);
			}

			// 2. wait for all to finish
			List<Future<DeductionResult>> futures = pool.invokeAll(tasks);

			//System.out.print("Active lines: " + active.size());
			//System.out.println("  Active futures: " + futures.size());

			// 3. merge results into grid
			int activeIndex = 0;
			for (Future<DeductionResult> f : futures) {
				boolean certain = f.get().certain(); // blocking get

				LineFillIterator r = active.get(activeIndex++);
				r.parallelMergeIntersectionIntoGrid();

				if (!certain) {
					// re-add to the end of the queue
					lineIterators.addLast(r);
				}

			}

		}

		Duration timeSpent = Duration.ofNanos(System.nanoTime() - start);
		if (lineIterators.isEmpty()) {
			// solved
			return new SolveResult(Optional.of(grid), timeSpent);
		}
		// budget exceeded
		return new SolveResult(Optional.empty(), timeSpent);

	}


	int[][] getGrid() {
		return grid;
	}
}