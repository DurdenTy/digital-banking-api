package com.adilsonTI.digital_banking_api;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class DigitalBankingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigitalBankingApiApplication.class, args);
	}

}
