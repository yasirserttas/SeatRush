package com.seatrush;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SeatRushApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeatRushApplication.class, args);
	}

}
