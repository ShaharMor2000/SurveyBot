package org.example;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SurveyBot extends TelegramLongPollingBot {
    private Set<Long> community = ConcurrentHashMap.newKeySet();
    private Survey currentSurvey;
    private boolean surveyActive = false;
    private Map<Long, Set<Integer>> userAnsweredQuestions = new ConcurrentHashMap<>();
    public SurveyBot() {}

    @Override
    public String getBotUsername() {
        return "SurveyChatApiBot";
    }

    @Override
    public String getBotToken() {
        return "8282965757:AAF9FbCVTi5JVgmCGcb2B_Xs1Ehp1tt9CZ8";
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                handleSurveyCallback(update.getCallbackQuery());
                return;
            }
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleText(update.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Handle text messages
    private void handleText(Message message) throws TelegramApiException {
        long chatId = message.getChatId();
        String text = message.getText().trim();
        String lower = text.toLowerCase();

        if ("/start".equals(text) || "היי".equals(lower) || "hi".equals(lower)) {
            boolean isNew = community.add(chatId);
            if (isNew) {
                send(chatId, "ברוך הבא לקהילת הסקרים! 🎉");
                // Build a display name from Telegram user object
                String displayName = buildDisplayName(message.getFrom());
                // Notify all existing community members about the new member
                String joinText =
                        "חבר חדש הצטרף לקהילה👋\n" +
                                "שם: " + displayName + "\n" +
                                "גודל הקהילה העדכני: " + community.size() + " חברים";
                broadcastExcept(joinText, chatId);
            } else {
                send(chatId, "היי שוב! אתה כבר חלק מהקהילה ✔");
            }
        } else {
            send(chatId, "כדי להצטרף לקהילה, כתבו \"היי\" או \"Hi\" או /start");
        }
    }
    //display name
    private String buildDisplayName(User user) {
        if (user == null) return "משתמש חדש";
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        if (user.getFirstName() != null) {
            return user.getFirstName();
        }
        if (user.getUserName() != null) {
            return "@" + user.getUserName();
        }
        return "משתמש חדש";
    }

    // Helper to send a simple text message to a specific chat
    private void send(long chatId, String text) throws TelegramApiException {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(text);
        execute(msg);
    }

    //Broadcast a message to all community members except one - the sender
    private void broadcastExcept(String text, long excludedChatId) throws TelegramApiException {
        for (Long id : community) {
            if (!id.equals(excludedChatId)) {
                send(id, text);
            }
        }
    }

    //Called from the Swing GUI to create a new survey
    //community size (>=3), Ensures there is no active survey,Ensures number of questions is between 1 and 3
    // Schedules sending the survey with an optional delay
    public synchronized void createSurveyFromSwing(List<SurveyQuestion> questions, int delayMinutes) throws Exception {
        if (community.size() < 3) {
            throw new Exception("צריך לפחות 3 חברים בקהילה כדי ליצור סקר");
        }
        if (surveyActive || (currentSurvey != null && currentSurvey.isActive())) {
            throw new Exception("כבר קיים סקר פעיל, אי אפשר ליצור סקר חדש");
        }
        if (questions.isEmpty() || questions.size() > 3) {
            throw new Exception("סקר יכול להכיל 1-3 שאלות");
        }
        // Create a new Survey object snapshot of current community as participants
        String id = UUID.randomUUID().toString();
        Set<Long> participants = new HashSet<>(community);
        currentSurvey = new Survey(id, questions, participants);
        userAnsweredQuestions.clear();

        // Schedule sending the survey
        new Thread(() -> {
            try {
                if (delayMinutes > 0) {
                    Thread.sleep(delayMinutes * 60L * 1000L);
                }
                startSurveyNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //Actually starts the survey
    private synchronized void startSurveyNow() throws TelegramApiException {
        if (currentSurvey == null) return;
        if (surveyActive) return;
        surveyActive = true;
        currentSurvey.setActive(true);
        currentSurvey.setStartTimeMillis(System.currentTimeMillis());

        //Send each question to each participant
        for (Long memberId : currentSurvey.getParticipants()) {
            for (SurveyQuestion q : currentSurvey.getQuestions()) {
                SendMessage msg = new SendMessage();
                msg.setChatId(memberId);
                msg.setText("שאלה " + (q.getIndex() + 1) + ":\n" + q.getText());
                msg.setReplyMarkup(createKeyboardForQuestion(q));
                execute(msg);
            }
        }
        // Schedule closing the survey after 5 minutes
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60L * 1000L);
                closeSurveyIfStillOpen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    //Lnline keyboard for a specific question
    private InlineKeyboardMarkup createKeyboardForQuestion(SurveyQuestion q) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (SurveyAnswer ans : q.getAnswers()) {
            InlineKeyboardButton b = new InlineKeyboardButton();
            b.setText(ans.getText());
            b.setCallbackData("Survey:" + q.getIndex() + ":" + ans.getIndex());
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(b);
            rows.add(row);
        }
        InlineKeyboardMarkup kb = new InlineKeyboardMarkup();
        kb.setKeyboard(rows);
        return kb;
    }

    //Handles survey clicks, checks validity, saves the answer, and closes the survey when all users finish.
    private void handleSurveyCallback(CallbackQuery callbackQuery) throws TelegramApiException {
        if (currentSurvey == null || !surveyActive) {
            answerCallback(callbackQuery, "הסקר כבר נסגר או לא פעיל");
            return;
        }
        String data = callbackQuery.getData();
        long userId = callbackQuery.getFrom().getId();
        if (!data.startsWith("SURVEY:")) {
            return;
        }
        if (!currentSurvey.getParticipants().contains(userId)) {
            answerCallback(callbackQuery, "לא רשום בסקר זה");
            return;
        }
        String[] parts = data.split(":");
        if (parts.length != 3) {
            return;
        }
        int qIndex = Integer.parseInt(parts[1]);
        int aIndex = Integer.parseInt(parts[2]);

        //The set of question indices this user already answered
        Set<Integer> answeredSet =
                userAnsweredQuestions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet());

        // If the user already answered this question - reject
        if (answeredSet.contains(qIndex)) {
            answerCallback(callbackQuery, "כבר ענית על השאלה הזו ✅");
            return;
        }
        // Record the vote
        currentSurvey.addVote(qIndex, aIndex);
        answeredSet.add(qIndex);
        answerCallback(callbackQuery, "הצבעתך נקלטה ✅");

        // Check if all participants answered all questions
        int totalQuestions = currentSurvey.getQuestions().size();
        boolean allDone = true;
        for (Long participant : currentSurvey.getParticipants()) {
            Set<Integer> s = userAnsweredQuestions.get(participant);
            if (s == null || s.size() < totalQuestions) {
                allDone = false;
                break;
            }
        }
        if (allDone) {
            closeSurveyIfStillOpen();
        }
    }

    //Send popup text to the user as callback answer
    private void answerCallback(CallbackQuery cq, String text) throws TelegramApiException {
        AnswerCallbackQuery ack = new AnswerCallbackQuery();
        ack.setCallbackQueryId(cq.getId());
        ack.setText(text);
        execute(ack);
    }

    //Closes the active survey, marks it inactive, sets the end time, and lets the UI later show the results
    private synchronized void closeSurveyIfStillOpen() {
        if (!surveyActive || currentSurvey == null) return;
        surveyActive = false;
        currentSurvey.setActive(false);
        currentSurvey.setEndTimeMillis(System.currentTimeMillis());
    }

    //Returns a textual summary of the survey results
    //Used by Swing to display a clear report to the survey creator
    public synchronized String getSurveyResultsText() {
        if (currentSurvey == null || currentSurvey.isActive()) {
            return "אין סקר סגור להצגה כרגע";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("תוצאות הסקר:\n\n");
        int[][] counts = currentSurvey.getCounts();
        List<SurveyQuestion> questions = currentSurvey.getQuestions();
        for (int i = 0; i < questions.size(); i++) {
            SurveyQuestion q = questions.get(i);
            sb.append("שאלה ").append(i + 1).append(": ").append(q.getText()).append("\n");
            int total = 0;
            for (int c : counts[i]) {
                total += c;
            }
            //List for sorting by frequency
            List<String> rows = new ArrayList<>();
            for (int j = 0; j < q.getAnswers().size(); j++) {
                SurveyAnswer ans = q.getAnswers().get(j);
                int c = counts[i][j];
                double perc = total == 0 ? 0 : (c * 100.0 / total);
                String line = ans.getText() + " - " + c + " (" + String.format("%.1f", perc) + "%)";
                rows.add(line);
            }
            // Sort rows by count descending
            rows.sort((s1, s2) -> {
                int n1 = extractCountFromLine(s1);
                int n2 = extractCountFromLine(s2);
                return Integer.compare(n2, n1);
            });
            for (String row : rows) {
                sb.append(" • ").append(row).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    //Helper to extract the numeric count from a line of the form
    private int extractCountFromLine(String line) {
        try {
            int dash = line.lastIndexOf(" - ");
            int openParen = line.indexOf("(", dash + 3);
            String numStr = line.substring(dash + 3, openParen).trim();
            return Integer.parseInt(numStr);
        } catch (Exception e) {
            return 0;
        }
    }
    // Expose the community set
    public Set<Long> getCommunity() {
        return community;
    }
}