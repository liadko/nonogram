package com.liadkoren.nonogram.service.jobs;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class JobQueue {
	private final BlockingQueue<UUID> queue = new LinkedBlockingQueue<>();

	public void submit(UUID jobId) {
		queue.add(jobId);    // throws if full, or use offer() with timeout if you want backpressure
	}

	public UUID take() throws InterruptedException {
		return queue.take();   // blocks until something is available
	}

}