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
		boolean[][] resultGrid,
		String errorMessage
) {
	public static JobStatusResponse fromEntity(JobEntity entity) {
		return new JobStatusResponse(
				entity.getId(),
				entity.getStatus(),
				entity.getSolutionTimeMs(),
				entity.getResultGrid(),
				entity.getErrorMessage()
		);
	}
}