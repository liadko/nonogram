package com.liadkoren.nonogram.solver;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;

public interface SolverEngine {
	SolveResult solve(Puzzle puzzle, java.time.Duration timeout);
}
