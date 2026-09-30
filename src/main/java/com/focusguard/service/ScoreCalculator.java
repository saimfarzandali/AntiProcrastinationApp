package com.focusguard.service;

import com.focusguard.model.StudySession;

public class ScoreCalculator {
    public int calculate(StudySession session) {
        int score = 100;
        score -= session.getDistractionCount() * 8;
        score -= session.getInactiveMinutes();
        if (session.isCompleted()) {
            score += 5;
        }
        return Math.max(0, Math.min(100, score));
    }

    public String ratingFor(int score) {
        if (score >= 90) {
            return "Excellent";
        }
        if (score >= 75) {
            return "Good";
        }
        if (score >= 50) {
            return "Needs Improvement";
        }
        return "Poor Focus";
    }
}
