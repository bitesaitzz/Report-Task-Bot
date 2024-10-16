package com.bitesait.ReportsBot.service;


import com.bitesait.ReportsBot.config.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
public class TelegramBot extends TelegramLongPollingBot implements TelegramBotExecutor {


    @Autowired
    private TelegramBotCommandHandler telegramBotCommandHandler;


    BotConfig botConfig;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/menu", "Главное"));
        botCommandList.add(new BotCommand("/reestr_grp", "Реестр ГРП"));
        botCommandList.add(new BotCommand("/toir_grp", "ТОиР ГРП"));
        botCommandList.add(new BotCommand("/reestr_mingaz", "Реестр обьектов МинГаз"));
        botCommandList.add(new BotCommand("/toir_mingaz", "ТОиР обьектов МинГаз"));
        botCommandList.add(new BotCommand("/settings", "Сменить имя"));
        try {
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }

    @Override
    public void executeSendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && (update.getMessage().hasText() || update.getMessage().hasPhoto())) {

            Long chatId = update.getMessage().getChatId();
            telegramBotCommandHandler.handleCommand(chatId, update.getMessage());
        }
    }
}



