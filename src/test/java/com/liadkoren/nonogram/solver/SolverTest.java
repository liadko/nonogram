package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.Solution;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SolverTest {

	@Test
	void rowOfFiveAllFilled() {
		// Row clue [5], Cols all empty clues
		List<int[]> rows = List.of(new int[]{5});
		List<int[]> cols = List.of(new int[]{1}, new int[]{1}, new int[]{1}, new int[]{1}, new int[]{1});
		Puzzle puzzle = new Puzzle(rows, cols);

		Solver solver = new Solver(puzzle);
		boolean ok = solver.solve(Duration.ofSeconds(1));

		assertTrue(ok);
		int[][] grid = solver.getGrid();
		assertArrayEquals(new int[]{1, 1, 1, 1, 1}, grid[0]);
	}

	@Test
	void rowOfFiveAllEmpty() {
		// Row clue [], Col clues all 0
		List<int[]> rows = List.of(new int[0]);
		List<int[]> cols = List.of(new int[0], new int[0], new int[0], new int[0], new int[0]);
		Puzzle puzzle = new Puzzle(rows, cols);

		Solver solver = new Solver(puzzle);
		boolean ok = solver.solve(Duration.ofDays(2));

		assertTrue(ok);
		int[][] grid = solver.getGrid();
		assertArrayEquals(new int[]{-1, -1, -1, -1, -1}, grid[0]);
	}

	@Test
	void simpleCrossTwoByTwo() {
		// 2x2 puzzle, row clues [1],[1], col clues [1],[1]
		List<int[]> rows = List.of(new int[]{1}, new int[]{1});
		List<int[]> cols = List.of(new int[]{1}, new int[]{1});
		Puzzle puzzle = new Puzzle(rows, cols);

		Solver solver = new Solver(puzzle);
		boolean ok = solver.solve(Duration.ofSeconds(1));

		assertTrue(ok);
		int[][] grid = solver.getGrid();
		assertEquals(2, grid.length);
		assertEquals(2, grid[0].length);
	}

	@Test
	void threeByThreeMiddleFullRow() {
		// 3x3 with middle row [3], others []
		List<int[]> rows = List.of(new int[0], new int[]{3}, new int[0]);
		List<int[]> cols = List.of(new int[]{1}, new int[]{1}, new int[]{1});
		Puzzle puzzle = new Puzzle(rows, cols);

		Solver solver = new Solver(puzzle);
		boolean ok = solver.solve(Duration.ofSeconds(1));

		assertTrue(ok);
		int[][] grid = solver.getGrid();
		assertArrayEquals(new int[]{-1,-1,-1}, grid[0]);
		assertArrayEquals(new int[]{1,1,1}, grid[1]);
		assertArrayEquals(new int[]{-1,-1,-1}, grid[2]);
	}

	@Test
	void throwsWhenNoValidFills() {
		// Row clue [4] in 3 cells → impossible
		List<int[]> rows = List.of(new int[]{4});
		List<int[]> cols = List.of(new int[0], new int[0], new int[0]);
		Puzzle puzzle = new Puzzle(rows, cols);

		Solver solver = new Solver(puzzle);
		assertThrows(IllegalStateException.class,
				() -> solver.solve(Duration.ofMillis(100)));
	}

	@Test
	void intersectWithGenerator_allCandidatesAgree() {
		// Clue [3] in length 3 → only one fill = [1,1,1]
		RowFillGenerator gen = new RowFillGenerator(new int[]{3}, new int[]{0,0,0});
		Solver solver = new Solver(new Puzzle(List.of(new int[]{3}), List.of(new int[]{1}, new int[]{1}, new int[]{1})));

		int[] result = solver.IntersectWithGenerator(gen, 3);
		assertArrayEquals(new int[]{1,1,1}, result);
	}

	@Test
	void intersectWithGenerator_conflictingCandidatesYieldZeros() {
		// Clue [1] in length 2 → candidates: [1,-1] and [-1,1]
		RowFillGenerator gen = new RowFillGenerator(new int[]{1}, new int[]{0,0});
		Solver solver = new Solver(new Puzzle(List.of(new int[]{1}), List.of(new int[]{1}, new int[]{1})));

		int[] result = solver.IntersectWithGenerator(gen, 2);
		assertArrayEquals(new int[]{0,0}, result); // no cell guaranteed
	}

	@Test
	void deduce_rowSimpleFill() {
		// Puzzle 1x3, row clue [3]
		List<int[]> rows = List.of(new int[]{3});
		List<int[]> cols = List.of(new int[]{1}, new int[]{1}, new int[]{1});
		Solver solver = new Solver(new Puzzle(rows, cols));

		boolean changed = solver.deduce(true, 0);
		assertTrue(changed);

		int[][] grid = solver.getGrid();
		assertArrayEquals(new int[]{1,1,1}, grid[0]);
	}

	@Test
	void deduce_columnSimpleFill() {
		// Puzzle 3x1, col clue [3]
		List<int[]> rows = List.of(new int[]{1}, new int[]{1}, new int[]{1});
		List<int[]> cols = List.of(new int[]{3});
		Solver solver = new Solver(new Puzzle(rows, cols));

		boolean changed = solver.deduce(false, 0);
		assertTrue(changed);

		int[][] grid = solver.getGrid();
		assertEquals(1, grid[0][0]);
		assertEquals(1, grid[1][0]);
		assertEquals(1, grid[2][0]);
	}

	@Test
	void deduce_returnsFalseWhenNoChange() {
		// Already solved row
		List<int[]> rows = List.of(new int[]{2});
		List<int[]> cols = List.of(new int[]{1}, new int[]{1});
		Solver solver = new Solver(new Puzzle(rows, cols));

		// first deduce sets the solution
		assertTrue(solver.deduce(true, 0));
		// second deduce should find no changes
		assertFalse(solver.deduce(true, 0));
	}
}
