package com.liadkoren.nonogram.core.ports;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.model.SolveResult;

import java.time.Duration;
import java.util.function.Supplier;

public interface SolverFactory {
	Solver create(Puzzle puzzle, Duration budget);
}
