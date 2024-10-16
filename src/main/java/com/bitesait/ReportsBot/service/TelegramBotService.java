package com.bitesait.ReportsBot.service;


import com.bitesait.ReportsBot.model.*;
import com.vdurmont.emoji.EmojiParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TelegramBotService {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    @Autowired
    private UserSession userSession;

    ReplyKeyboardMarkup replyKeyboardMarkup;

    public TelegramBotService(UserSession userSession,  @Value("${admin.id}") Long adminId) {
        this.userSession = userSession;
        this.ADMINID = adminId;
        replyKeyboardMarkup = new ReplyKeyboardMarkup();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add("Отмена");
        replyKeyboardMarkup.setKeyboard(List.of(keyboardRow));
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ReestrRepository reestrRepository;

    @Autowired
    private ToirReestrRepository toirReestrRepository;

    @Autowired
    private ToirReestrService toirReestrService;


    @Autowired
    private ReestrTypeRepository reestrTypeRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private TelegramBot telegramBot;

    private Long ADMINID;

    public void sendMessage(Long chatId, String messageText, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(messageText);
        if(replyKeyboardMarkup != null)
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        else{
            ReplyKeyboardMarkup replyKeyboardMarkup1 = new ReplyKeyboardMarkup();
            KeyboardRow keyboardRow = new KeyboardRow();
            replyKeyboardMarkup1.setKeyboard(List.of(keyboardRow));
            sendMessage.setReplyMarkup(replyKeyboardMarkup1);
        }
        try {
            telegramBot.execute(sendMessage);
            logger.info("Message sent: {} to user {}", messageText, chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message: {} to user: {}", messageText, chatId, e);
        }

    }



    public void registerUser(Long chatId, Message message) {
        if(userRepository.findById(chatId).isEmpty()) {
            User user = new User();
            user.setChatId(chatId);
            user.setName(message.getChat().getFirstName());
            userRepository.save(user);
            sendMessage(chatId, "Вы успешно зарегистрированы!", null);
            logger.info("User registered: {}", chatId);
        } else {
            //sendMessage(chatId, "You are already registered!");
            logger.info("User already registered: {}", chatId);
        }
    }

    public void changeUserName(Long chatId, Message message) {
        User user = userRepository.findById(chatId).get();
        user.setName(message.getText());
        userRepository.save(user);
        sendMessage(chatId, "Ваше ФИО изменено на " + message.getText(), null);
        menuCommandReceived(chatId, message);
        logger.info("User name changed: {}", chatId);
    }

    public void promptUserForName(Long chatId) {
        sendMessage(chatId, "Введите ФИО", null);
        userSession.getUserStates().put(chatId, "WAITING_FOR_NAME");
        //userStates.put(chatId, "WAITING_FOR_NAME");
    }

    public void addReportGPR(Long chatId, Long reestrId, String type){
        Reestr reestr = reestrRepository.findById(reestrId).get();
        ToirReestr toirReestr = toirReestrRepository.findByReestr(reestr).orElse(null);
        if(reestr == null ){
            sendMessage(chatId, "Реестр не найден", null);
            return;
        }
        if(type == "Тип 2" && toirReestr == null){
            sendMessage(chatId, "Реестр не найден в ГРП для ТОиР", null);
            return;
        }
        Report report = new Report();
        report.setReestr(reestr);
        report.setType(type);
        userSession.getUserReports().put(chatId, report);
        sendMessage(chatId, "Введите описание", replyKeyboardMarkup);
        userSession.getUserStates().put(chatId, "WAITING_FOR_DESCRIPTION");

    }


    public void handleReportDescription(Long chatId, Message message){
        Report report = userSession.getUserReports().get(chatId);
        String description = message.getText();
        if(description.charAt(0) == '/'){
            sendMessage(chatId, "Описание не может начинаться с /", replyKeyboardMarkup);
            return;
        }
        else if(description.length() <= 5){
            sendMessage(chatId, "Описание должно быть длиннее 5 символов", replyKeyboardMarkup);
            return;
        }
        report.setDescription(message.getText());
        userSession.getUserReports().put(chatId, report);
        sendMessage(chatId, "Отправьте фото(после отправки нажмте /add)", replyKeyboardMarkup);
        userSession.getUserStates().put(chatId, "WAITING_FOR_PHOTO");
    }



    public void handlePhoto(Long chatId, Message message){
        if(message.hasPhoto()) {
            Report report = userSession.getUserReports().get(chatId);

            List<PhotoSize> photos = message.getPhoto();
            PhotoSize largestPhoto = photos.get(photos.size() - 1); // Последнее фото с максимальным разрешением
            String fileId = largestPhoto.getFileId();
            //byte[] photoBytes = downloadPhoto(fileId);
            if(!fileId.isEmpty()){
                Photo photo = new Photo();
                photo.setFileId(fileId);
                report.addPhoto(photo);
                userSession.getUserReports().put(chatId, report);
            }

            else{
                sendMessage(chatId, "Не удалось сохранить фото. Попробуйте еще раз", replyKeyboardMarkup);
            }
        }
        else{
            sendMessage(chatId, "Отправьте фото", replyKeyboardMarkup);
        }

    }

    public void approveReport(Long chatId) {
        Report report = userSession.getUserReports().get(chatId);
        User user = userRepository.findById(chatId).get();
        if (report == null) {
            sendMessage(chatId, "Отчет не найден. Попробуйте еще раз", replyKeyboardMarkup);
            return;
        } else if (user == null) {
            sendMessage(chatId, "Пользователь не найден. Попробуйте еще раз", replyKeyboardMarkup);
            return;
        } else if (report.getPhotos().isEmpty()) {
            sendMessage(chatId, "Отчет не содержит фото. Добавьте и нажмите /add", replyKeyboardMarkup);
            return;
        }
        else if(report.getPhotos().size() > 10){
            report.getPhotos().clear();
            userSession.getUserReports().put(chatId, report);
            sendMessage(chatId, "Максимальное количество фото - 10. Еще раз добавьте фото и нажмите /add", replyKeyboardMarkup);
            return;
        }

        report.setUser(user);
        LocalDate currentDate = java.time.LocalDate.now();
        report.setDate(currentDate);
        userSession.getUserReports().put(chatId, report);
        showReport(report, chatId);
        sendMessage(chatId, "Отчет готов к сохранению. Проверьте все ли верно.\nНажмите /save - сохранить отчет\n /cancel - отменить отчет.", replyKeyboardMarkup);

    }
    public void saveReport(Long chatId){
        Report report = userSession.getUserReports().get(chatId);
        if(report.getType().equals("ТОиР")){
            toirReestrService.deleteReestrFromToir(report.getReestr());
        }
        Long id = reportRepository.save(report).getId();
        userSession.getUserStates().remove(chatId);
        userSession.getUserReports().remove(chatId);

        sendMessage(chatId, "Отчет номер " + id +" сохранен", replyKeyboardMarkup);
        sendAdminReport(id);
    }

    public void sendAdminReport(Long reportId){
        Optional<Report> report = reportRepository.findById(reportId);
        if(report.isPresent()){
            String info = "";
             info += report.get().getUser().getName() + " добавил отчет к ";

             if(report.get().getReestr().getType() < 10){
                 ;
             }
             else if(report.get().getReestr().getType() < 100){
                 info += reestrTypeRepository.findNameById(10) +" ->";
             }
             else if(report.get().getReestr().getType() < 1000){
                 info += reestrTypeRepository.findNameById(100) +" ->";
             }
             else{
                 info += reestrTypeRepository.findNameById(1000) +" ->";
             }
             String type = reestrTypeRepository.findNameById(report.get().getReestr().getType());
             info += " " + type + " -> " + report.get().getReestr().getName()+ "\n Показать отчет: /get_report_" + reportId;
             sendMessage(ADMINID, info, null);
        }
    }

   public void getReportGPR(Long chatId, Long reportId){
        Report report = reportRepository.findById(reportId).get();
        if(report == null){
            sendMessage(chatId, "Отчет не найден", null);
            return;
        }
        showReport(report, chatId);
    }

    public byte [] downloadPhoto(String fileId){
        try {
            // Get the file path
            String filePath = telegramBot.execute(new GetFile(fileId)).getFilePath();
            // Construct the file URL
            String fileUrl = "https://api.telegram.org/file/bot" + telegramBot.getBotToken() + "/" + filePath;
            // Download the file
            InputStream inputStream = new URL(fileUrl).openStream();
            return inputStream.readAllBytes();
        } catch (IOException | TelegramApiException e) {
            logger.error("Failed to download photo: {}", fileId, e);
            return null;
        }
    }




    public void showReport(Report report, Long chatId){
        //Report report = reportRepository.findById(id).get();
        String type = reestrTypeRepository.findNameById(report.getReestr().getType());
        if(report.getReestr().getType() < 10){
            ;
        }
        else if(report.getReestr().getType() < 100){
            type = reestrTypeRepository.findNameById(10) +" -> "+ type;
        }
        else if(report.getReestr().getType() < 1000){
            type = reestrTypeRepository.findNameById(100) +" -> "+ type;
        }
        else{
            type = reestrTypeRepository.findNameById(1000) +" -> "+ type;
        }
        String text =  "Реестр: " + report.getReestr().getName() + " (" + type+ ")\n" +
                "Тип отчета: " + report.getType() + "\n" +
                "Описание: " + report.getDescription() + "\n" +
                "Дата: " + report.getDate().toString() + "\n" +
                "Выполнен: " + report.getUser().getName();
        if(report.getId() != 0){
            text = "ОТЧЕТ № " + report.getId() + "\n\n" + text;
        }
        sendMessage(chatId, text, null);



        //List<Photo> photos = photoRepository.findByReport(report.getId());
        List<Photo> photos = report.getPhotos();
        if(photos.isEmpty()){
            sendMessage(chatId, "Фото отсутствуют", null);
            return;
        }
        if(photos.size() == 1){
            sendPhotoByFileId(chatId, photos.get(0).getFileId());
        }

        else {
            List<InputMedia> mediaList = new ArrayList<>();
            for (Photo photo : photos) {
                //sendPhoto(chatId, photo.getPhotoData());
                InputMediaPhoto mediaPhoto = new InputMediaPhoto();
                mediaPhoto.setMedia(photo.getFileId()); // Устанавливаем fileId как media
                mediaList.add(mediaPhoto);
            }
            sendMultiplePhotos(chatId, mediaList);
        }
//

    }

    public void sendPhoto(Long chatId, byte[] photoBytes) {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(chatId.toString());
        InputFile inputFile = new InputFile(new ByteArrayInputStream(photoBytes), "photo.jpg");
        sendPhotoRequest.setPhoto(inputFile);
        try {
            telegramBot.execute(sendPhotoRequest);
            logger.info("Photo sent to user {}", chatId);
        } catch (TelegramApiException e) {
            logger.error("Failed to send photo to user: {}", chatId, e);
        }

    }
    public void sendPhotoByFileId(Long chatId, String fileId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId.toString());  // Set the chat ID
        sendPhoto.setPhoto(new InputFile(fileId));  // Set the fileId as the photo

        try {
            telegramBot.execute(sendPhoto);  // Send the photo
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMultiplePhotos(Long chatId, List<InputMedia> mediaList) {

        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        sendMediaGroup.setChatId(chatId.toString());
        sendMediaGroup.setMedias(mediaList);

        try {
            telegramBot.execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            logger.error("Failed to send photo to user: {}", chatId, e);
        }
    }

    public void getReport(Long chatId) {
        sendMessage(chatId, "Введите id отчета", replyKeyboardMarkup);
        userSession.getUserStates().put(chatId, "WAITING_FOR_REPORT_ID");

    }


    public void showPartReestrs(Long chatId, int start, int end, int type){
        Optional<List<Reestr>> reestrListOptional = reestrRepository.findByType(type);
        if(reestrListOptional.isEmpty()){
            sendMessage(chatId, "Реестры отсутствуют", null);
            return;
        }
        List<Reestr> reestrList = reestrListOptional.get();
        String typeName = reestrTypeRepository.findById(Long.valueOf(type)).get().getName();

        String allReestr = "Реестры " + typeName + ":\n\n";

        int currentMonth = java.time.LocalDate.now().getMonthValue();
        int currentYear = java.time.LocalDate.now().getYear();
        for(int i = start; i < end; i++){
            if(i >= reestrList.size() || i < 0){
                break;
            }
            Reestr reestr = reestrList.get(i);
            String reestrName = reestr.getId() + ". " + reestr.getName();
            if(!reestr.getAddress().isEmpty()){
                reestrName +=  " (" + reestr.getAddress() + ")";
            }
            allReestr += reestrName + "\n /add_report_" +reestr.getId() + " - добавить отчет\n /get_reports_"+ reestr.getId() + " -получить все отчеты";
            if(reportRepository.existsByReestrIdAndCurrentMonthAndType(reestr.getId(), currentMonth, currentYear, "Обычный")){
                allReestr += EmojiParser.parseToUnicode(" :white_check_mark:");
            }
            Optional<LocalDate> lastToirReport = reportRepository.findLastReportDateByType(reestr.getId(), "ТОиР");
            if(lastToirReport.isPresent()){
                allReestr += EmojiParser.parseToUnicode(" :page_facing_up:");
                allReestr += "\nПоследний ТОиР: " + lastToirReport.get().toString() + "." ;

            }
            allReestr += "\n\n";
            if(allReestr.length() > 3700){
                sendMessage(chatId, allReestr, null);
                allReestr = "";
            }
        }
        if(end + 100 <= reestrList.size()){
            allReestr += "\n Показать следующие 100 реестров: /show_reestrs_" + (end) + "_" + (end + 100) + "_" + type;
        }
        else{
            if(end <= reestrList.size())
                allReestr += "\n Показать следующие 100 реестров: /show_reestrs_" + (end) + "_" + (reestrList.size()) + "_" + type;
        }
        if(start-100 >= 0){
            allReestr += "\nПоказать предыдущие 100 реестров: /show_reestrs_" + (start - 100) + "_" + (start) + "_" + type;
        }
        else{
            if(start > 0)
                allReestr += "\nПоказать предыдущие 100 реестров: /show_reestrs_" + (0) + "_" + (start) + "_" + type;
        }

        sendMessage(chatId, allReestr, null);


    }
    public void getReportsGPR(Long chatId, Long reestId){
        Reestr reestr = reestrRepository.findById(reestId).get();


        if(reestr == null){
            sendMessage(chatId, "Реестр " + reestr.getName() + " не найден", null);
            return;
        }
        List<Long> reportIdList = reportRepository.findByReestrId(reestId);
        if(reportIdList.isEmpty()){
            sendMessage(chatId, "Отчеты по реестру " + reestr.getName() + " отсутствуют.", null);
            return;
        }
        for(Long reportId : reportIdList){
            Report report = reportRepository.findById(reportId).get();
            showReport(report, chatId);
        }

    }

    public void menuCommandReceived(Long chatId, Message message){
        userSession.getUserStates().remove(chatId);
        userSession.getUserReports().remove(chatId);
        sendMessage(chatId, "Главная страница\n\nБот предназначен для ведения отчетности по ГРП и обьектам МинГаз. Используйте меню для выбора нужной Вам опции.", null);
    }

    public void showAllToirGrp(Long chatId, int type){
        //List<ToirReestr> toirReestrs = (List<ToirReestr>) toirReestrRepository.findAll();
        List<ToirReestr> toirReestrs = toirReestrRepository.findByType(type);
        String description = reestrTypeRepository.findById(Long.valueOf(type)).get().getName();
        List<Reestr> reestrList =  toirReestrs.stream()
                .map(ToirReestr::getReestr)
                .collect(Collectors.toList());
        if(reestrList.isEmpty()){
            String text = "Реестры " + description + " для ТОиР отсутствуют.";
            sendMessage(chatId, text, null);
        }
        else{
            String allReestr = "Реестры " + description + " для ТОиР:\n\n";

            for(Reestr reestr : reestrList){
                String reestrName = reestr.getId() + ". " + reestr.getName();
                if(!reestr.getAddress().isEmpty()){
                    reestrName +=  " (" + reestr.getAddress() + ")";
                }
                allReestr += reestrName + "\n /add_report_toir_"+ reestr.getId() +" - добавит отчет для ТОиР";
                allReestr += "\n\n";
                if(allReestr.length() > 4000){
                    sendMessage(chatId, allReestr, null);
                    allReestr = "";
                }
            }
            sendMessage(chatId, allReestr, null);
        }

        sendMessage(chatId, "Добавить реестры ТОиР - /choose_toir_"+ type, null);
    }

    public void showAllToirMingaz(Long chatId){
        //List<ToirReestr> toirReestrs = (List<ToirReestr>) toirReestrRepository.findAll();
        List<ToirReestr> toirReestrs = toirReestrRepository.findMingaz();
        String description = "МинГаз";
        List<Reestr> reestrList =  toirReestrs.stream()
                .map(ToirReestr::getReestr)
                .collect(Collectors.toList());
        if(reestrList.isEmpty()){
            String text = "Реестры " + description + " для ТОиР отсутствуют.";
            sendMessage(chatId, text, null);
        }
        else{
            String allReestr = "Реестры " + description + " для ТОиР:\n\n";

            for(Reestr reestr : reestrList){
                String reestrName = reestr.getId() + ". " + reestr.getName();
                if(!reestr.getAddress().isEmpty()){
                    reestrName +=  " (" + reestr.getAddress() + ")";
                }
                allReestr += reestrName + "\n /add_report_toir_"+ reestr.getId() +" - добавит отчет для ТОиР";
                allReestr += "\n\n";
                if(allReestr.length() > 4000){
                    sendMessage(chatId, allReestr, null);
                    allReestr = "";
                }
            }
            sendMessage(chatId, allReestr, null);
        }

        //sendMessage(chatId, "Добавить реестры ТОиР - /choose_toir_"+ type, null);
    }


    public void addTOiRReestrGRP(Long chatId, int type){
        if(!chatId.equals(ADMINID)){
            sendMessage(chatId, "У вас нет прав на добавление реестров для ТОиР", null);
            logger.info("User {} tried to add reestr to TOiR. Admin Id {}", chatId, ADMINID);
        }
        else{
           Optional<List<Reestr>> reestrListOptional = reestrRepository.findByType(type);
           if(reestrListOptional.isEmpty()){
                sendMessage(chatId, "Реестры отсутствуют", null);
                return;
           }
              List<Reestr> reestrList = reestrListOptional.get();
            String allReestr = "";
            for(Reestr reestr : reestrList){
                String reestrName = reestr.getId() + ". " + reestr.getName();
                if(!reestr.getAddress().isEmpty()){
                    reestrName +=  " (" + reestr.getAddress() + ")";
                }
                allReestr += reestrName + "\n /add_toir_reestr_" +reestr.getId() + " - добавить реестр в ТОиР";
                allReestr += "\n\n";
                if(allReestr.length() > 4000){
                    sendMessage(chatId, allReestr, null);
                    allReestr = "";
                }
            }
            sendMessage(chatId, allReestr, null);
        }
    }

    public void addReestrToToir(Long chatId, Long reestrId){
        Reestr reestr = reestrRepository.findById(reestrId).get();
        if(reestr == null){
            sendMessage(chatId, "Реестр не найден", null);
            return;
        }
        ToirReestr checkToirReestr = toirReestrRepository.findByReestr(reestr).orElse(null);
        if(checkToirReestr != null){
            sendMessage(chatId, "Реестр " + reestr.getName() + " уже добавлен в ТОиР", null);
            return;
        }
        ToirReestr toirReestr = new ToirReestr();
        toirReestr.setReestr(reestr);
        toirReestr.setType(reestr.getType());
        toirReestrRepository.save(toirReestr);
        sendMessage(chatId, "Реестр " + reestr.getName() + " добавлен в ТОиР", null);
    }


    public void deletePreviousMessage(Long chatId, Message message) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId.toString());
        deleteMessage.setMessageId(message.getMessageId() - 1);
        try{
            telegramBot.execute(deleteMessage);
        }
        catch (TelegramApiException e){
            logger.error("Failed to delete message: {}", message.getMessageId() - 1, e);
        }
    }

    public int[] getStartEndType(String messageText){
        String[] parts = messageText.split("_");
        if (parts.length == 5) {
            try {
                int start = Integer.parseInt(parts[2]);
                int end = Integer.parseInt(parts[3]);
                int type = Integer.parseInt(parts[4]);
                return new int[]{start, end, type};
            } catch (NumberFormatException e) {
               logger.error("Failed to parse message: {}", messageText, e);
               return null;
            }
        } else {
           logger.error("Failed to parse message: {}", messageText);
           return null;
        }
    }
    public int getIdFromMessage(String messageText, int part){
        String[] parts = messageText.split("_");
        if (parts.length == part+1) {
            try {
                return Integer.parseInt(parts[part]);
            } catch (NumberFormatException e) {
                logger.error("Failed to parse message: {}", messageText, e);
                return 0;
            }
        } else {
            logger.error("Failed to parse message: {}", messageText);
            return 0;
        }
    }
    public void showMainTypeMingaz(Long chatId){
        List<ReestrType> reestrTypes = reestrTypeRepository.findAllByIdIn(Arrays.asList(10, 100, 1000, 10000));
        String text = "Выберите тип объекта МинГаз:\n\n\n";
        for(ReestrType reestrType : reestrTypes){
            text += reestrType.getName() + " -  /show_objects_" + reestrType.getId() + "\n\n";
        }
        sendMessage(chatId, text, null);
    }

    public void showObjectsMingaz(Long chatId, int mainType){
        List<ReestrType> reestrObjects = reestrTypeRepository.findAllByIdBetween(mainType+1, mainType * 10-1);
        String text = "Выберите объект МинГаз:\n\n";
        for(ReestrType reestrType : reestrObjects){
            text += reestrType.getName() + "\nРеестры: /show_reestrs_0_30_" +  reestrType.getId() + "\nТОиР: /show_toir_"+reestrType.getId() + "\n\n";
        }
        sendMessage(chatId, text, null);
    }



}
