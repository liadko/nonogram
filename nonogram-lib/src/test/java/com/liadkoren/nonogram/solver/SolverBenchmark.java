package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks the SimpleSolver and ParallelSolver on a complex 25x25 "Dragon" puzzle.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS) // Fewer, longer warmups for a complex puzzle
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS) // 5 real measurement runs
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"}) // Fork the JVM once with a 2GB heap
public class SolverBenchmark {

	// --- State objects ---
	// These are initialized once for the entire benchmark run.
	private Puzzle dragonPuzzle;
	private SimpleSolver simpleSolver;
	private ParallelSolver parallelSolver;
	private Duration budget;

	@Setup(Level.Trial)
	public void setup() {
		// This method runs once before all benchmark iterations begin.

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
		budget = Duration.ofSeconds(10); // Generous budget for the benchmark


		System.out.println("Setup complete. Using " + Runtime.getRuntime().availableProcessors() + " threads for parallel solver.");
	}


	// --- Benchmark Methods ---
	@Benchmark
	public void parallelSolverDragonPuzzle(Blackhole bh) {
		SolveResult result = new ParallelSolver(dragonPuzzle, budget).get();
		bh.consume(result);
	}
	@Benchmark
	public void simpleSolverDragonPuzzle(Blackhole bh) {
		SolveResult result = new SimpleSolver(dragonPuzzle, budget).get();
		// Consume the result to prevent the JVM from optimizing away the call.
		bh.consume(result);
	}


	/**
	 * Main method to run the benchmarks from the IDE.
	 */
	public static void main(String[] args) throws RunnerException {
		Options opt = new OptionsBuilder()
				.include(SolverBenchmark.class.getSimpleName())
				.build();

		new Runner(opt).run();
	}
}