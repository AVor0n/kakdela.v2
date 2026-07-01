package ru.hh.kakdela_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
public class KakdelaV2Application {

	public static void main(String[] args) {
		SpringApplication.run(KakdelaV2Application.class, args);
	}

}
