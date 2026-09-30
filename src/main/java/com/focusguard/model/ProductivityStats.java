package com.focusguard.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProductivityStats {
    private final Map<LocalDate, Integer> focusMinutesByDay = new LinkedHashMap<>();
    private final List<StudySession> recentSessions = new ArrayList<>();
    private int todayFocusMinutes;
    private int completedSessions;
    private int currentStreak;
    private int distractionsToday;
    private int totalDistractions;
    private int totalInactiveMinutes;
    private int totalFocusMinutes;
    private int bestScore;
    private double averageScore;

    public Map<LocalDate, Integer> getFocusMinutesByDay() {
        return focusMinutesByDay;
    }

    public List<StudySession> getRecentSessions() {
        return recentSessions;
    }

    public int getTodayFocusMinutes() {
        return todayFocusMinutes;
    }

    public void setTodayFocusMinutes(int todayFocusMinutes) {
        this.todayFocusMinutes = todayFocusMinutes;
    }

    public int getCompletedSessions() {
        return completedSessions;
    }

    public void setCompletedSessions(int completedSessions) {
        this.completedSessions = completedSessions;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getDistractionsToday() {
        return distractionsToday;
    }

    public void setDistractionsToday(int distractionsToday) {
        this.distractionsToday = distractionsToday;
    }

    public int getTotalDistractions() {
        return totalDistractions;
    }

    public void setTotalDistractions(int totalDistractions) {
        this.totalDistractions = totalDistractions;
    }

    public int getTotalInactiveMinutes() {
        return totalInactiveMinutes;
    }

    public void setTotalInactiveMinutes(int totalInactiveMinutes) {
        this.totalInactiveMinutes = totalInactiveMinutes;
    }

    public int getTotalFocusMinutes() {
        return totalFocusMinutes;
    }

    public void setTotalFocusMinutes(int totalFocusMinutes) {
        this.totalFocusMinutes = totalFocusMinutes;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }
}
