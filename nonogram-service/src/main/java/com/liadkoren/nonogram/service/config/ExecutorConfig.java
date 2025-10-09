package com.liadkoren.nonogram.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class ExecutorConfig {

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
