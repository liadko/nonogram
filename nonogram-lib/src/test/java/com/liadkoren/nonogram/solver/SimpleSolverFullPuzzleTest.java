package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSolverFullPuzzleTest {

	private Puzzle dragonPuzzle;

	@BeforeEach
	void setUp() {
		// Row clues for the 25x25 Dragon puzzle
		List<int[]> rowClues = List.of(
				new int[]{14}, new int[]{2, 2, 6}, new int[]{3, 2, 4}, new int[]{4, 1, 3}, new int[]{3, 1, 3, 2},
				new int[]{2, 1, 2, 1, 1}, new int[]{4, 1, 2, 2, 1}, new int[]{5, 1, 1, 2, 1, 2, 1}, new int[]{6, 1, 1, 1, 2, 2}, new int[]{5, 1, 6},
				new int[]{1, 2, 1, 3, 1}, new int[]{1, 1, 3}, new int[]{2, 1, 3, 1, 3}, new int[]{1, 1, 5, 2, 6}, new int[]{2, 3, 3, 3, 1},
				new int[]{1, 2, 3, 2}, new int[]{1, 3}, new int[]{1, 1, 1, 2}, new int[]{1, 1, 1, 1, 2, 2}, new int[]{1, 1, 1, 1, 1, 2},
				new int[]{2, 2, 1, 4, 1}, new int[]{2, 2, 2, 2, 6, 1}, new int[]{2, 8, 9}, new int[]{3, 4, 8}, new int[]{4, 4, 8}
		);
		// Column clues for the 25x25 Dragon puzzle
		List<int[]> colClues = List.of(
				new int[]{4, 6}, new int[]{1, 1, 5}, new int[]{1, 2, 2}, new int[]{4, 3, 1, 1}, new int[]{3, 4, 2},
				new int[]{5, 2, 1}, new int[]{7, 1, 1}, new int[]{7, 1, 2}, new int[]{3, 3, 2, 3}, new int[]{2, 2, 3, 2},
				new int[]{10, 2, 4}, new int[]{2, 2, 8}, new int[]{1, 2, 1, 2}, new int[]{1, 2, 1, 1, 2, 1}, new int[]{1, 1, 1, 1, 1},
				new int[]{1, 2, 3, 4}, new int[]{2, 2, 3}, new int[]{3, 1, 1, 1, 2, 7}, new int[]{1, 3, 1, 2, 2, 4, 5}, new int[]{2, 4, 8, 4},
				new int[]{2, 3, 2, 2, 4}, new int[]{3, 4, 1, 3}, new int[]{4, 1, 1, 3}, new int[]{5, 2, 2, 3}, new int[]{9, 6}
		);
		dragonPuzzle = new Puzzle(rowClues, colClues);
	}

	@Test
	void knowsPuzzleIsImpossible() {
		System.out.println("Running SimpleSolver unsolvable test...");
		Duration budget = Duration.ofSeconds(15);

		List<int[]> modifiedRowClues = new ArrayList<>(dragonPuzzle.rows());
		modifiedRowClues.set(0, new int[]{25}); // Change first row to impossible clue
		Puzzle impossiblePuzzle = new Puzzle(modifiedRowClues, dragonPuzzle.cols());

		SolveResult result = new SimpleSolver(impossiblePuzzle, budget).get();

		assertEquals(SolveResult.SolveStatus.UNSOLVABLE, result.status(), "Solver should detect that the puzzle is unsolvable.");
		System.out.println("SimpleSolver unsolvable test passed in " + result.duration().toMillis() + " ms.");
	}

	@Test
	void solvesGiven5x5Puzzle() {
		// Row and col clues as int[] arrays
		List<int[]> rows = List.of(
				new int[]{1}, new int[]{2}, new int[]{3, 1}, new int[]{3}, new int[]{3}
		);
		List<int[]> cols = List.of(
				new int[]{3}, new int[]{3}, new int[]{4}, new int[]{2}, new int[]{1}
		);
		Puzzle puzzle = new Puzzle(rows, cols);
		Duration budget = Duration.ofSeconds(2);

		SolveResult result = new SimpleSolver(puzzle, budget).get();

		System.out.println(result.duration().toMillis() + " ms");
		assertEquals(SolveResult.SolveStatus.SUCCESS, result.status(), "Solver should succeed on this puzzle");

		boolean[][] grid = result.grid();
		boolean[][] expected = {
				{false, false, false, true, false},
				{false, false, true, true, false},
				{true, true, true, false, true},
				{true, true, true, false, false},
				{true, true, true, false, false}
		};

		assertArrayEquals(expected[0], grid[0]);
		assertArrayEquals(expected[1], grid[1]);
		assertArrayEquals(expected[2], grid[2]);
		assertArrayEquals(expected[3], grid[3]);
		assertArrayEquals(expected[4], grid[4]);
	}
}
