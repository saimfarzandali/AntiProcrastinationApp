package com.focusguard.view;

import com.focusguard.model.StudySession;
import com.focusguard.model.UserSettings;
import com.focusguard.service.DistractionMonitor;
import com.focusguard.service.InactivityMonitor;
import com.focusguard.service.ScoreCalculator;
import com.focusguard.service.SessionService;
import com.focusguard.util.UiFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class FocusSessionView extends BorderPane {
    private final SessionService sessionService;
    private final ScoreCalculator scoreCalculator;
    private final UserSettings settings;
    private final Runnable sessionSavedCallback;

    private TextField goalField;
    private ComboBox<Integer> durationBox;
    private Label timerLabel;
    private Label goalLabel;
    private Label strictLabel;
    private Label inactiveLabel;
    private Label distractionLabel;
    private Label warningBanner;
    private ProgressBar progressBar;
    private Button startButton;
    private Button pauseButton;
    private Button resumeButton;
    private Button endButton;

    private Timeline countdown;
    private InactivityMonitor inactivityMonitor;
    private DistractionMonitor distractionMonitor;
    private StudySession currentSession;
    private int remainingSeconds;
    private int elapsedSeconds;
    private boolean running;
    private boolean paused;

    public FocusSessionView(SessionService sessionService, ScoreCalculator scoreCalculator,
                            UserSettings settings, Runnable sessionSavedCallback) {
        this.sessionService = sessionService;
        this.scoreCalculator = scoreCalculator;
        this.settings = settings;
        this.sessionSavedCallback = sessionSavedCallback;
        buildUi();
        updateControls();
    }

    public void shutdown() {
        stopCountdown();
        stopMonitors();
    }

    public void refreshSettings() {
        if (!running && durationBox != null) {
            Integer defaultDuration = settings.getDefaultSessionDuration();
            if (durationBox.getItems().contains(defaultDuration)) {
                durationBox.getSelectionModel().select(defaultDuration);
            }
        }
        updateSessionDisplay();
    }

    private void buildUi() {
        setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox content = new VBox(20);
        content.setPadding(new Insets(26));
        content.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = UiFactory.title("Focus Session");
        Label subtitle = UiFactory.muted("Set a study goal, start the timer, and let FocusGuard watch for inactivity and distraction signals.");

        warningBanner = UiFactory.body("");
        warningBanner.setPadding(new Insets(12));
        warningBanner.setVisible(false);
        warningBanner.setManaged(false);

        HBox main = new HBox(18);
        main.setAlignment(Pos.TOP_LEFT);

        VBox setupCard = UiFactory.card(14);
        setupCard.setPrefWidth(360);
        setupCard.getChildren().add(UiFactory.sectionTitle("Session Setup"));

        goalField = new TextField();
        goalField.setPromptText("Example: Revise OOP inheritance notes");
        UiFactory.styleTextInput(goalField);

        durationBox = new ComboBox<>();
        durationBox.getItems().addAll(15, 25, 45, 60);
        Integer defaultDuration = settings.getDefaultSessionDuration();
        if (durationBox.getItems().contains(defaultDuration)) {
            durationBox.getSelectionModel().select(defaultDuration);
        } else {
            durationBox.getSelectionModel().select(Integer.valueOf(25));
        }
        UiFactory.styleCombo(durationBox);

        startButton = UiFactory.primaryButton("Start Session");
        pauseButton = UiFactory.secondaryButton("Pause");
        resumeButton = UiFactory.secondaryButton("Resume");
        endButton = UiFactory.dangerButton("End Session");
        startButton.setOnAction(event -> startSession());
        pauseButton.setOnAction(event -> pauseSession());
        resumeButton.setOnAction(event -> resumeSession());
        endButton.setOnAction(event -> finishSession(false));

        HBox buttonRow = new HBox(10, startButton, pauseButton, resumeButton, endButton);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        setupCard.getChildren().addAll(labelWithControl("Goal", goalField), labelWithControl("Duration", durationBox), buttonRow);

        VBox timerCard = UiFactory.card(18);
        timerCard.setAlignment(Pos.CENTER);
        timerCard.setMaxWidth(Double.MAX_VALUE);
        timerLabel = new Label("25:00");
        timerLabel.setTextFill(UiFactory.TEXT);
        timerLabel.setFont(Font.font("System", FontWeight.BOLD, 66));
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(12);

        goalLabel = UiFactory.body("Current goal: No active session");
        goalLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        strictLabel = UiFactory.badge(settings.isStrictMode() ? "Strict mode ON" : "Strict mode OFF",
                settings.isStrictMode() ? Color.web("#FEE2E2") : Color.web("#E0F2FE"),
                settings.isStrictMode() ? UiFactory.RED : UiFactory.PRIMARY);
        inactiveLabel = UiFactory.body("Inactivity warnings: 0");
        distractionLabel = UiFactory.body("Distraction warnings: 0");

        HBox counters = new HBox(16, inactiveLabel, distractionLabel, strictLabel);
        counters.setAlignment(Pos.CENTER);
        timerCard.getChildren().addAll(timerLabel, progressBar, goalLabel, counters);

        main.getChildren().addAll(setupCard, timerCard);
        HBox.setHgrow(timerCard, Priority.ALWAYS);

        content.getChildren().addAll(title, subtitle, warningBanner, main);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        setCenter(scrollPane);
    }

    private VBox labelWithControl(String labelText, javafx.scene.Node control) {
        VBox box = new VBox(7);
        Label label = UiFactory.muted(labelText);
        box.getChildren().addAll(label, control);
        UiFactory.stretch(control);
        return box;
    }

    private void startSession() {
        String goal = goalField.getText() == null ? "" : goalField.getText().trim();
        if (goal.isEmpty()) {
            showBanner("Please enter a clear study goal before starting.", Color.rgb(245, 158, 11, 0.18), UiFactory.TEXT);
            return;
        }
        int plannedMinutes = durationBox.getValue() == null ? 25 : durationBox.getValue();
        currentSession = new StudySession(goal, plannedMinutes);
        remainingSeconds = plannedMinutes * 60;
        elapsedSeconds = 0;
        running = true;
        paused = false;
        updateSessionDisplay();
        startCountdown();
        startMonitors();
        showBanner("Focus session started. Keep your work steady.", Color.rgb(15, 159, 143, 0.14), UiFactory.TEXT);
        updateControls();
    }

    private void pauseSession() {
        if (!running || paused) {
            return;
        }
        paused = true;
        if (countdown != null) {
            countdown.pause();
        }
        stopMonitors();
        showBanner("Session paused. Monitoring will resume when you continue.", Color.rgb(99, 112, 131, 0.16), UiFactory.TEXT);
        updateControls();
    }

    private void resumeSession() {
        if (!running || !paused) {
            return;
        }
        paused = false;
        if (countdown != null) {
            countdown.play();
        }
        startMonitors();
        showBanner("Session resumed.", Color.rgb(15, 159, 143, 0.14), UiFactory.TEXT);
        updateControls();
    }

    private void finishSession(boolean completed) {
        if (!running || currentSession == null) {
            return;
        }
        stopCountdown();
        stopMonitors();
        currentSession.setEndTime(LocalDateTime.now());
        currentSession.setActualFocusSeconds(elapsedSeconds);
        currentSession.setCompleted(completed);
        currentSession.setScore(scoreCalculator.calculate(currentSession));
        sessionService.saveSession(currentSession);

        StudySession finished = currentSession;
        currentSession = null;
        running = false;
        paused = false;
        remainingSeconds = settings.getDefaultSessionDuration() * 60;
        elapsedSeconds = 0;
        updateSessionDisplay();
        updateControls();
        showSummary(finished);
        sessionSavedCallback.run();
    }

    private void startCountdown() {
        stopCountdown();
        countdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (!running || paused || currentSession == null) {
                return;
            }
            remainingSeconds = Math.max(0, remainingSeconds - 1);
            elapsedSeconds++;
            currentSession.setActualFocusSeconds(elapsedSeconds);
            updateSessionDisplay();
            if (remainingSeconds <= 0) {
                finishSession(true);
            }
        }));
        countdown.setCycleCount(Timeline.INDEFINITE);
        countdown.play();
    }

    private void stopCountdown() {
        if (countdown != null) {
            countdown.stop();
            countdown = null;
        }
    }

    private void startMonitors() {
        stopMonitors();
        inactivityMonitor = new InactivityMonitor(settings.getIdleTimeoutSeconds(), this::handleInactivity);
        distractionMonitor = new DistractionMonitor(new ArrayList<>(settings.getBlacklistedKeywords()), this::handleDistraction);
        inactivityMonitor.start();
        distractionMonitor.start();
    }

    private void stopMonitors() {
        if (inactivityMonitor != null) {
            inactivityMonitor.stop();
            inactivityMonitor = null;
        }
        if (distractionMonitor != null) {
            distractionMonitor.stop();
            distractionMonitor = null;
        }
    }

    private void handleInactivity() {
        if (!running || paused || currentSession == null) {
            return;
        }
        currentSession.addInactivity(settings.getIdleTimeoutSeconds(), LocalDateTime.now());
        sessionService.logInactivityEvent(currentSession.getInactivityEvents().get(currentSession.getInactivityEvents().size() - 1));
        updateSessionDisplay();
        showBanner("Are you still studying? Move back to your goal when ready.", Color.rgb(245, 158, 11, 0.20), UiFactory.TEXT);
    }

    private void handleDistraction(String appName) {
        if (!running || paused || currentSession == null) {
            return;
        }
        currentSession.addDistraction(appName, LocalDateTime.now());
        sessionService.logDistractionEvent(currentSession.getDistractionEvents().get(currentSession.getDistractionEvents().size() - 1));
        updateSessionDisplay();

        if (settings.isStrictMode()) {
            showStrictWarning(appName);
        } else {
            showBanner("Distraction detected: " + appName + ". FocusGuard logged a warning only.", Color.rgb(220, 38, 38, 0.15), UiFactory.TEXT);
        }
    }

    private void showStrictWarning(String appName) {
        boolean shouldResume = running && !paused;
        if (countdown != null) {
            countdown.pause();
        }
        Stage owner = getScene() == null ? null : (Stage) getScene().getWindow();
        FocusWarningOverlay overlay = new FocusWarningOverlay(owner, appName, 10);
        overlay.showAndWait();
        if (shouldResume && running && !paused && countdown != null) {
            countdown.play();
        }
    }

    private void updateSessionDisplay() {
        int secondsToShow = running ? remainingSeconds : settings.getDefaultSessionDuration() * 60;
        timerLabel.setText(formatTime(secondsToShow));
        if (running && currentSession != null) {
            goalLabel.setText("Current goal: " + currentSession.getGoal());
            inactiveLabel.setText("Inactivity warnings: " + currentSession.getInactivityEvents().size());
            distractionLabel.setText("Distraction warnings: " + currentSession.getDistractionCount());
            double plannedSeconds = Math.max(1, currentSession.getPlannedDurationMinutes() * 60.0);
            progressBar.setProgress(Math.min(1.0, elapsedSeconds / plannedSeconds));
        } else {
            goalLabel.setText("Current goal: No active session");
            inactiveLabel.setText("Inactivity warnings: 0");
            distractionLabel.setText("Distraction warnings: 0");
            progressBar.setProgress(0);
        }
        updateStrictBadge();
    }

    private void updateControls() {
        goalField.setDisable(running);
        durationBox.setDisable(running);
        startButton.setDisable(running);
        pauseButton.setDisable(!running || paused);
        resumeButton.setDisable(!running || !paused);
        endButton.setDisable(!running);
    }

    private void updateStrictBadge() {
        strictLabel.setText(settings.isStrictMode() ? "Strict mode ON" : "Strict mode OFF");
        strictLabel.setTextFill(settings.isStrictMode() ? UiFactory.RED : UiFactory.PRIMARY);
        strictLabel.setBackground(new Background(new BackgroundFill(
                settings.isStrictMode() ? Color.web("#FEE2E2") : Color.web("#E0F2FE"),
                new CornerRadii(999),
                Insets.EMPTY
        )));
    }

    private void showBanner(String message, Color background, Color textColor) {
        warningBanner.setText(message);
        warningBanner.setTextFill(textColor);
        warningBanner.setBackground(new Background(new BackgroundFill(background, new CornerRadii(8), Insets.EMPTY)));
        warningBanner.setVisible(true);
        warningBanner.setManaged(true);
        Timeline hide = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            warningBanner.setVisible(false);
            warningBanner.setManaged(false);
        }));
        hide.play();
    }

    private void showSummary(StudySession session) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Session Summary");
        alert.setHeaderText("Focus session saved");
        alert.setContentText("Goal: " + session.getGoal()
                + "\nActual focus: " + session.getActualFocusMinutes() + " min"
                + "\nDistractions: " + session.getDistractionCount()
                + "\nInactive time: " + session.getInactiveMinutes() + " min"
                + "\nCompleted: " + (session.isCompleted() ? "yes" : "no")
                + "\nScore: " + session.getScore() + " (" + scoreCalculator.ratingFor(session.getScore()) + ")");
        if (getScene() != null) {
            alert.initOwner(getScene().getWindow());
        }
        alert.showAndWait();
    }

    private String formatTime(int totalSeconds) {
        int minutes = Math.max(0, totalSeconds) / 60;
        int seconds = Math.max(0, totalSeconds) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
