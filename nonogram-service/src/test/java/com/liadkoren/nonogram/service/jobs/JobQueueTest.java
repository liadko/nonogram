package com.liadkoren.nonogram.service.jobs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class JobQueueTest {

	private final ExecutorService es = Executors.newCachedThreadPool();

	@AfterEach
	void shutdown() {
		es.shutdownNow();
	}

	@Test
	void take_blocks_until_submit() throws Exception {
		JobQueue q = new JobQueue();
		UUID id = UUID.randomUUID();

		CountDownLatch started = new CountDownLatch(1);
		Future<UUID> fut = es.submit(() -> {
			started.countDown();
			return q.take(); // blocks
		});

		assertTrue(started.await(200, TimeUnit.MILLISECONDS));
		// still blocked
		Thread.sleep(100);
		assertFalse(fut.isDone(), "take() should still be blocked");

		q.submit(id);
		assertEquals(id, fut.get(500, TimeUnit.MILLISECONDS));
	}

	@Test
	void fifo_is_preserved() throws Exception {
		JobQueue q = new JobQueue();
		UUID a = UUID.randomUUID();
		UUID b = UUID.randomUUID();
		UUID c = UUID.randomUUID();

		q.submit(a);
		q.submit(b);
		q.submit(c);

		assertEquals(a, q.take());
		assertEquals(b, q.take());
		assertEquals(c, q.take());
	}

	@Test
	void take_can_be_interrupted() throws Exception {
		JobQueue q = new JobQueue();
		CountDownLatch caught = new CountDownLatch(1);
		AtomicReference<Throwable> error = new AtomicReference<>();

		Thread t = new Thread(() -> {
			try {
				q.take(); // should block
				fail("take() should not return normally when interrupted");
			} catch (InterruptedException expected) {
				caught.countDown();
			} catch (Throwable th) {
				error.set(th);
				caught.countDown();
			}
		}, "jobqueue-interrupt-test");

		t.start();
		// let it block
		Thread.sleep(100);
		t.interrupt();

		assertTrue(caught.await(500, TimeUnit.MILLISECONDS), "interrupted take() should exit quickly");
		assertNull(error.get(), "no unexpected exceptions");
		t.join(Duration.ofSeconds(1).toMillis());
	}

	@Test
	void submit_null_throws_NPE() {
		JobQueue q = new JobQueue();
		assertThrows(NullPointerException.class, () -> q.submit(null));
	}
}
