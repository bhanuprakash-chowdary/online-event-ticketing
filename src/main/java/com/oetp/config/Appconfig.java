package com.oetp.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableCaching
public class Appconfig implements AsyncConfigurer {

	
//	@Bean(name = "bookingExecutor")
//	private Executor bookingExecutor() {
//		return Executors.newVirtualThreadPerTaskExecutor();
//	}
//	
	
//	@Bean(name = "bookingExecutor")
//	private Executor bookingExecutor() {
//		return new ForkJoinPool(5,ForkJoinPool.defaultForkJoinWorkerThreadFactory,null,true);
//	}
	
	
	@Bean(name = "bookingExecutor")
    public Executor bookingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 5 printers
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100); // Queue for busy times
        executor.setThreadNamePrefix("Booking-"); // Name threads
        executor.initialize();
        return executor;
    }
	
	@Bean
	public Semaphore bookingSemaphore() {
		return new Semaphore(5);
	}
	
	@Override
    public Executor getAsyncExecutor() {
        return bookingExecutor();
    }

}

