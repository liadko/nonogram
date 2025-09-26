package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.Solution;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

public final class Solver {
	private final Puzzle puzzle;
	private final int[][] grid;
	int rows, cols;

	private final boolean[] rowCompleted, colCompleted;

	public Solver(Puzzle puzzle) {
		this.puzzle = puzzle;
		this.grid = new int[puzzle.rows().size()][puzzle.cols().size()];
		this.rows = puzzle.rows().size();
		this.cols = puzzle.cols().size();

		this.rowCompleted = new boolean[rows];
		this.colCompleted = new boolean[cols];
	}

	public boolean solve(Duration budget) {
		long deadline = System.nanoTime() + budget.toNanos();
		boolean deducingRows = true;
		boolean anyRowChanges = false, anyColChanges=false;
		while (System.nanoTime() < deadline) {
			if(deducingRows) {
				anyRowChanges = false; anyColChanges = false;
			}

			int count = deducingRows ? rows : cols;
			for (int j = 0; j < count; j++) {
//				if(deducingRows && rowCompleted[j]) continue;
//				if(!deducingRows && colCompleted[j]) continue;

				boolean currentChange = deduce(deducingRows, j);
				if(deducingRows && !currentChange) rowCompleted[j] = true;
				if(!deducingRows && !currentChange) colCompleted[j] = true;

				if(deducingRows) anyRowChanges |= currentChange;
				else anyColChanges |= currentChange;

			}
			if (!deducingRows && !anyRowChanges && !anyColChanges) return true; // converged

			deducingRows = !deducingRows;
		}
		return false; // budget exceeded
	}

	boolean deduce(boolean deduceRow, int index) {
		boolean changed = false;
		if (deduceRow) {
			RowFillGenerator gen = new RowFillGenerator(puzzle.rows().get(index), grid[index]);

			int[] newCertain = IntersectWithGenerator(gen, cols);

			// write back to grid
			for (int i = 0; i < cols; i++) {
				if (grid[index][i] != newCertain[i]) {
					changed = true;
					grid[index][i] = newCertain[i];
				}

			}

		} else {
			// deduce column
			int[] colCertain = new int[rows];
			for (int i = 0; i < rows; i++) {
				colCertain[i] = grid[i][index];
			}

			RowFillGenerator gen = new RowFillGenerator(puzzle.cols().get(index), colCertain);

			int[] newCertain = IntersectWithGenerator(gen, rows);

			// write back to grid
			for (int i = 0; i < rows; i++) {
				if (grid[i][index] != newCertain[i]) {
					changed = true;
					grid[i][index] = newCertain[i];
				}
			}
		}

		return changed;
	}

	int[] IntersectWithGenerator(RowFillGenerator gen, int rowLength) {
		if (!gen.hasNext()) {
			throw new IllegalStateException("No valid fills for row/col");
		}
		// seed from the first candidate
		int[] newCertain = gen.next().clone();

		// any mismatch results in uncertainty (0)
		while (gen.hasNext()) {
			int[] next = gen.next();
			for (int i = 0; i < rowLength; i++) {
				if (newCertain[i] == 0) continue;

				if (newCertain[i] != next[i]) {
					newCertain[i] = 0;
				}
			}

		}

		return newCertain;
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