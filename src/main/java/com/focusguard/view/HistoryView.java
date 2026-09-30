package com.focusguard.view;

import com.focusguard.model.StudySession;
import com.focusguard.service.ScoreCalculator;
import com.focusguard.service.SessionService;
import com.focusguard.util.UiFactory;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class HistoryView extends BorderPane {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final SessionService sessionService;
    private final ScoreCalculator scoreCalculator;

    public HistoryView(SessionService sessionService, ScoreCalculator scoreCalculator) {
        this.sessionService = sessionService;
        this.scoreCalculator = scoreCalculator;
        buildUi();
    }

    private void buildUi() {
        setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox content = new VBox(20);
        content.setPadding(new Insets(26));
        content.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = UiFactory.title("History");
        Label subtitle = UiFactory.muted("All previous focus sessions are listed here with score and completion details.");

        TableView<StudySession> tableView = createTable();
        Button exportButton = UiFactory.primaryButton("Export Summary");
        exportButton.setOnAction(event -> exportSummary());
        HBox actions = new HBox(10, exportButton);

        content.getChildren().addAll(title, subtitle, tableView, actions);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        setCenter(scrollPane);
    }

    private TableView<StudySession> createTable() {
        TableView<StudySession> tableView = new TableView<>();
        TableColumn<StudySession, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getStartTime().format(DATE_FORMAT)));
        TableColumn<StudySession, String> goalColumn = new TableColumn<>("Goal");
        goalColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getGoal()));
        TableColumn<StudySession, Number> plannedColumn = new TableColumn<>("Planned");
        plannedColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().getPlannedDurationMinutes()));
        TableColumn<StudySession, Number> actualColumn = new TableColumn<>("Actual");
        actualColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().getActualFocusMinutes()));
        TableColumn<StudySession, Number> distractionColumn = new TableColumn<>("Distractions");
        distractionColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().getDistractionCount()));
        TableColumn<StudySession, Number> inactiveColumn = new TableColumn<>("Inactive");
        inactiveColumn.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().getInactiveMinutes()));
        TableColumn<StudySession, String> completedColumn = new TableColumn<>("Completed");
        completedColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().isCompleted() ? "yes" : "no"));
        TableColumn<StudySession, String> scoreColumn = new TableColumn<>("Score");
        scoreColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getScore()
                + " " + scoreCalculator.ratingFor(cell.getValue().getScore())));

        tableView.getColumns().clear();
        tableView.getColumns().add(dateColumn);
        tableView.getColumns().add(goalColumn);
        tableView.getColumns().add(plannedColumn);
        tableView.getColumns().add(actualColumn);
        tableView.getColumns().add(distractionColumn);
        tableView.getColumns().add(inactiveColumn);
        tableView.getColumns().add(completedColumn);
        tableView.getColumns().add(scoreColumn);
        tableView.setItems(FXCollections.observableArrayList(sessionService.loadSessions()));
        tableView.setPrefHeight(500);
        UiFactory.styleTable(tableView);
        return tableView;
    }

    private void exportSummary() {
        Path path = sessionService.exportSummaryReport();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Complete");
        alert.setHeaderText("Summary report exported");
        alert.setContentText(path.toString());
        if (getScene() != null) {
            alert.initOwner(getScene().getWindow());
        }
        alert.showAndWait();
    }
}
