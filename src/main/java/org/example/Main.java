package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.swing.*;

public class Main {
    public static void main(String[] args) throws Exception {
        SurveyBot bot = new SurveyBot();

        // botsApi
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(bot);

        // ChatGPT client
        ChatGptApiClient chatGptClient = new ChatGptApiClient("211563820");

        // SWING
        SwingUtilities.invokeLater(() -> {
            SurveyCreatorFrame frame = new SurveyCreatorFrame(bot, chatGptClient);
            frame.setVisible(true);
        });
    }
}
