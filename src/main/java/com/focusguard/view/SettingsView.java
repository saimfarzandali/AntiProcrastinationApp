package com.focusguard.view;

import com.focusguard.model.UserSettings;
import com.focusguard.service.SettingsService;
import com.focusguard.util.UiFactory;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;

public class SettingsView extends BorderPane {
    private final SettingsService settingsService;
    private final UserSettings settings;
    private Spinner<Integer> idleSpinner;
    private CheckBox strictModeBox;
    private TextArea blacklistArea;
    private ComboBox<Integer> defaultDurationBox;
    private Spinner<Integer> breakSpinner;
    private Label statusLabel;

    public SettingsView(SettingsService settingsService, UserSettings settings) {
        this.settingsService = settingsService;
        this.settings = settings;
        buildUi();
    }

    private void buildUi() {
        setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox content = new VBox(20);
        content.setPadding(new Insets(26));
        content.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = UiFactory.title("Settings");
        Label subtitle = UiFactory.muted("Adjust monitoring, strict warning behavior, default session duration, and blacklisted process keywords.");

        VBox card = UiFactory.card(16);
        card.getChildren().add(UiFactory.sectionTitle("Monitoring Preferences"));

        idleSpinner = new Spinner<>(10, 3600, settings.getIdleTimeoutSeconds(), 5);
        idleSpinner.setEditable(true);
        UiFactory.styleSpinner(idleSpinner);

        strictModeBox = new CheckBox("Enable strict warning overlay");
        strictModeBox.setSelected(settings.isStrictMode());
        strictModeBox.setTextFill(UiFactory.TEXT);
        strictModeBox.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 14));

        defaultDurationBox = new ComboBox<>();
        defaultDurationBox.getItems().addAll(15, 25, 45, 60);
        if (defaultDurationBox.getItems().contains(settings.getDefaultSessionDuration())) {
            defaultDurationBox.getSelectionModel().select(Integer.valueOf(settings.getDefaultSessionDuration()));
        } else {
            defaultDurationBox.getSelectionModel().select(Integer.valueOf(25));
        }
        UiFactory.styleCombo(defaultDurationBox);

        breakSpinner = new Spinner<>(1, 60, settings.getBreakReminderMinutes(), 1);
        breakSpinner.setEditable(true);
        UiFactory.styleSpinner(breakSpinner);

        blacklistArea = new TextArea(String.join("\n", settings.getBlacklistedKeywords()));
        blacklistArea.setPrefRowCount(8);
        blacklistArea.setWrapText(true);
        UiFactory.styleTextInput(blacklistArea);

        Button saveButton = UiFactory.primaryButton("Save Settings");
        saveButton.setOnAction(event -> saveSettings());
        statusLabel = UiFactory.muted("");

        card.getChildren().addAll(
                field("Idle timeout seconds", idleSpinner),
                strictModeBox,
                field("Default session duration", defaultDurationBox),
                field("Break reminder minutes", breakSpinner),
                field("Blacklisted app keywords", blacklistArea),
                saveButton,
                statusLabel
        );

        content.getChildren().addAll(title, subtitle, card);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        setCenter(scrollPane);
    }

    private VBox field(String labelText, javafx.scene.Node control) {
        VBox box = new VBox(7);
        box.getChildren().addAll(UiFactory.muted(labelText), control);
        UiFactory.stretch(control);
        return box;
    }

    private void saveSettings() {
        settings.setIdleTimeoutSeconds(idleSpinner.getValue());
        settings.setStrictMode(strictModeBox.isSelected());
        settings.setDefaultSessionDuration(defaultDurationBox.getValue() == null ? 25 : defaultDurationBox.getValue());
        settings.setBreakReminderMinutes(breakSpinner.getValue());
        settings.setBlacklistedKeywords(SettingsService.parseKeywords(blacklistArea.getText()));
        settingsService.saveSettings(settings);
        statusLabel.setText("Settings saved. New sessions will use the updated values.");
    }
}
