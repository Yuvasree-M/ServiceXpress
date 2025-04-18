package com.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.backend")

public class ServiceXpressBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceXpressBackendApplication.class, args);
	}

}
