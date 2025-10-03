package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.service.jobs.model.Job;
import com.liadkoren.nonogram.service.jobs.model.JobStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class JobQueueTest {

	@Test
	void take_blocks_until_submit() throws Exception {
		JobQueue q = new JobQueue(); // your impl
		ExecutorService es = Executors.newSingleThreadExecutor();

		Future<Job> fut = es.submit(q::take); // will block

		// ensure it’s blocking
		Thread.sleep(100);
		assertFalse(fut.isDone());

		Job j = new Job(UUID.randomUUID(), JobStatus.PENDING, "{}", 1000);
		q.submit(j);

		Job got = fut.get(500, TimeUnit.MILLISECONDS);
		assertEquals(j.getId(), got.getId());

		es.shutdownNow();
	}

	@Test
	void fifo_is_preserved() throws InterruptedException {
		JobQueue q = new JobQueue();
		Job a = new Job(UUID.randomUUID(), JobStatus.PENDING, "A", 1000);
		Job b = new Job(UUID.randomUUID(), JobStatus.PENDING, "B", 1000);

		q.submit(a);
		q.submit(b);

		assertEquals(a.getId(), q.take().getId());
		assertEquals(b.getId(), q.take().getId());
	}

//	@Test
//	void bounded_queue_rejects_when_full_if_configured() {
//		// Only if you implemented a capacity ctor and use add()/offer with bound
//		JobQueue q = new JobQueue(1); // optional API
//		q.submit(new Job(UUID.randomUUID(), JobStatus.PENDING, "{}", 1000));
//		assertThrows(IllegalStateException.class, () ->
//				q.submit(new Job(UUID.randomUUID(), JobStatus.PENDING, "{}", 1000))
//		);
//	}
}