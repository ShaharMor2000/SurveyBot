package org.example;

import java.util.List;

public class SurveyQuestion {
    private int index;
    private String text;
    private List<SurveyAnswer> answers;

    public SurveyQuestion(int index, String text, List<SurveyAnswer> answers) {
        this.index = index;
        this.text = text;
        this.answers = answers;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }

    public List<SurveyAnswer> getAnswers() {
        return answers;
    }
}
