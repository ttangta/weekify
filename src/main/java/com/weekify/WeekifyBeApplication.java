package com.weekify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class WeekifyBeApplication {
	public static void main(String[] args) {
		SpringApplication.run(WeekifyBeApplication.class, args);
	}
}
