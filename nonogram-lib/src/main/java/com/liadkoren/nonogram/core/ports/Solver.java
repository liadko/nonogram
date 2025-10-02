package com.liadkoren.nonogram.core.ports;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;

import java.time.Duration;

public interface Solver  {
	SolveResult solve(Puzzle puzzle, Duration timeout);
}
