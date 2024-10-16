package com.bitesait.ReportsBot;

import com.bitesait.ReportsBot.service.TelegramBotCommandHandler;
import com.bitesait.ReportsBot.service.TelegramBotService;
import com.bitesait.ReportsBot.service.UserSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProjMinGazApplication {

	public static void main(String[] args) {

		SpringApplication.run(ProjMinGazApplication.class, args);
	}

	@Bean
	public UserSession userSession() {
		return new UserSession();
	}

	@Bean
	public TelegramBotService telegramBotService(UserSession userSession, @Value("${admin.id}") Long adminId) {
		return new TelegramBotService(userSession, adminId);
	}

	@Bean
	public TelegramBotCommandHandler telegramBotCommandHandler(TelegramBotService telegramBotService, UserSession userSession) {
		return new TelegramBotCommandHandler(telegramBotService, userSession);
	}
}
