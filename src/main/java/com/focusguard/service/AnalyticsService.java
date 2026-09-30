package com.focusguard.service;

import com.focusguard.model.ProductivityStats;
import com.focusguard.model.StudySession;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalyticsService {
    private final SessionService sessionService;

    public AnalyticsService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public ProductivityStats buildStats() {
        ProductivityStats stats = new ProductivityStats();
        List<StudySession> sessions = sessionService.loadSessions();
        LocalDate today = LocalDate.now();
        for (int day = 6; day >= 0; day--) {
            stats.getFocusMinutesByDay().put(today.minusDays(day), 0);
        }

        int scoreTotal = 0;
        int scoredSessions = 0;
        Set<LocalDate> completedDays = new HashSet<>();

        for (StudySession session : sessions) {
            LocalDate date = session.getStartTime().toLocalDate();
            int focusMinutes = session.getActualFocusMinutes();
            if (stats.getFocusMinutesByDay().containsKey(date)) {
                stats.getFocusMinutesByDay().put(date, stats.getFocusMinutesByDay().get(date) + focusMinutes);
            }
            if (date.equals(today)) {
                stats.setTodayFocusMinutes(stats.getTodayFocusMinutes() + focusMinutes);
                stats.setDistractionsToday(stats.getDistractionsToday() + session.getDistractionCount());
            }
            if (session.isCompleted()) {
                stats.setCompletedSessions(stats.getCompletedSessions() + 1);
                completedDays.add(date);
            }
            stats.setTotalDistractions(stats.getTotalDistractions() + session.getDistractionCount());
            stats.setTotalInactiveMinutes(stats.getTotalInactiveMinutes() + session.getInactiveMinutes());
            stats.setTotalFocusMinutes(stats.getTotalFocusMinutes() + focusMinutes);
            stats.setBestScore(Math.max(stats.getBestScore(), session.getScore()));
            scoreTotal += session.getScore();
            scoredSessions++;
        }

        if (scoredSessions > 0) {
            stats.setAverageScore(scoreTotal / (double) scoredSessions);
        }
        LocalDate cursor = today;
        int streak = 0;
        while (completedDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        stats.setCurrentStreak(streak);
        stats.getRecentSessions().addAll(sessions.stream().limit(5).toList());
        return stats;
    }
}
