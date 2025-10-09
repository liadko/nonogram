package com.liadkoren.nonogram.core.model;

import java.time.Duration;
import java.util.Optional;

/**
 * The result of a solve attempt.
 * Immutable.
 */
public record SolveResult(SolveStatus status,
						  boolean[][] grid,
						  Duration duration,
						  String reason) {

	public SolveResult {
		if (status == null || duration == null) throw new IllegalArgumentException("status/duration required");
		if (status == SolveStatus.SUCCESS && grid == null) throw new IllegalArgumentException("SUCCESS needs grid");
		if (status != SolveStatus.SUCCESS && grid != null) throw new IllegalArgumentException("grid only on SUCCESS");
		if (status == SolveStatus.SUCCESS && reason != null) throw new IllegalArgumentException("no reason on SUCCESS");
		if (status != SolveStatus.SUCCESS && (reason == null || reason.isBlank())) throw new IllegalArgumentException("reason required on failure");
	}

	public static SolveResult success(int[][] grid, Duration duration) {
		boolean[][] boolGrid = new boolean[grid.length][];
		for (int i = 0; i < grid.length; i++) {
			boolGrid[i] = new boolean[grid[i].length];
			for (int j = 0; j < grid[i].length; j++) {
				boolGrid[i][j] = (grid[i][j] == 1);
			}
		}

		return new SolveResult(SolveStatus.SUCCESS, boolGrid, duration, null);
	}

	public static SolveResult timeout(Duration d) {
		return new SolveResult(SolveStatus.TIMEOUT, null, d, "TIMEOUT");
	}

	public static SolveResult unsolvable(String why, Duration d) {
		return new SolveResult(SolveStatus.UNSOLVABLE, null, d, why);
	}

	public static SolveResult error(String why, Duration d) {
		return new SolveResult(SolveStatus.ERROR, null, d, why);
	}

	public enum SolveStatus {SUCCESS, TIMEOUT, ERROR, UNSOLVABLE}
}
