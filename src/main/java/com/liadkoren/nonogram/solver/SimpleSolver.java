package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Optional;

/**
 * A stateful solver instance.
 * Not thread-safe: one instance may only be used for a single solve at a time.
 * For multiple concurrent solves, create separate instances.
 */
public final class SimpleSolver implements Solver {
	private Puzzle puzzle;
	private int[][] grid;
	int rows, cols;

	private ArrayDeque<LineFillIterator> lineIterators;

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
		long deadline = start + budget.toNanos();

		initializeSolver(puzzle);

		while (System.nanoTime() < deadline && !lineIterators.isEmpty()) {
			LineFillIterator lineIt = lineIterators.removeFirst();

			boolean certain = lineIt.deduce();

			if (!certain) {
				// re-add to the end of the queue
				lineIterators.addLast(lineIt);
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


	public int[][] getGrid() {
		return grid;
	}
}