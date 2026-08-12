package com.spiceflow.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class SpiceflowBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpiceflowBackendApplication.class, args);
	}

}
