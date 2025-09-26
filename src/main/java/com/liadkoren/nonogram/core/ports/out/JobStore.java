package com.liadkoren.nonogram.core.ports.out;

import com.liadkoren.nonogram.core.model.Solution;

import java.util.UUID;

public interface JobStore {

	void markRunning(UUID jobId);

	void markSolved(UUID jobId, Solution solution, java.time.Duration duration);

	void markUnsolvable(UUID jobId, java.time.Duration duration);

	void markFailed(UUID jobId, String reason);
}
