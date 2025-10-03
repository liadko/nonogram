package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.service.jobs.model.Job;
import com.liadkoren.nonogram.service.jobs.model.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JobStore.class) // bring the service into the JPA slice
class JobStoreTest {

	@Autowired JobStore store;

	@Test
	void save_and_find_roundtrip() {
		UUID id = randomUUID();
		Job j = new Job(id, JobStatus.PENDING, "{\"url\":\"http://x\"}", 5000);
		store.save(j);

		Job loaded = store.find(id).orElseThrow();
		assertEquals(JobStatus.PENDING, loaded.getStatus());
		assertEquals("{\"url\":\"http://x\"}", loaded.getPayloadJson());
		assertNotNull(loaded.getCreatedAt());
		assertNull(loaded.getStartedAt());
		assertNull(loaded.getCompletedAt());
	}

	@Test
	void markRunning_sets_status_and_startedAt() {
		UUID id = randomUUID();
		store.save(new Job(id, JobStatus.PENDING, "{}", 5000));

		Job running = store.markRunning(id);

		assertEquals(JobStatus.RUNNING, running.getStatus());
		assertNotNull(running.getStartedAt());
		assertTrue(running.getStartedAt().isBefore(Instant.now().plusSeconds(1)));
	}

	@Test
	void markCompleted_and_markFailed_set_expected_fields() {
		UUID cId = randomUUID();
		store.save(new Job(cId, JobStatus.RUNNING, "{}", 5000));
		Job done = store.markCompleted(cId, "{\"grid\":[]}");
		assertEquals(JobStatus.SUCCESS, done.getStatus());
		assertEquals("{\"grid\":[]}", done.getResultJson());
		assertNotNull(done.getCompletedAt());

		UUID fId = randomUUID();
		store.save(new Job(fId, JobStatus.RUNNING, "{}", 5000));
		Job failed = store.markFailed(fId, "timeout");
		assertEquals(JobStatus.FAIL, failed.getStatus());
		assertEquals("timeout", failed.getErrorMessage());
		assertNotNull(failed.getCompletedAt());
	}
}
