package com.liadkoren.nonogram.app.jobs.adapters;

import com.liadkoren.nonogram.core.model.Puzzle;
import com.liadkoren.nonogram.core.ports.Solver;
import com.liadkoren.nonogram.core.ports.SolverFactory;
import com.liadkoren.nonogram.solver.SimpleSolver;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class SimpleSolverFactory implements SolverFactory {
	@Override
	public Solver create(Puzzle puzzle, Duration budget) {
		return new SimpleSolver(puzzle, budget);
	}
}
