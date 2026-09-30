package com.focusguard.model;

import java.time.LocalDateTime;

public class DistractionEvent {
    private final String sessionId;
    private final String appName;
    private final LocalDateTime eventTime;

    public DistractionEvent(String sessionId, String appName, LocalDateTime eventTime) {
        this.sessionId = sessionId;
        this.appName = appName;
        this.eventTime = eventTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAppName() {
        return appName;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }
}
