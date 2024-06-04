package com.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "aggregator_dating_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7083219903:AAFY6LuHSei24P2Ur947XLjVUSRPgPhsttY"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = ""; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPTService = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode dialogMode = null;
    private ArrayList<String>list = new ArrayList<>();

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

        // command ChatGPT.
        if (message.equalsIgnoreCase("/gpt")) {
            sendPhotoMessage("gpt");
            dialogMode = DialogMode.GPT;
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (dialogMode == DialogMode.GPT) {
            String prompt = loadPrompt("gpt");

            Message serviceMessage = sendTextMessage("Подождите несколько секунд, думаю над ответом...");
            String answer = chatGPTService.addMessage(message);
            updateTextMessage(serviceMessage, answer);
            return;
        }

        // command Date.
        if (message.equalsIgnoreCase("/date")) {
            dialogMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendeya",
                    "Райан Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }

        if (dialogMode == DialogMode.DATE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("Отличный выбор! \nТвоя задача пригласить этого человека на свидание за 5 сообщений!");

                String prompt = loadPrompt(query);
                chatGPTService.setPrompt(prompt);
                return;
            }
            Message serviceMessage = sendTextMessage("Подождите несколько секунд, думаю над ответом...");
            String answer = chatGPTService.addMessage(message);
            updateTextMessage(serviceMessage, answer);
            return;
        }

        // command Message.
        if (message.equalsIgnoreCase("/message")) {
            dialogMode = DialogMode.MESSAGE;
            sendTextButtonsMessage("Пришлите в чат Вашу переписку.",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }

        if (dialogMode == DialogMode.MESSAGE) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);

                Message serviceMessage = sendTextMessage("Подождите несколько секунд, я придумываю лучший ответ из всех возможных...");
                String answer = chatGPTService.sendMessage(prompt, userChatHistory);
                updateTextMessage(serviceMessage, answer);
                return;
            }
            list.add(message);
            return;
        }

        sendTextButtonsMessage("Выберите режим работы:", "Старт", "start", "Стоп", "stop");


    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}