package com.liadkoren.nonogram.service.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import com.liadkoren.nonogram.service.jobs.model.JobEntity.JobStatus;
import java.time.Instant;
import java.util.UUID;

// This annotation tells Jackson to omit any fields that are null from the JSON output.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JobStatusResponse(
		UUID id,
		JobStatus status,
		Long solutionTimeMs,
		Integer rows, Integer cols,
		boolean[][] resultGrid,
		String errorMessage
) {
	public static JobStatusResponse fromEntity(JobEntity entity) {
		boolean[][] grid = entity.getResultGrid();
		return new JobStatusResponse(
				entity.getId(),
				entity.getStatus(),
				entity.getSolutionTimeMs(),
				grid != null ? grid.length : null,
				grid != null && grid.length > 0 ? grid[0].length : null,
				grid,
				entity.getErrorMessage()
		);
	}
}