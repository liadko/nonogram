package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;

import java.time.Duration;
import java.util.Optional;

public interface Solver  {
	public SolveResult solve(Puzzle puzzle, Duration timeout);
}
