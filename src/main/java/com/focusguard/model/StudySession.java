package com.focusguard.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StudySession {
    private final String id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String goal;
    private int plannedDurationMinutes;
    private int actualFocusSeconds;
    private int distractionCount;
    private int inactiveSeconds;
    private boolean completed;
    private int score;
    private final List<DistractionEvent> distractionEvents = new ArrayList<>();
    private final List<InactivityEvent> inactivityEvents = new ArrayList<>();

    public StudySession(String goal, int plannedDurationMinutes) {
        this(UUID.randomUUID().toString(), LocalDateTime.now(), null, goal, plannedDurationMinutes, 0, 0, 0, false, 0);
    }

    public StudySession(String id, LocalDateTime startTime, LocalDateTime endTime, String goal,
                        int plannedDurationMinutes, int actualFocusSeconds, int distractionCount,
                        int inactiveSeconds, boolean completed, int score) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.goal = goal;
        this.plannedDurationMinutes = plannedDurationMinutes;
        this.actualFocusSeconds = actualFocusSeconds;
        this.distractionCount = distractionCount;
        this.inactiveSeconds = inactiveSeconds;
        this.completed = completed;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public int getPlannedDurationMinutes() {
        return plannedDurationMinutes;
    }

    public void setPlannedDurationMinutes(int plannedDurationMinutes) {
        this.plannedDurationMinutes = plannedDurationMinutes;
    }

    public int getActualFocusSeconds() {
        return actualFocusSeconds;
    }

    public void setActualFocusSeconds(int actualFocusSeconds) {
        this.actualFocusSeconds = Math.max(0, actualFocusSeconds);
    }

    public int getActualFocusMinutes() {
        return actualFocusSeconds == 0 ? 0 : (int) Math.ceil(actualFocusSeconds / 60.0);
    }

    public int getDistractionCount() {
        return distractionCount;
    }

    public void setDistractionCount(int distractionCount) {
        this.distractionCount = Math.max(0, distractionCount);
    }

    public int getInactiveSeconds() {
        return inactiveSeconds;
    }

    public void setInactiveSeconds(int inactiveSeconds) {
        this.inactiveSeconds = Math.max(0, inactiveSeconds);
    }

    public int getInactiveMinutes() {
        return inactiveSeconds == 0 ? 0 : (int) Math.ceil(inactiveSeconds / 60.0);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));
    }

    public List<DistractionEvent> getDistractionEvents() {
        return distractionEvents;
    }

    public List<InactivityEvent> getInactivityEvents() {
        return inactivityEvents;
    }

    public void addDistraction(String appName, LocalDateTime eventTime) {
        distractionCount++;
        distractionEvents.add(new DistractionEvent(id, appName, eventTime));
    }

    public void addInactivity(int seconds, LocalDateTime eventTime) {
        inactiveSeconds += Math.max(0, seconds);
        inactivityEvents.add(new InactivityEvent(id, Math.max(0, seconds), eventTime));
    }
}
