package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import com.liadkoren.nonogram.core.ports.Solver;

import java.time.Duration;
import java.util.ArrayDeque;


/**
 * A stateful solver instance.
 * Not thread-safe: one instance may only be used for a single solve at a time.
 * For multiple concurrent solves, create separate instances.
 */
public final class SimpleSolver implements Solver {
	private int[][] grid;
	int rows, cols;

	private ArrayDeque<LineFillIterator> lineIterators;

	private void initializeSolver(Puzzle puzzle) {
		this.rows = puzzle.rows().size();
		this.cols = puzzle.cols().size();
		this.grid = new int[rows][cols];

		lineIterators = new ArrayDeque<>(rows + cols);
		LineFillIterator.populateWithLineIterators(puzzle, grid, lineIterators);
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
			return SolveResult.success(grid, timeSpent);
		}
		// budget exceeded
		return SolveResult.timeout(timeSpent);
	}


	public int[][] getGrid() {
		return grid;
	}
}