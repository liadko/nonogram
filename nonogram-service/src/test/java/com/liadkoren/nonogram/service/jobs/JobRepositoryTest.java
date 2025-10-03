package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.service.jobs.model.Job;
import com.liadkoren.nonogram.service.jobs.model.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class JobRepositoryTest {
	@Autowired
	JobRepository repo;

	@Test
	void save_and_load_roundtrip() {
		UUID id = randomUUID();
		Job j = new Job(id, JobStatus.PENDING, "{\"url\":\"http://x\"}", 5000);
		j.setResultJson(null);
		repo.save(j);

		Job loaded = repo.findById(id).orElseThrow();
		assertEquals(JobStatus.PENDING, loaded.getStatus());
		assertEquals("{\"url\":\"http://x\"}", loaded.getPayloadJson());
		assertNull(loaded.getResultJson());
		assertNotNull(loaded.getCreatedAt());
	}

	@Test
	void update_status_and_timestamps() {
		UUID id = randomUUID();
		Job j = new Job(id, JobStatus.PENDING, "{}", 1000);
		repo.save(j);

		Job running = repo.findById(id).orElseThrow();
		running.setStatus(JobStatus.RUNNING);
		running.setStartedAt(Instant.now());
		repo.save(running);

		Job done = repo.findById(id).orElseThrow();
		assertEquals(JobStatus.RUNNING, done.getStatus());
		assertNotNull(done.getStartedAt());
	}
}