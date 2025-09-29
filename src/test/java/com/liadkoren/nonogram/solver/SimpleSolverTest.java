package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSolverTest {

	@Test
	void rowOfFiveAllFilled() {
		List<int[]> rows = List.of(new int[]{5});
		List<int[]> cols = List.of(new int[]{1}, new int[]{1}, new int[]{1}, new int[]{1}, new int[]{1});
		SimpleSolver simpleSolver = new SimpleSolver();

		simpleSolver.solve(new Puzzle(rows, cols), Duration.ofSeconds(1)); // ignore boolean; check grid
		assertArrayEquals(new int[]{1, 1, 1, 1, 1}, simpleSolver.getGrid()[0]);
	}

	@Test
	void rowOfFiveAllEmpty() {
		List<int[]> rows = List.of(new int[0]);
		List<int[]> cols = List.of(new int[0], new int[0], new int[0], new int[0], new int[0]);
		SimpleSolver simpleSolver = new SimpleSolver();

		simpleSolver.solve(new Puzzle(rows, cols), Duration.ofSeconds(1));
		assertArrayEquals(new int[]{-1, -1, -1, -1, -1}, simpleSolver.getGrid()[0]);
	}

	@Test
	void threeByThreeMiddleFullRow() {
		List<int[]> rows = List.of(new int[0], new int[]{3}, new int[0]);
		List<int[]> cols = List.of(new int[]{1}, new int[]{1}, new int[]{1});
		SimpleSolver simpleSolver = new SimpleSolver();

		simpleSolver.solve(new Puzzle(rows, cols), Duration.ofSeconds(1));
		assertArrayEquals(new int[]{-1,-1,-1}, simpleSolver.getGrid()[0]);
		assertArrayEquals(new int[]{ 1, 1, 1}, simpleSolver.getGrid()[1]);
		assertArrayEquals(new int[]{-1,-1,-1}, simpleSolver.getGrid()[2]);
	}

	@Test
	void constructingSolverFails_whenAnyLineDoesNotFit() {
		List<int[]> rows = List.of(new int[]{4});                 // 1 row, length must be 3 → impossible
		List<int[]> cols = List.of(new int[0], new int[0], new int[0]); // width=3
		Puzzle puzzle = new Puzzle(rows, cols);
		SimpleSolver simpleSolver = new SimpleSolver();
		assertThrows(IllegalStateException.class, () -> simpleSolver.solve(puzzle, Duration.ofSeconds(1)));
	}
}
