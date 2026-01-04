package com.example.flightlab;

import java.io.Serializable;

public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private int quizPoints;
    private int missionPoints;

    public Player(String name) {
        this.name = name;
        this.quizPoints = 0;
        this.missionPoints = 0;
}

//Data for table of player's points
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuizPoints() { return quizPoints; }
    public void setQuizPoints(int quizPoints) { this.quizPoints = quizPoints; }
    public void addQuizPoint() { this.quizPoints++; }

    public int getMissionPoints() { return missionPoints; }
    public void setMissionPoints(int missionPoints) { this.missionPoints = missionPoints; }
    public void addMissionPoints(int points) { this.missionPoints += points; }

    public int getTotalScore() { return quizPoints + missionPoints; }

    @Override
    public String toString() {
        return name;
    }
}