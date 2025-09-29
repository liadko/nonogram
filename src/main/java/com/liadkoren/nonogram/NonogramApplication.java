package com.liadkoren.nonogram;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import com.liadkoren.nonogram.solver.SimpleSolver;
import com.liadkoren.nonogram.solver.Solver;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@SpringBootApplication
public class NonogramApplication {

	public static void main(String[] args) {
		//SpringApplication.run(NonogramApplication.class, args);

		// Column clues
		List<int[]> colClues = List.of(
				new int[]{4, 6}, new int[]{1, 1, 5}, new int[]{1, 2, 2}, new int[]{4, 3, 1, 1}, new int[]{3, 4, 2},
				new int[]{5, 2, 1}, new int[]{7, 1, 1}, new int[]{7, 1, 2}, new int[]{3, 3, 2, 3}, new int[]{2, 2, 3, 2},
				new int[]{10, 2, 4}, new int[]{2, 2, 8}, new int[]{1, 2, 1, 2}, new int[]{1, 2, 1, 1, 2, 1}, new int[]{1, 1, 1, 1, 1},
				new int[]{1, 2, 3, 4}, new int[]{2, 2, 3}, new int[]{3, 1, 1, 1, 2, 7}, new int[]{1, 3, 1, 2, 2, 4, 5}, new int[]{2, 4, 8, 4},
				new int[]{2, 3, 2, 2, 4}, new int[]{3, 4, 1, 3}, new int[]{4, 1, 1, 3}, new int[]{5, 2, 2, 3}, new int[]{9, 6}
		);

		// Row clues
		List<int[]> rowClues = List.of(
				new int[]{14}, new int[]{2, 2, 6}, new int[]{3, 2, 4}, new int[]{4, 1, 3}, new int[]{3, 1, 3, 2},
				new int[]{2, 1, 2, 1, 1}, new int[]{4, 1, 2, 2, 1}, new int[]{5, 1, 1, 2, 1, 2, 1}, new int[]{6, 1, 1, 1, 2, 2}, new int[]{5, 1, 6},
				new int[]{1, 2, 1, 3, 1}, new int[]{1, 1, 3}, new int[]{2, 1, 3, 1, 3}, new int[]{1, 1, 5, 2, 6}, new int[]{2, 3, 3, 3, 1},
				new int[]{1, 2, 3, 2}, new int[]{1, 3}, new int[]{1, 1, 1, 2}, new int[]{1, 1, 1, 1, 2, 2}, new int[]{1, 1, 1, 1, 1, 2},
				new int[]{2, 2, 1, 4, 1}, new int[]{2, 2, 2, 2, 6, 1}, new int[]{2, 8, 9}, new int[]{3, 4, 8}, new int[]{4, 4, 8}
		);

		Puzzle puzzle = new Puzzle(rowClues, colClues);

		//sum the duration of 10 runs
		Duration totalDuration = Duration.ZERO;
		// Run the solver multiple times to get an average time
		final int RUNS = 50;
		for (int run = 0; run < RUNS; run++) {
			System.out.println("Run #" + (run + 1));
			Solver solver = new SimpleSolver();
			SolveResult result = solver.solve(puzzle, Duration.ofSeconds(5));
			Duration d = result.duration();
			System.out.println("Solved? " + result.solutionGrid().isPresent() + " (took: " + d.toMillis() + " ms)");

			totalDuration = totalDuration.plus(d);
		}

		System.out.println("\nAverage time: " + (totalDuration.toMillis() / (double) RUNS) + " ms");

		// Print grid
		SimpleSolver simpleSolver = new SimpleSolver();
		SolveResult result = simpleSolver.solve(puzzle, Duration.ofSeconds(30));

		if (result.solutionGrid().isPresent())
			printGrid(result.solutionGrid().get());
		else
			System.out.println("No solutionGrid found.");


	}

	private static void printGrid(int[][] grid) {
		for (int[] row : grid) {
			for (int cell : row) {
				if (cell == 1) {
					System.out.print("██");  // filled
				} else if (cell == -1) {
					System.out.print("  ");  // empty
				} else {
					System.out.print("?");  // unknown
				}
			}
			System.out.println();
		}
	}

}


