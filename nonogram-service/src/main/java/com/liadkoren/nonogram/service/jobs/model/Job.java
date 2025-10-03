package com.liadkoren.nonogram.service.jobs.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor 
@Entity
@Table(name = "jobs")
public class Job {
	@Id
	private UUID id;

	@Enumerated(EnumType.STRING)
	private JobStatus status;

	@Lob
	private String payloadJson;  // store Puzzle or URL serialized

	@Lob
	private String resultJson;  // SolveResult, serialized

	private String errorMessage;

	private Instant createdAt;
	private Instant startedAt;
	private Instant completedAt;

	private long budgetMs;


	// convenience constructor for new jobs
	public Job(UUID id, JobStatus status, String payloadJson, long budgetMs) {
		this.id = id;
		this.status = status;
		this.payloadJson = payloadJson;
		this.createdAt = Instant.now();
		this.budgetMs = budgetMs;
	}
}
