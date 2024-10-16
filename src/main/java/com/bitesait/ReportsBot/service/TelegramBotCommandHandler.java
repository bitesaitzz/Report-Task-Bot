package com.bitesait.ReportsBot.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class TelegramBotCommandHandler {
    @Autowired
    private TelegramBotService telegramBotService;

    @Autowired
    private UserSession userSession;

    public TelegramBotCommandHandler(TelegramBotService telegramBotService, UserSession userSession) {
        this.telegramBotService = telegramBotService;
        this.userSession = userSession;
    }

    public void handleCommand(Long chatId, Message message){
        String messageText = message.hasText()? message.getText() : null;
        if (messageText != null && messageText.equals("/add")) {
            telegramBotService.approveReport(chatId);
        }
        else if (messageText != null && messageText.equals("/save")) {
            telegramBotService.saveReport(chatId);
        }
        else if(messageText != null && (messageText.equals("Отмена") || messageText.equals("/cancel"))){
            telegramBotService.menuCommandReceived(chatId, message);
        }
        else if (userSession.getUserStates().containsKey(chatId)) {
            switch (userSession.getUserStates().get(chatId)) {
                case "WAITING_FOR_NAME":
                    userSession.getUserStates().remove(chatId);
                    telegramBotService.changeUserName(chatId, message);
                    break;

                case "WAITING_FOR_DESCRIPTION":
                    telegramBotService.handleReportDescription(chatId, message);
                    break;

                case "WAITING_FOR_PHOTO":
                    telegramBotService.handlePhoto(chatId, message);
                    break;
                case "WAITING_FOR_REPORT_ID":

                    userSession.getUserStates().remove(chatId);
                    break;

                default:
                    telegramBotService.sendMessage(chatId, "Что-то на непонятном...", null);
            }
        } else {
            if (messageText != null && messageText.matches("/add_report_\\d+")) {
                int reestrId = telegramBotService.getIdFromMessage(messageText, 2);
                String type = "Обычный";
                telegramBotService.addReportGPR(chatId, Long.valueOf(reestrId), type);
            } else if (messageText != null && messageText.matches("/get_reports_\\d+")) {
                int reestrId = telegramBotService.getIdFromMessage(messageText, 2);
                telegramBotService.getReportsGPR(chatId, Long.valueOf(reestrId));
            }
            else if(messageText != null && messageText.matches("/get_report_\\d+")){
                int reportId = telegramBotService.getIdFromMessage(messageText, 2);
                telegramBotService.getReportGPR(chatId, Long.valueOf(reportId));
            }
            else if (messageText != null && messageText.matches("/add_toir_reestr_\\d+")) {
                int reestrId = telegramBotService.getIdFromMessage(messageText, 3);
                telegramBotService.addReestrToToir(chatId, Long.valueOf(reestrId));
            }  else if (messageText != null && messageText.matches("/add_report_toir_\\d+")) {
                int reestrId = telegramBotService.getIdFromMessage(messageText, 3);
                String type = "ТОиР";
                telegramBotService.addReportGPR(chatId, Long.valueOf(reestrId), type);
            }
            else if(messageText != null && messageText.matches("/show_reestrs_.*")){
                int [] parts = telegramBotService.getStartEndType(messageText);
                if(parts != null){
                    telegramBotService.showPartReestrs(chatId, parts[0], parts[1], parts[2]);
                    telegramBotService.deletePreviousMessage(chatId, message);
                }
                else {
                    telegramBotService.sendMessage(chatId, "Неверная команда.", null);
                }
            }
            else if(messageText != null && messageText.matches("/show_objects_\\d+")){
                int objectTypeId = telegramBotService.getIdFromMessage(messageText, 2);
                if(objectTypeId != -1){
                    telegramBotService.showObjectsMingaz(chatId, objectTypeId);
                }
                else {
                    telegramBotService.sendMessage(chatId, "Неверная команда.", null);
                }
            }
            else if(messageText != null && messageText.matches("/show_toir_\\d+")){
                int reestrType = telegramBotService.getIdFromMessage(messageText, 2);
                if(reestrType != -1){
                    telegramBotService.showAllToirGrp(chatId, reestrType);
                }
                else {
                    telegramBotService.sendMessage(chatId, "Неверная команда.", null);
                }
            }
            else if(messageText != null && messageText.matches("/choose_toir_\\d+")){
                int reestrType = telegramBotService.getIdFromMessage(messageText, 2);
                if(reestrType != -1){
                    telegramBotService.addTOiRReestrGRP(chatId, reestrType);
                }
                else {
                    telegramBotService.sendMessage(chatId, "Неверная команда.", null);
                }
            }

            else {
                switch (messageText) {
                    case "/start":
                        userSession.getUserStates().remove(chatId);
                        userSession.getUserReports().remove(chatId);
                        telegramBotService.registerUser(chatId, message);
                        telegramBotService.promptUserForName(chatId);
                        break;
                    case "/menu":

                        telegramBotService.menuCommandReceived(chatId, message);
                        break;
                    case "/settings":
                        telegramBotService.promptUserForName(chatId);
                        break;

                    case "/get":
                        telegramBotService.getReport(chatId);
                        break;
                    case "/reestr_grp":
                        telegramBotService.showPartReestrs(chatId, 0, 100, 1);

                        //telegramBotService.showAllReestrGrp(chatId, 1);
                        break;
                    case "/toir_grp":
                        telegramBotService.showAllToirGrp(chatId, 1);
                        break;
                    case "/reestr_mingaz":
                        telegramBotService.showMainTypeMingaz(chatId);
                        break;
                    case "/toir_mingaz":
                        telegramBotService.showAllToirMingaz(chatId);
                        break;

                    default:
                        telegramBotService.sendMessage(chatId, "Не понимаю, что Вам нужно.", null);
                }
            }
        }
    }
    }

