package com.liadkoren.nonogram.service.jobs.model;

@Entity
@Table(name = "jobs")
public class Job {
	@Id
	private String id;

	@Enumerated(EnumType.STRING)
	private JobStatus status;

	@Lob
	private String payloadJson;  // store Puzzle or URL serialized

	private Instant createdAt;
	private long budgetMs;
}
