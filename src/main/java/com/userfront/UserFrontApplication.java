package com.userfront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableScheduling
public class UserFrontApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserFrontApplication.class, args);
	}
}
