package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleSolverFullPuzzleTest {

	@Test
	void solvesGiven5x5Puzzle() {
		// Row and col clues as int[] arrays
		List<int[]> rows = List.of(
				new int[]{1},
				new int[]{2},
				new int[]{3, 1},
				new int[]{3},
				new int[]{3}
		);
		List<int[]> cols = List.of(
				new int[]{3},
				new int[]{3},
				new int[]{4},
				new int[]{2},
				new int[]{1}
		);
		Puzzle puzzle = new Puzzle(rows, cols);

		SimpleSolver simpleSolver = new SimpleSolver();
		SolveResult result = simpleSolver.solve(puzzle, Duration.ofSeconds(2));

		System.out.println(result.duration().toMillis() + " ms");
		assertTrue(result.solutionGrid().isPresent(), "Solver should succeed on this puzzle");

		int[][] grid = result.solutionGrid().get();

		int[][] expected = {
				{-1, -1, -1,  1, -1},
				{-1, -1,  1,  1, -1},
				{ 1,  1,  1, -1,  1},
				{ 1,  1,  1, -1, -1},
				{ 1,  1,  1, -1, -1}
		};

		assertArrayEquals(expected[0], grid[0]);
		assertArrayEquals(expected[1], grid[1]);
		assertArrayEquals(expected[2], grid[2]);
		assertArrayEquals(expected[3], grid[3]);
		assertArrayEquals(expected[4], grid[4]);
	}
}