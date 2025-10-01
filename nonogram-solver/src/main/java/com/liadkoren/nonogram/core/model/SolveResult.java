package com.liadkoren.nonogram.core.model;

import java.time.Duration;
import java.util.Optional;

public record SolveResult(Optional<int[][]> solutionGrid, Duration duration) {}
