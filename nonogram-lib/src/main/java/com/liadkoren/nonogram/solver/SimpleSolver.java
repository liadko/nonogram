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

	private ArrayDeque<LineFillIterator> linesDeque;

	long startTime, deadline;

	public SimpleSolver(Puzzle puzzle, Duration budget) {
		this.rows = puzzle.rows().size();
		this.cols = puzzle.cols().size();
		this.grid = new int[rows][cols];

		linesDeque = new ArrayDeque<>(rows + cols);
		LineFillIterator.populateWithLineIterators(puzzle, grid, linesDeque);

		startTime = System.nanoTime();
		deadline = startTime + budget.toNanos();
	}

	public SolveResult get() {
		try {
			return trySolve();
		} catch (IllegalStateException e) {
			return SolveResult.unsolvable("Puzzle is unsolvable: " + e.getMessage(), elapsedSinceStart());
		} catch (Exception e) {
			return SolveResult.error("Solver encountered an error: " + e.getMessage(), elapsedSinceStart());
		}
	}

	public SolveResult trySolve() throws IllegalStateException {

		while (withinTimeBudget() && !linesDeque.isEmpty()) {
			LineFillIterator lineIt = linesDeque.removeFirst();

			boolean certain = lineIt.deduce();

			if (!certain) {
				linesDeque.addLast(lineIt);
			}

		}

		if (linesDeque.isEmpty()) // solved
			return SolveResult.success(grid, elapsedSinceStart());

		return SolveResult.timeout(elapsedSinceStart()); // budget exceeded
	}

	private boolean withinTimeBudget() {
		return System.nanoTime() < deadline;
	}

	private Duration elapsedSinceStart() {
		try {
			return Duration.ofNanos(System.nanoTime() - startTime);
		} catch (Exception e) {
			return Duration.ZERO;
		}
	}

}