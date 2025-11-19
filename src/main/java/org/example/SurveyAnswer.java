package org.example;

public class SurveyAnswer {
    private int index;
    private String text;

    public SurveyAnswer(int index, String text) {
        this.index = index;
        this.text = text;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }
}
