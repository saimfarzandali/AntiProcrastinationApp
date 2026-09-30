package com.focusguard.app;

import com.focusguard.model.UserSettings;
import com.focusguard.service.AnalyticsService;
import com.focusguard.service.FileStorageService;
import com.focusguard.service.ScoreCalculator;
import com.focusguard.service.SessionService;
import com.focusguard.service.SettingsService;
import com.focusguard.service.TaskService;
import com.focusguard.util.UiFactory;
import com.focusguard.view.AnalyticsView;
import com.focusguard.view.DashboardView;
import com.focusguard.view.FocusSessionView;
import com.focusguard.view.HistoryView;
import com.focusguard.view.SettingsView;
import com.focusguard.view.TaskPlannerView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainApp extends Application {
    private StackPane contentPane;
    private Button dashboardButton;
    private Button focusButton;
    private Button plannerButton;
    private Button analyticsButton;
    private Button historyButton;
    private Button settingsButton;

    private SessionService sessionService;
    private TaskService taskService;
    private SettingsService settingsService;
    private AnalyticsService analyticsService;
    private ScoreCalculator scoreCalculator;
    private UserSettings userSettings;
    private FocusSessionView focusSessionView;

    public static void main(String[] args) {
        launch(args);
    }

    public void start() {
        start(null);
    }

    @Override
    public void start(@org.jetbrains.annotations.NotNull Stage stage) {
        initializeServices();

        BorderPane shell = new BorderPane();
        shell.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        shell.setLeft(createSidebar());

        contentPane = new StackPane();
        contentPane.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        shell.setCenter(contentPane);

        Scene scene = new Scene(shell, 1180, 760);
        stage.setTitle("FocusGuard - Anti-Procrastination Desktop Assistant");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            if (focusSessionView != null) {
                focusSessionView.shutdown();
            }
        });

        showDashboard();
        stage.show();
    }

    private void initializeServices() {
        FileStorageService storageService = new FileStorageService();
        settingsService = new SettingsService(storageService);
        userSettings = settingsService.loadSettings();
        sessionService = new SessionService(storageService);
        taskService = new TaskService(storageService);
        analyticsService = new AnalyticsService(sessionService);
        scoreCalculator = new ScoreCalculator();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(22, 16, 22, 16));
        sidebar.setPrefWidth(232);
        sidebar.setBackground(new Background(new BackgroundFill(UiFactory.DARK, CornerRadii.EMPTY, Insets.EMPTY)));

        Label brand = new Label("FocusGuard");
        brand.setTextFill(Color.WHITE);
        brand.setFont(Font.font("System", FontWeight.BOLD, 24));
        Label tagline = new Label("Study assistant");
        tagline.setTextFill(Color.web("#9CA3AF"));
        tagline.setFont(Font.font("System", 12));

        dashboardButton = UiFactory.navButton("Dashboard");
        focusButton = UiFactory.navButton("Focus Session");
        plannerButton = UiFactory.navButton("Task Planner");
        analyticsButton = UiFactory.navButton("Analytics");
        historyButton = UiFactory.navButton("History");
        settingsButton = UiFactory.navButton("Settings");

        dashboardButton.setOnAction(event -> showDashboard());
        focusButton.setOnAction(event -> showFocusSession());
        plannerButton.setOnAction(event -> showTaskPlanner());
        analyticsButton.setOnAction(event -> showAnalytics());
        historyButton.setOnAction(event -> showHistory());
        settingsButton.setOnAction(event -> showSettings());

        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label safety = new Label("Safe detection only\nNo OS lock or forced kills");
        safety.setTextFill(Color.web("#9CA3AF"));
        safety.setFont(Font.font("System", 12));

        sidebar.getChildren().addAll(brand, tagline, dashboardButton, focusButton, plannerButton,
                analyticsButton, historyButton, settingsButton, spacer, safety);
        return sidebar;
    }

    private void showDashboard() {
        DashboardView dashboardView = new DashboardView(analyticsService, this::showFocusSession,
                this::showTaskPlanner, this::showAnalytics, this::showSettings);
        showView(dashboardView, dashboardButton);
    }

    private void showFocusSession() {
        if (focusSessionView == null) {
            focusSessionView = new FocusSessionView(sessionService, scoreCalculator, userSettings, this::showDashboard);
        }
        focusSessionView.refreshSettings();
        showView(focusSessionView, focusButton);
    }

    private void showTaskPlanner() {
        showView(new TaskPlannerView(taskService), plannerButton);
    }

    private void showAnalytics() {
        showView(new AnalyticsView(analyticsService), analyticsButton);
    }

    private void showHistory() {
        showView(new HistoryView(sessionService, scoreCalculator), historyButton);
    }

    private void showSettings() {
        showView(new SettingsView(settingsService, userSettings), settingsButton);
    }

    private void showView(Parent view, Button activeButton) {
        contentPane.getChildren().setAll(view);
        Button[] buttons = {dashboardButton, focusButton, plannerButton, analyticsButton, historyButton, settingsButton};
        for (Button button : buttons) {
            UiFactory.markNavButtonActive(button, button == activeButton);
        }
    }
}
