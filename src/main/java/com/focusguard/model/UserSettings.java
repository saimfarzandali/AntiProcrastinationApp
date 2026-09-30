package com.focusguard.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserSettings {
    private final IntegerProperty idleTimeoutSeconds = new SimpleIntegerProperty(60);
    private final BooleanProperty strictMode = new SimpleBooleanProperty(false);
    private final IntegerProperty defaultSessionDuration = new SimpleIntegerProperty(25);
    private final IntegerProperty breakReminderMinutes = new SimpleIntegerProperty(5);
    private final List<String> blacklistedKeywords = new ArrayList<>();

    public UserSettings() {
        blacklistedKeywords.addAll(Arrays.asList("chrome", "brave", "firefox", "vlc", "steam", "discord", "spotify"));
    }

    public int getIdleTimeoutSeconds() {
        return idleTimeoutSeconds.get();
    }

    public void setIdleTimeoutSeconds(int idleTimeoutSeconds) {
        this.idleTimeoutSeconds.set(Math.max(10, idleTimeoutSeconds));
    }

    public IntegerProperty idleTimeoutSecondsProperty() {
        return idleTimeoutSeconds;
    }

    public boolean isStrictMode() {
        return strictMode.get();
    }

    public void setStrictMode(boolean strictMode) {
        this.strictMode.set(strictMode);
    }

    public BooleanProperty strictModeProperty() {
        return strictMode;
    }

    public int getDefaultSessionDuration() {
        return defaultSessionDuration.get();
    }

    public void setDefaultSessionDuration(int defaultSessionDuration) {
        this.defaultSessionDuration.set(defaultSessionDuration);
    }

    public IntegerProperty defaultSessionDurationProperty() {
        return defaultSessionDuration;
    }

    public int getBreakReminderMinutes() {
        return breakReminderMinutes.get();
    }

    public void setBreakReminderMinutes(int breakReminderMinutes) {
        this.breakReminderMinutes.set(Math.max(1, breakReminderMinutes));
    }

    public IntegerProperty breakReminderMinutesProperty() {
        return breakReminderMinutes;
    }

    public List<String> getBlacklistedKeywords() {
        return blacklistedKeywords;
    }

    public void setBlacklistedKeywords(List<String> keywords) {
        blacklistedKeywords.clear();
        if (keywords == null || keywords.isEmpty()) {
            return;
        }
        for (String keyword : keywords) {
            String clean = keyword == null ? "" : keyword.trim().toLowerCase();
            if (!clean.isEmpty() && !blacklistedKeywords.contains(clean)) {
                blacklistedKeywords.add(clean);
            }
        }
    }
}
