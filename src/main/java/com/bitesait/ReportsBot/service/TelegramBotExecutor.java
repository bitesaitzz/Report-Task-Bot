package com.bitesait.ReportsBot.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramBotExecutor {
    void executeSendMessage(SendMessage sendMessage) throws TelegramApiException;
}
