package com.oetp.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Appconfig {

	@Bean
	public ExecutorService bookingExecutor() {
		return Executors.newFixedThreadPool(5);
	}
	
	@Bean
	public Semaphore bookingSemaphore() {
		return new Semaphore(5);
	}
}
