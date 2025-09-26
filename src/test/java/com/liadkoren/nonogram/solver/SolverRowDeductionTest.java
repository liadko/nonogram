package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SolverRowDeductionTest {

	private static Puzzle puzzle5x5Rows(List<int[]> rows) {
		List<int[]> cols = List.of(new int[0], new int[0], new int[0], new int[0], new int[0]);
		return new Puzzle(rows, cols);
	}

	private static Puzzle puzzle3x3Rows(List<int[]> rows) {
		List<int[]> cols = List.of(new int[0], new int[0], new int[0]);
		return new Puzzle(rows, cols);
	}

	@Test
	void row_block3_in_len5_forces_center_only() {
		Puzzle p = puzzle5x5Rows(List.of(new int[]{3}, new int[0], new int[0], new int[0], new int[0]));
		Solver s = new Solver(p);

		LineFillIterator it = new LineFillIterator(new int[]{3}, s.getGrid(), true, 0);
		boolean certain = it.deduce();
		assertFalse(certain); // only center becomes certain

		int[][] g = s.getGrid();
		assertArrayEquals(new int[]{0, 0, 1, 0, 0}, g[0]);
		assertArrayEquals(new int[]{0, 0, 0, 0, 0}, g[1]);
	}

	@Test
	void row_block5_in_len5_all_filled() {
		Puzzle p = puzzle5x5Rows(List.of(new int[]{5}, new int[0], new int[0], new int[0], new int[0]));
		Solver s = new Solver(p);

		LineFillIterator it = new LineFillIterator(new int[]{5}, s.getGrid(), true, 0);
		boolean certain = it.deduce();
		assertTrue(certain);

		assertArrayEquals(new int[]{1, 1, 1, 1, 1}, s.getGrid()[0]);
	}

	@Test
	void row_twoSingles_in_len3_forces_gap() {
		Puzzle p = puzzle3x3Rows(List.of(new int[]{1, 1}, new int[0], new int[0]));
		Solver s = new Solver(p);

		LineFillIterator it = new LineFillIterator(new int[]{1, 1}, s.getGrid(), true, 0);
		boolean certain = it.deduce();
		assertTrue(certain);
		assertArrayEquals(new int[]{1, -1, 1}, s.getGrid()[0]);
	}

	@Test
	void respects_existing_certain_empty_cells() {
		Puzzle p = puzzle5x5Rows(List.of(new int[]{3}, new int[0], new int[0], new int[0], new int[0]));
		Solver s = new Solver(p);

		s.getGrid()[0][0] = -1; // pre-mark known empty

		LineFillIterator it = new LineFillIterator(new int[]{3}, s.getGrid(), true, 0);
		boolean certain = it.deduce();
		assertFalse(certain);

		assertArrayEquals(new int[]{-1, 0, 1, 1, 0}, s.getGrid()[0]);
	}

	@Test
	void constructingSolverThrows_whenBlocksDoNotFit() {
		Puzzle p = puzzle3x3Rows(List.of(new int[]{4}, new int[0], new int[0]));

		assertThrows(IllegalStateException.class, () -> new Solver(p));
	}
}