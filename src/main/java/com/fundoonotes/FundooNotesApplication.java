
package com.fundoonotes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // UC10: Enable scheduled background jobs.
public class FundooNotesApplication {

	public static void main(String[] args) {

		SpringApplication.run(
				FundooNotesApplication.class,
				args
		);
	}
}
