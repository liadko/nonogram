package com.liadkoren.nonogram.app.jobs.worker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ExecutorService;

@Configuration
@EnableAsync
public class WorkerConfig {

	@Bean
	public ExecutorService jobListenerExecutor() {
		var ex = new ThreadPoolTaskExecutor();
		int n = Runtime.getRuntime().availableProcessors();
		ex.setCorePoolSize(n);
		ex.setMaxPoolSize(n);
		ex.setQueueCapacity(0);          // direct handoff → backpressure
		ex.setThreadNamePrefix("worker-");
		ex.initialize();
		return ex.getThreadPoolExecutor();
	}


}
