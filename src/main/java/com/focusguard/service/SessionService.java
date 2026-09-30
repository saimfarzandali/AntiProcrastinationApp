package com.focusguard.service;

import com.focusguard.model.DistractionEvent;
import com.focusguard.model.InactivityEvent;
import com.focusguard.model.StudySession;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SessionService {
    private static final String SESSION_HEADER = "id,startTime,endTime,goal,plannedMinutes,actualSeconds,distractions,inactiveSeconds,completed,score";
    private static final String EVENT_HEADER = "sessionId,type,time,detail,seconds";
    private final FileStorageService storageService;

    public SessionService(FileStorageService storageService) {
        this.storageService = storageService;
        storageService.ensureFile(storageService.sessionsPath(), SESSION_HEADER);
        storageService.ensureFile(storageService.eventsPath(), EVENT_HEADER);
    }

    public synchronized List<StudySession> loadSessions() {
        List<StudySession> sessions = new ArrayList<>();
        List<String> lines = storageService.readLines(storageService.sessionsPath());
        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.trim().isEmpty()) {
                continue;
            }
            List<String> parts = FileStorageService.parseCsv(line);
            if (parts.size() < 10) {
                continue;
            }
            try {
                LocalDateTime start = LocalDateTime.parse(parts.get(1));
                LocalDateTime end = parts.get(2).isEmpty() ? null : LocalDateTime.parse(parts.get(2));
                StudySession session = new StudySession(
                        parts.get(0),
                        start,
                        end,
                        parts.get(3),
                        parseInt(parts.get(4)),
                        parseInt(parts.get(5)),
                        parseInt(parts.get(6)),
                        parseInt(parts.get(7)),
                        Boolean.parseBoolean(parts.get(8)),
                        parseInt(parts.get(9))
                );
                sessions.add(session);
            } catch (RuntimeException ignored) {
                // Invalid rows are ignored so a damaged line cannot crash the app.
            }
        }
        sessions.sort(Comparator.comparing(StudySession::getStartTime).reversed());
        return sessions;
    }

    public synchronized void saveSession(StudySession session) {
        List<StudySession> sessions = loadSessions().stream()
                .filter(existing -> !existing.getId().equals(session.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        sessions.add(session);
        sessions.sort(Comparator.comparing(StudySession::getStartTime));

        List<String> lines = new ArrayList<>();
        lines.add(SESSION_HEADER);
        for (StudySession saved : sessions) {
            lines.add(serializeSession(saved));
        }
        storageService.writeLines(storageService.sessionsPath(), lines);
    }

    public synchronized void logDistractionEvent(DistractionEvent event) {
        storageService.appendLine(storageService.eventsPath(),
                FileStorageService.csvLine(event.getSessionId(), "DISTRACTION", event.getEventTime().toString(), event.getAppName(), "0"),
                EVENT_HEADER);
    }

    public synchronized void logInactivityEvent(InactivityEvent event) {
        storageService.appendLine(storageService.eventsPath(),
                FileStorageService.csvLine(event.getSessionId(), "INACTIVITY", event.getEventTime().toString(), "Are you still studying?", Integer.toString(event.getInactiveSeconds())),
                EVENT_HEADER);
    }

    public synchronized Path exportSummaryReport() {
        List<StudySession> sessions = loadSessions();
        List<String> lines = new ArrayList<>();
        lines.add("FocusGuard Session Report");
        lines.add("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        lines.add("");
        if (sessions.isEmpty()) {
            lines.add("No focus sessions recorded yet.");
        } else {
            for (StudySession session : sessions) {
                lines.add(session.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        + " | " + session.getGoal()
                        + " | planned " + session.getPlannedDurationMinutes() + " min"
                        + " | actual " + session.getActualFocusMinutes() + " min"
                        + " | distractions " + session.getDistractionCount()
                        + " | inactive " + session.getInactiveMinutes() + " min"
                        + " | completed " + (session.isCompleted() ? "yes" : "no")
                        + " | score " + session.getScore());
            }
        }
        try {
            Files.write(storageService.reportPath(), lines, StandardCharsets.UTF_8);
            return storageService.reportPath();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to export report", ex);
        }
    }

    private String serializeSession(StudySession session) {
        return FileStorageService.csvLine(
                session.getId(),
                session.getStartTime().toString(),
                session.getEndTime() == null ? "" : session.getEndTime().toString(),
                session.getGoal(),
                Integer.toString(session.getPlannedDurationMinutes()),
                Integer.toString(session.getActualFocusSeconds()),
                Integer.toString(session.getDistractionCount()),
                Integer.toString(session.getInactiveSeconds()),
                Boolean.toString(session.isCompleted()),
                Integer.toString(session.getScore())
        );
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
