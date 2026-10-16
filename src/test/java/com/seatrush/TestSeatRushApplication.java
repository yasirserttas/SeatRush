package com.seatrush;

import org.springframework.boot.SpringApplication;

public class TestSeatRushApplication {

	public static void main(String[] args) {
		SpringApplication.from(SeatRushApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
