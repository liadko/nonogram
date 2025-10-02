package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSolverRowDeductionTest {

	private static int[][] grid5x5() {
		return new int[5][5];
	}

	private static int[][] grid3x3() {
		return new int[3][3];
	}

	@Test
	void row_block3_in_len5_forces_center_only() {
		int[][] grid = grid5x5();
		LineFillIterator it = new LineFillIterator(new int[]{3}, grid, true, 0);

		boolean certain = it.deduce();
		assertFalse(certain); // only center becomes certain

		assertArrayEquals(new int[]{0, 0, 1, 0, 0}, grid[0]);
		assertArrayEquals(new int[]{0, 0, 0, 0, 0}, grid[1]);
	}

	@Test
	void row_block5_in_len5_all_filled() {
		int[][] grid = grid5x5();
		LineFillIterator it = new LineFillIterator(new int[]{5}, grid, true, 0);

		boolean certain = it.deduce();
		assertTrue(certain);

		assertArrayEquals(new int[]{1, 1, 1, 1, 1}, grid[0]);
	}

	@Test
	void row_twoSingles_in_len3_forces_gap() {
		int[][] grid = grid3x3();
		LineFillIterator it = new LineFillIterator(new int[]{1, 1}, grid, true, 0);

		boolean certain = it.deduce();
		assertTrue(certain);
		assertArrayEquals(new int[]{1, -1, 1}, grid[0]);
	}

	@Test
	void respects_existing_certain_empty_cells() {
		int[][] grid = grid5x5();
		grid[0][0] = -1; // pre-mark known empty

		LineFillIterator it = new LineFillIterator(new int[]{3}, grid, true, 0);
		boolean certain = it.deduce();
		assertFalse(certain);

		assertArrayEquals(new int[]{-1, 0, 1, 1, 0}, grid[0]);
	}

	@Test
	void constructingIteratorThrows_whenBlocksDoNotFit() {
		int[][] grid = grid3x3();
		assertThrows(IllegalStateException.class,
				() -> new LineFillIterator(new int[]{4}, grid, true, 0));
	}
}
