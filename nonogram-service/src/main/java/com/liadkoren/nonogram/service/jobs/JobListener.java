package com.liadkoren.nonogram.service.jobs;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j(topic = "jobs.listener")
@Service
public class JobListener {

	private final ExecutorService jobListenerExecutor;
	private final JobQueue jobQueue;
	private final JobProcessingService jobProcessingService;

	private Future<?> workerFuture;

	public JobListener(@Qualifier("jobListenerExecutor") ExecutorService jobListenerExecutor, JobQueue jobQueue, JobProcessingService jobProcessingService) {
		this.jobListenerExecutor = jobListenerExecutor;
		this.jobQueue = jobQueue;
		this.jobProcessingService = jobProcessingService;
	}

	@PostConstruct
	public void start() {
		// This method is called by Spring after the bean is created.
		// We submit the loop as a task and store its Future.
		this.workerFuture = jobListenerExecutor.submit(this::runLoop);
	}

	@PreDestroy
	public void stop() {
		if (workerFuture != null) {
			log.info("Interrupting worker thread...");
			workerFuture.cancel(true);
		}
	}

	public void runLoop() {
		log.info("Worker run loop started.");
		while (!Thread.currentThread().isInterrupted()) {

			final UUID jobId;
			try {
				jobId = jobQueue.take();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}

			try {
				jobProcessingService.processJob(jobId);
			} catch (Throwable t) {
				log.error("Fatal error processing job {}; continuing loop", jobId, t);
			}
		}
		log.info("Worker run loop finished.");
	}


}
