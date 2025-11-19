package org.example;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ChatGptApiClient {
    private String id;

    public ChatGptApiClient(String id) {
        this.id = id;
    }
    //Check balance
    public boolean checkBalance() {
        try {
            HttpResponse<String> response = Unirest.get("https://app.seker.live/fm1/check-balance")
                    .queryString("id", id)
                    .asString();
            System.out.println("check balance status " + response.getStatus());
            System.out.println("check balance body " + response.getBody());
            if (response.getStatus() != 200) {
                System.out.println("check balance HTTP error " + response.getStatus());
                return false;
            }
            JSONObject body = new JSONObject(response.getBody());
            boolean success = body.optBoolean("success", false);
            if (!success) {
                Integer errorCode = null;
                if (!body.isNull("error")) {
                    errorCode = body.optInt("error");
                }
                System.out.println("check balance API error " + errorCode);
                return false;
            }
            if (!body.isNull("extra")) {
                String extra = body.optString("extra", null);
                System.out.println("Remaining messages " + extra);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Clearing server side call history
    public void clearHistory() {
        try {
            HttpResponse<String> response = Unirest.get("https://app.seker.live/fm1/clear-history")
                    .queryString("id", id)
                    .asString();
            System.out.println("clear history status " + response.getStatus());
            System.out.println("clear history body " + response.getBody());
            if (response.getStatus() != 200) {
                System.out.println("clear history HTTP error " + response.getStatus());
                return;
            }
            JSONObject body = new JSONObject(response.getBody());
            boolean success = body.optBoolean("success", false);
            if (!success) {
                Integer errorCode = null;
                if (!body.isNull("error")) {
                    errorCode = body.optInt("error");
                }
                System.out.println("clear history API error: " + errorCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //Survey questions and answers using the ChatGPT API
    public List<SurveyQuestion> generateQuestionsFromTopic(String topic, int numQuestions) {
        List<SurveyQuestion> result = new ArrayList<>();
        String prompt =
                "צור בבקשה " + numQuestions + " שאלות סקר בעברית בנושא: \"" + topic + "\".\n" +
                        "לכל שאלה צור בין 2 ל-4 תשובות אפשריות.\n" +
                        "החזר JSON בלבד בפורמט: " +
                        "{\"questions\":[{\"text\":\"...\",\"answers\":[\"...\",\"...\"]}]}";
        try {
            HttpResponse<String> response = Unirest.get("https://app.seker.live/fm1/send-message")
                    .queryString("id", id)
                    .queryString("text", prompt)
                    .asString();
            System.out.println("send message status " + response.getStatus());
            System.out.println("send message body " + response.getBody());
            if (response.getStatus() != 200) {
                System.out.println("send message HTTP error " + response.getStatus());
                return result;
            }
            JSONObject body = new JSONObject(response.getBody());
            boolean success = body.optBoolean("success", false);
            if (!success) {
                Integer errorCode = null;
                if (!body.isNull("error")) {
                    errorCode = body.optInt("error");
                }
                System.out.println("send message API error " + errorCode);
                return result;
            }
            //The extra field is where the ChatGPT generated JSON is located
            String raw = body.optString("extra", null);
            if (raw == null || raw.isEmpty()) {
                System.out.println("send message extra is empty");
                return result;
            }
            System.out.println("RAW extra " + raw);
            String cleaned = stripCodeFence(raw);
            //Extracts the JSON block by taking the substring from first to last
            cleaned = extractJsonObject(cleaned);
            if (cleaned == null) {
                System.out.println("Could not extract JSON object from extra");
                return result;
            }
            System.out.println("Json only " + cleaned);
            JSONObject json = new JSONObject(cleaned);
            JSONArray questionsArr = json.getJSONArray("questions");
            for (int i = 0; i < questionsArr.length(); i++) {
                JSONObject qObj = questionsArr.getJSONObject(i);
                String qText = qObj.getString("text");
                JSONArray ansArr = qObj.getJSONArray("answers");
                List<SurveyAnswer> answers = new ArrayList<>();
                for (int j = 0; j < ansArr.length(); j++) {
                    answers.add(new SurveyAnswer(j, ansArr.getString(j)));
                }
                result.add(new SurveyQuestion(i, qText, answers));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    // raw original string that may include code fences or extra text
    private String stripCodeFence(String raw) {
        String s = raw.trim();
        if (s.startsWith("")) {
            int firstNewline = s.indexOf('\n');
            if (firstNewline != -1) {
                s = s.substring(firstNewline + 1);
            } else {
                s = s.substring(3);
            }
            int lastFence = s.lastIndexOf("");
            if (lastFence != -1) {
                s = s.substring(0, lastFence);
            }
        }
        return s.trim();
    }
    //Returns the JSON substring between the first '{' and last '}', or null if invalid.
    private String extractJsonObject(String text) {
        String s = text.trim();
        int firstBrace = s.indexOf('{');
        int lastBrace = s.lastIndexOf('}');
        if (firstBrace == -1 || lastBrace == -1 || lastBrace <= firstBrace) {
            return null;
        }
        return s.substring(firstBrace, lastBrace + 1).trim();
    }
}