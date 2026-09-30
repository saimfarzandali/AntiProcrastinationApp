package com.focusguard.model;

import java.time.LocalDateTime;

public class InactivityEvent {
    private final String sessionId;
    private final int inactiveSeconds;
    private final LocalDateTime eventTime;

    public InactivityEvent(String sessionId, int inactiveSeconds, LocalDateTime eventTime) {
        this.sessionId = sessionId;
        this.inactiveSeconds = inactiveSeconds;
        this.eventTime = eventTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getInactiveSeconds() {
        return inactiveSeconds;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }
}
