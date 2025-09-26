package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.Solution;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutorService;

public final class Solver {
	private final Puzzle puzzle;
	private final int[][] grid;
	int rows, cols;

	private final ArrayDeque<LineFillIterator> lineIterators;

	public Solver(Puzzle puzzle) {
		this.puzzle = puzzle;
		this.grid = new int[puzzle.rows().size()][puzzle.cols().size()];
		this.rows = puzzle.rows().size();
		this.cols = puzzle.cols().size();

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

	public boolean solve(Duration budget) {
		long deadline = System.nanoTime() + budget.toNanos();


		while (System.nanoTime() < deadline && !lineIterators.isEmpty()) {
			LineFillIterator lineIt = lineIterators.removeFirst();

			boolean certain = lineIt.deduce();

			if(!certain) {
				// re-add to the end of the queue
				lineIterators.addLast(lineIt);
			}

		}

		if (lineIterators.isEmpty()) {
			return true; // solved
		}
		return false; // budget exceeded
	}


	public boolean parallelSolve(Duration budget, ExecutorService pool) {
		throw new UnsupportedOperationException();
	}


	public Solution toSolution() {
		return new Solution(grid);
	}

	int[][] getGrid() {
		return grid;
	}
}