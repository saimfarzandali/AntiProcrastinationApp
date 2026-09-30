package com.focusguard.view;

import com.focusguard.model.ProductivityStats;
import com.focusguard.model.StudySession;
import com.focusguard.service.AnalyticsService;
import com.focusguard.util.UiFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class DashboardView extends BorderPane {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, HH:mm");

    public DashboardView(AnalyticsService analyticsService,
                         Runnable openFocusSession,
                         Runnable openPlanner,
                         Runnable openAnalytics,
                         Runnable openSettings) {
        setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        ProductivityStats stats = analyticsService.buildStats();

        VBox content = new VBox(22);
        content.setPadding(new Insets(26));
        content.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = UiFactory.title("FocusGuard");
        Label subtitle = UiFactory.muted("Anti-procrastination desktop assistant for safer, smarter study sessions.");

        HBox metrics = new HBox(14);
        metrics.getChildren().addAll(
                UiFactory.metricCard("Today's focus time", formatMinutes(stats.getTodayFocusMinutes()), UiFactory.PRIMARY),
                UiFactory.metricCard("Completed sessions", Integer.toString(stats.getCompletedSessions()), UiFactory.GREEN),
                UiFactory.metricCard("Current streak", stats.getCurrentStreak() + " day(s)", UiFactory.TEAL),
                UiFactory.metricCard("Distractions today", Integer.toString(stats.getDistractionsToday()), UiFactory.ORANGE)
        );
        for (javafx.scene.Node node : metrics.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
            UiFactory.stretch(node);
        }

        Label quickTitle = UiFactory.sectionTitle("Quick Actions");
        GridPane actions = new GridPane();
        actions.setHgap(14);
        actions.setVgap(14);
        for (int index = 0; index < 4; index++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(25);
            actions.getColumnConstraints().add(column);
        }

        Button focusCard = UiFactory.actionCard("Start Focus Session", "Run a monitored timer", UiFactory.PRIMARY);
        focusCard.setOnAction(event -> openFocusSession.run());
        Button plannerCard = UiFactory.actionCard("Task Planner", "Plan study work by priority", UiFactory.TEAL);
        plannerCard.setOnAction(event -> openPlanner.run());
        Button analyticsCard = UiFactory.actionCard("Analytics", "Review scores and trends", UiFactory.GREEN);
        analyticsCard.setOnAction(event -> openAnalytics.run());
        Button settingsCard = UiFactory.actionCard("Settings", "Tune strict mode and alerts", UiFactory.ORANGE);
        settingsCard.setOnAction(event -> openSettings.run());
        actions.add(focusCard, 0, 0);
        actions.add(plannerCard, 1, 0);
        actions.add(analyticsCard, 2, 0);
        actions.add(settingsCard, 3, 0);
        for (javafx.scene.Node node : actions.getChildren()) {
            GridPane.setHgrow(node, Priority.ALWAYS);
            UiFactory.stretch(node);
        }

        VBox recent = UiFactory.card(12);
        recent.getChildren().add(UiFactory.sectionTitle("Recent Session Summary"));
        if (stats.getRecentSessions().isEmpty()) {
            recent.getChildren().add(UiFactory.muted("No sessions yet. Start a focus session to build your history."));
        } else {
            for (StudySession session : stats.getRecentSessions()) {
                recent.getChildren().add(sessionRow(session));
            }
        }

        content.getChildren().addAll(title, subtitle, metrics, quickTitle, actions, recent);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        setCenter(scrollPane);
    }

    private HBox sessionRow(StudySession session) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        VBox text = new VBox(3);
        Label goal = UiFactory.body(session.getGoal());
        goal.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 14));
        Label detail = UiFactory.muted(session.getStartTime().format(DATE_FORMAT)
                + " | " + session.getActualFocusMinutes() + " min"
                + " | " + session.getDistractionCount() + " distractions");
        text.getChildren().addAll(goal, detail);

        Label score = UiFactory.badge("Score " + session.getScore(), UiFactory.SURFACE_ALT, UiFactory.TEXT);
        row.getChildren().addAll(text, score);
        HBox.setHgrow(text, Priority.ALWAYS);
        return row;
    }

    private String formatMinutes(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        return (minutes / 60) + "h " + (minutes % 60) + "m";
    }
}
