package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SolverRowDeductionTest {

	// --- helpers ------------------------------------------------------------
	private static Puzzle puzzle5x5Rows(List<int[]> rows) {
		// 5 empty columns (solver only needs width for row deduction)
		List<int[]> cols = List.of(new int[0], new int[0], new int[0], new int[0], new int[0]);
		return new Puzzle(rows, cols);
	}

	private static Puzzle puzzle3x3Rows(List<int[]> rows) {
		List<int[]> cols = List.of(new int[0], new int[0], new int[0]);
		return new Puzzle(rows, cols);
	}


	// --- tests --------------------------------------------------------------

	@Test
	void row_block3_in_len5_forces_center_only() throws Exception {
		// candidates for [3] in len 5: [0..2], [1..3], [2..4] → intersection is only cell 2
		Puzzle p = puzzle5x5Rows(List.of(new int[]{3}, new int[0], new int[0], new int[0], new int[0]));
		Solver s = new Solver(p);

		s.deduce(true, 0);

		int[][] g = s.getGrid();
		assertArrayEquals(new int[]{0, 0, 1, 0, 0}, g[0]); // only center pinned filled
		assertArrayEquals(new int[]{0, 0, 0, 0, 0}, g[1]); // others untouched
	}

	@Test
	void row_block5_in_len5_all_filled() throws Exception {
		Puzzle p = puzzle5x5Rows(List.of(new int[]{5}, new int[0], new int[0], new int[0], new int[0]));
		Solver s = new Solver(p);

		s.deduce(true, 0);

		int[][] g = s.getGrid();
		assertArrayEquals(new int[]{1, 1, 1, 1, 1}, g[0]);
	}

	@Test
	void row_twoSingles_in_len3_forces_gap() throws Exception {
		// Only one arrangement for [1,1] in len 3: # . #
		Puzzle p = puzzle3x3Rows(List.of(new int[]{1, 1}, new int[0], new int[0]));
		Solver s = new Solver(p);

		s.deduce(true, 0);

		int[][] g = s.getGrid();
		assertArrayEquals(new int[]{1, -1, 1}, g[0]);
	}

	@Test
	void respects_existing_certain_empty_cells() throws Exception {
		// With cell0 = -1 and clue [3] in len5, legal starts are 1 or 2 → intersection is cells 2 and 3
		Puzzle p = puzzle5x5Rows(List.of(new int[]{3}, new int[0], new int[0], new int[0], new int[0]));
		Solver s = new Solver(p);

		int[][] g = s.getGrid();
		g[0][0] = -1; // pre-mark known empty

		s.deduce(true, 0);

		assertArrayEquals(new int[]{-1, 0, 1, 1, 0}, g[0]);
	}

	@Test
	void throws_when_no_valid_fills_for_row() throws Exception {
		// Impossible: clue [4] in len 3
		Puzzle p = puzzle3x3Rows(List.of(new int[]{4}, new int[0], new int[0]));
		Solver s = new Solver(p);

		assertThrows(IllegalStateException.class, () -> {
			s.deduce(true, 0);
		});
	}
}
