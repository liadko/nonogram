package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.service.jobs.model.JobEntity;
import com.liadkoren.nonogram.service.jobs.model.JobEntity.JobSourceType;
import com.liadkoren.nonogram.service.jobs.model.JobEntity.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import static java.util.Arrays.deepEquals;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JobStore.class) // JPA slice + your service
class JobStoreTest {

	@Autowired
	JobStore store;
	@Autowired
	jakarta.persistence.EntityManager em;

	@Test
	void save_and_find_roundtrip_url_job() {
		// assumes puzzle column is nullable=true
		JobEntity j = JobEntity.forUrl(URI.create("http://x"), 5_000);

		JobEntity saved = store.save(j);
		em.flush();

		UUID id = saved.getId();
		assertNotNull(id);

		JobEntity loaded = store.find(id).orElseThrow();
		assertEquals(JobStatus.QUEUED, loaded.getStatus());
		assertEquals(JobSourceType.URL, loaded.getSourceType());
		assertEquals("http://x", loaded.getSourceUrl());
		assertNull(loaded.getPuzzle());
		assertNotNull(loaded.getCreatedAt());
		assertNull(loaded.getStartedAt());
		assertNull(loaded.getCompletedAt());
		assertEquals(5_000, loaded.getBudgetMs());
	}

	@Test
	void markRunning_sets_status_and_startedAt() {
		var j = JobEntity.forUrl(URI.create("http://x"), 5_000);
		store.save(j);

		JobEntity running = store.markRunning(j.getId());

		assertEquals(JobStatus.RUNNING, running.getStatus());
		assertNotNull(running.getStartedAt());
		assertTrue(running.getStartedAt().isBefore(Instant.now().plusSeconds(1)));
	}

	@Test
	void markSuccess_sets_status_result_and_completedAt() {
		var j = JobEntity.forUrl(URI.create("http://x"), 5_000);
		store.save(j);
		store.markRunning(j.getId()); // typical flow

		JobEntity done = store.markSuccess(j.getId(), new boolean[10][10], 123L);

		assertEquals(JobStatus.SUCCESS, done.getStatus());
		assertNotNull(done.getCompletedAt());
		assertNull(done.getErrorMessage());
	}

	@Test
	void markFailed_sets_status_error_fields_and_completedAt() {
		var j = JobEntity.forUrl(URI.create("http://x"), 5_000);
		store.save(j);
		store.markRunning(j.getId());

		JobEntity failed = store.markFailed(j.getId(), /*errorMessage*/ "timeout");
		assertEquals(JobStatus.FAIL, failed.getStatus());
		assertEquals("timeout", failed.getErrorMessage());
		assertNotNull(failed.getCompletedAt());
	}

	@Test
	void forUrl_requires_nonNull_url_and_nonNegative_budget() {
		assertThrows(IllegalArgumentException.class, () -> JobEntity.forUrl(null, 100));
		assertThrows(IllegalArgumentException.class, () -> JobEntity.forUrl(URI.create("http://x"), -1));
		// zero budget is allowed by your invariant
		var ok = JobEntity.forUrl(URI.create("http://x"), 0);
		assertNotNull(ok);
	}

	@Test
	void markSuccess_sets_result_grid_and_persists() {
		var j = JobEntity.forUrl(URI.create("http://x"), 5_000);
		store.save(j);
		em.flush(); // ensure INSERT

		boolean[][] grid = {
				{true, false, true},
				{false, false, true}
		};

		// your JobStore should set both summary and grid; if you split APIs, adjust call
		JobEntity done = store.markSuccess(j.getId(), grid, 123L);
		em.flush(); // force UPDATE

		assertEquals(JobStatus.SUCCESS, done.getStatus());
		assertNotNull(done.getCompletedAt());

		// reload to ensure JSON roundtrip works
		var reloaded = store.find(j.getId()).orElseThrow();
		assertTrue(deepEquals(grid, reloaded.getResultGrid()), "result grid must persist identically");
	}

	@Test
	void result_grid_defaults_to_null_until_completion() {
		var j = JobEntity.forUrl(URI.create("http://x"), 5_000);
		store.save(j);
		em.flush();

		var loaded = store.find(j.getId()).orElseThrow();
		assertNull(loaded.getResultGrid());
	}


}
