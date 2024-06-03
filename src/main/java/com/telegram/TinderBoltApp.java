package com.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "aggregator_dating_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7083219903:AAFY6LuHSei24P2Ur947XLjVUSRPgPhsttY"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "sk-proj-bbiv7C8wuSuVju0nNS4VT3BlbkFJL52e32WoW31ukEsmuJuG"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPTService = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode dialogMode = null;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();
        sendTextMessage("Вы написали: ".concat("*").concat(message).concat("*"));

        if (message.equalsIgnoreCase("/start")) {
            dialogMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            sendTextMessage("Добро пожаловать!");
            String greetMessage = loadMessage("main");
            sendTextMessage(greetMessage);

            showMainMenu(
                    "главное меню бота\n", "/start",
                    "генерация Tinder-профля \uD83D\uDE0E", "/profile",
                    "сообщение для знакомства \uD83E\uDD70", "/opener",
                    "переписка от вашего имени \uD83D\uDE08", "/message",
                    "переписка со звездами \uD83D\uDD25", "/date",
                    "задать вопрос чату GPT \uD83E\uDDE0", "/gpt"
            );
            return;
        }

        if (message.equalsIgnoreCase("/gpt")) {
            sendPhotoMessage("gpt");
            dialogMode = DialogMode.GPT;
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (dialogMode == DialogMode.GPT) {
            String prompt = loadPrompt("gpt");
            String answer = chatGPTService.sendMessage(prompt, message);
            sendTextMessage(answer);
            return;
        }

        sendTextButtonsMessage("Выберите режим работы:", "Старт", "start", "Стоп", "stop");


    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}