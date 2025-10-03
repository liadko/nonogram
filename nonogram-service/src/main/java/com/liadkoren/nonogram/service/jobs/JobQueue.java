package com.liadkoren.nonogram.service.jobs;

import com.liadkoren.nonogram.service.jobs.model.Job;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class JobQueue {
	private final BlockingQueue<Job> queue = new LinkedBlockingQueue<>();

	public void submit(Job job) {
		queue.add(job);    // throws if full, or use offer() with timeout if you want backpressure
	}

	public Job take() throws InterruptedException {
		return queue.take();   // blocks until something is available
	}

}