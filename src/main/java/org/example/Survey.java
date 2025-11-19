package org.example;

import java.util.Set;
import java.util.List;

public class Survey {
    private String id;
    private List<SurveyQuestion> questions;
    private boolean active;
    private long startTimeMillis;
    private long endTimeMillis;
    private Set<Long> participants;


    private int[][] counts;

    public Survey(String id, List<SurveyQuestion> questions, Set<Long> participants) {
        this.id = id;
        this.questions = questions;
        this.participants = participants;
        this.active = false;

        this.counts = new int[questions.size()][];
        for (int i = 0; i < questions.size(); i++) {
            int answersCount = questions.get(i).getAnswers().size();
            this.counts[i] = new int[answersCount];
        }
    }

    public String getId() {
        return id;
    }

    public List<SurveyQuestion> getQuestions() {
        return questions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
    }

    public Set<Long> getParticipants() {
        return participants;
    }

    public int[][] getCounts() {
        return counts;
    }

    public void addVote(int questionIndex, int answerIndex) {
        if (questionIndex >= 0 && questionIndex < counts.length) {
            if (answerIndex >= 0 && answerIndex < counts[questionIndex].length) {
                counts[questionIndex][answerIndex]++;
            }
        }
    }
}
