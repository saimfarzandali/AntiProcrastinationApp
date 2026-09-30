package com.focusguard.view;

import com.focusguard.model.StudyTask;
import com.focusguard.service.TaskService;
import com.focusguard.util.UiFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class TaskPlannerView extends BorderPane {
    private final TaskService taskService;
    private final ObservableList<StudyTask> tasks;
    private final FilteredList<StudyTask> filteredTasks;
    private final TableView<StudyTask> tableView = new TableView<>();
    private TextField titleField;
    private TextField subjectField;
    private ComboBox<StudyTask.Priority> priorityBox;
    private Spinner<Integer> minutesSpinner;
    private ComboBox<String> statusFilter;
    private ComboBox<String> priorityFilter;
    private Label statusLabel;

    public TaskPlannerView(TaskService taskService) {
        this.taskService = taskService;
        this.tasks = FXCollections.observableArrayList(taskService.loadTasks());
        this.filteredTasks = new FilteredList<>(tasks, task -> true);
        buildUi();
    }

    private void buildUi() {
        setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        VBox content = new VBox(20);
        content.setPadding(new Insets(26));
        content.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = UiFactory.title("Task Planner");
        Label subtitle = UiFactory.muted("Create study tasks, filter them by status or priority, and keep the plan saved in CSV.");

        VBox form = UiFactory.card(14);
        form.getChildren().add(UiFactory.sectionTitle("Add Study Task"));
        HBox fields = new HBox(12);
        fields.setAlignment(Pos.BOTTOM_LEFT);

        titleField = new TextField();
        titleField.setPromptText("Task title");
        UiFactory.styleTextInput(titleField);
        subjectField = new TextField();
        subjectField.setPromptText("Subject");
        UiFactory.styleTextInput(subjectField);

        priorityBox = new ComboBox<>();
        priorityBox.getItems().setAll(StudyTask.Priority.values());
        priorityBox.getSelectionModel().select(StudyTask.Priority.MEDIUM);
        UiFactory.styleCombo(priorityBox);

        minutesSpinner = new Spinner<>(5, 300, 25, 5);
        minutesSpinner.setEditable(true);
        UiFactory.styleSpinner(minutesSpinner);

        Button addButton = UiFactory.primaryButton("Add Task");
        addButton.setOnAction(event -> addTask());

        fields.getChildren().addAll(
                labeled("Title", titleField),
                labeled("Subject", subjectField),
                labeled("Priority", priorityBox),
                labeled("Est. minutes", minutesSpinner),
                addButton
        );
        HBox.setHgrow(fields.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(fields.getChildren().get(1), Priority.ALWAYS);

        statusLabel = UiFactory.muted("");
        form.getChildren().addAll(fields, statusLabel);

        HBox filters = new HBox(12);
        filters.setAlignment(Pos.CENTER_LEFT);
        statusFilter = new ComboBox<>();
        statusFilter.getItems().add("ALL");
        for (StudyTask.Status status : StudyTask.Status.values()) {
            statusFilter.getItems().add(status.name());
        }
        statusFilter.getSelectionModel().select("ALL");
        UiFactory.styleCombo(statusFilter);

        priorityFilter = new ComboBox<>();
        priorityFilter.getItems().add("ALL");
        for (StudyTask.Priority priority : StudyTask.Priority.values()) {
            priorityFilter.getItems().add(priority.name());
        }
        priorityFilter.getSelectionModel().select("ALL");
        UiFactory.styleCombo(priorityFilter);
        statusFilter.setOnAction(event -> updateFilters());
        priorityFilter.setOnAction(event -> updateFilters());
        filters.getChildren().addAll(UiFactory.muted("Status"), statusFilter, UiFactory.muted("Priority"), priorityFilter);

        buildTable();

        HBox actions = new HBox(10);
        Button inProgressButton = UiFactory.secondaryButton("Mark In Progress");
        inProgressButton.setOnAction(event -> updateSelectedStatus(StudyTask.Status.IN_PROGRESS));
        Button completeButton = UiFactory.primaryButton("Mark Complete");
        completeButton.setOnAction(event -> updateSelectedStatus(StudyTask.Status.COMPLETED));
        Button deleteButton = UiFactory.dangerButton("Delete Task");
        deleteButton.setOnAction(event -> deleteSelectedTask());
        actions.getChildren().addAll(inProgressButton, completeButton, deleteButton);

        content.getChildren().addAll(title, subtitle, form, filters, tableView, actions);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        setCenter(scrollPane);
    }

    private void buildTable() {
        TableColumn<StudyTask, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cell -> cell.getValue().titleProperty());
        TableColumn<StudyTask, String> subjectColumn = new TableColumn<>("Subject");
        subjectColumn.setCellValueFactory(cell -> cell.getValue().subjectProperty());
        TableColumn<StudyTask, String> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getPriority().name()));
        TableColumn<StudyTask, Number> minutesColumn = new TableColumn<>("Minutes");
        minutesColumn.setCellValueFactory(cell -> cell.getValue().estimatedMinutesProperty());
        TableColumn<StudyTask, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getStatus().name()));

        tableView.getColumns().clear();
        tableView.getColumns().add(titleColumn);
        tableView.getColumns().add(subjectColumn);
        tableView.getColumns().add(priorityColumn);
        tableView.getColumns().add(minutesColumn);
        tableView.getColumns().add(statusColumn);
        tableView.setItems(filteredTasks);
        tableView.setPrefHeight(420);
        UiFactory.styleTable(tableView);
    }

    private VBox labeled(String labelText, javafx.scene.Node control) {
        VBox box = new VBox(6);
        box.setMinWidth(140);
        box.getChildren().addAll(UiFactory.muted(labelText), control);
        UiFactory.stretch(control);
        return box;
    }

    private void addTask() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String subject = subjectField.getText() == null ? "" : subjectField.getText().trim();
        if (title.isEmpty()) {
            statusLabel.setText("Enter a task title first.");
            return;
        }
        StudyTask task = new StudyTask(title, subject, priorityBox.getValue(), minutesSpinner.getValue());
        tasks.add(0, task);
        saveTasks();
        titleField.clear();
        subjectField.clear();
        priorityBox.getSelectionModel().select(StudyTask.Priority.MEDIUM);
        minutesSpinner.getValueFactory().setValue(25);
        statusLabel.setText("Task saved.");
    }

    private void updateSelectedStatus(StudyTask.Status status) {
        StudyTask selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a task first.");
            return;
        }
        selected.setStatus(status);
        tableView.refresh();
        saveTasks();
        statusLabel.setText("Task updated.");
    }

    private void deleteSelectedTask() {
        StudyTask selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a task first.");
            return;
        }
        tasks.remove(selected);
        saveTasks();
        statusLabel.setText("Task deleted.");
    }

    private void updateFilters() {
        String status = statusFilter.getValue();
        String priority = priorityFilter.getValue();
        filteredTasks.setPredicate(task -> {
            boolean statusMatches = "ALL".equals(status) || task.getStatus().name().equals(status);
            boolean priorityMatches = "ALL".equals(priority) || task.getPriority().name().equals(priority);
            return statusMatches && priorityMatches;
        });
    }

    private void saveTasks() {
        taskService.saveTasks(tasks);
    }
}
