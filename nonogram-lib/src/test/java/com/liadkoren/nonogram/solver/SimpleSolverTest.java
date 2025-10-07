package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSolverTest {

	@Test
	void rowOfFiveAllFilled() {
		// --- Setup ---
		List<int[]> rows = List.of(new int[]{5});
		List<int[]> cols = List.of(new int[]{1}, new int[]{1}, new int[]{1}, new int[]{1}, new int[]{1});
		Puzzle puzzle = new Puzzle(rows, cols);
		Duration budget = Duration.ofSeconds(1);

		// --- Action ---
		SolveResult result = new SimpleSolver(puzzle, budget).get();

		// --- Assertion ---
		assertEquals(SolveResult.SolveStatus.SUCCESS, result.status());
		boolean[][] grid = result.grid();
		assertArrayEquals(new boolean[]{true, true, true, true, true}, grid[0]);
	}

	@Test
	void rowOfFiveAllEmpty() {
		// --- Setup ---
		List<int[]> rows = List.of(new int[0]);
		List<int[]> cols = List.of(new int[0], new int[0], new int[0], new int[0], new int[0]);
		Puzzle puzzle = new Puzzle(rows, cols);
		Duration budget = Duration.ofSeconds(1);

		// --- Action ---
		SolveResult result = new SimpleSolver(puzzle, budget).get();

		// --- Assertion ---
		assertEquals(SolveResult.SolveStatus.SUCCESS, result.status());
		boolean[][] grid = result.grid();
		assertArrayEquals(new boolean[]{false, false, false, false, false}, grid[0]);
	}

	@Test
	void threeByThreeMiddleFullRow() {
		// --- Setup ---
		List<int[]> rows = List.of(new int[0], new int[]{3}, new int[0]);
		List<int[]> cols = List.of(new int[]{1}, new int[]{1}, new int[]{1});
		Puzzle puzzle = new Puzzle(rows, cols);
		Duration budget = Duration.ofSeconds(1);

		// --- Action ---
		SolveResult result = new SimpleSolver(puzzle, budget).get();

		// --- Assertion ---
		assertEquals(SolveResult.SolveStatus.SUCCESS, result.status());
		boolean[][] grid = result.grid();
		assertArrayEquals(new boolean[]{false, false, false}, grid[0]);
		assertArrayEquals(new boolean[]{true, true, true}, grid[1]);
		assertArrayEquals(new boolean[]{false, false, false}, grid[2]);
	}
}