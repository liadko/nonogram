package com.liadkoren.nonogram.service.jobs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobListenerTest {

	@Mock
	private JobQueue jobQueue;

	@Mock
	private JobProcessingService jobProcessingService;

	@InjectMocks
	private JobListener jobListener;

	@Test
	public void shouldTakeJobFromQueueAndProcessIt() throws Exception {
		UUID jobId = UUID.randomUUID();
		final AtomicBoolean processJobWasCalled = new AtomicBoolean(false);

		when(jobQueue.take()).thenReturn(jobId);

		doAnswer(invocation -> {
			processJobWasCalled.set(true);
			return null;
		}).when(jobProcessingService).processJob(jobId);


		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> jobListener.runLoop());


		await()
				.atMost(2, TimeUnit.SECONDS)
				.untilTrue(processJobWasCalled);



		future.cancel(true);

		verify(jobProcessingService, atLeastOnce()).processJob(jobId);
	}

	@Test
	public void shouldContinueLoopOnProcessingError() throws Exception {
		UUID job1 = UUID.randomUUID();
		UUID job2 = UUID.randomUUID();

		AtomicBoolean job2Processed = new AtomicBoolean(false);

		when(jobQueue.take()).thenReturn(job1).thenReturn(job2).thenAnswer(invocation -> {
			// Block indefinitely to prevent further processing
			new CountDownLatch(1).await();
			return null;
		});
		doThrow(new RuntimeException("Simulated processing error")).when(jobProcessingService).processJob(job1);
		doAnswer(invocation -> {
			job2Processed.set(true);
			return null;
		}).when(jobProcessingService).processJob(job2);


		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> jobListener.runLoop());

		await()
				.atMost(2, TimeUnit.SECONDS)
				.untilTrue(job2Processed);

		future.cancel(true);

		verify(jobProcessingService).processJob(job1);


	}

	@Test
	public void shouldTerminateLoopGracefullyOnInterruption() throws Exception {
		when(jobQueue.take()).thenThrow(new InterruptedException("Test shutdown"));

		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> jobListener.runLoop());

		try {
			future.get(2, TimeUnit.SECONDS); // Wait for the future to complete.
		} catch (TimeoutException e) {
			fail("The runLoop did not terminate within the timeout when InterruptedException was thrown.");
		}

		verify(jobProcessingService, never()).processJob(any(UUID.class));
	}
}
