package com.focusguard.view;

import com.focusguard.util.UiFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class FocusWarningOverlay extends Stage {
    private int remainingSeconds;
    private final Button returnButton;
    private final Label countdownLabel;
    private final TextArea commitmentArea;

    public FocusWarningOverlay(Stage owner, String appName, int cooldownSeconds) {
        this.remainingSeconds = Math.max(1, cooldownSeconds);
        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.APPLICATION_MODAL);
        setTitle("FocusGuard Warning");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(34));
        root.setBackground(new Background(new BackgroundFill(UiFactory.DARK, CornerRadii.EMPTY, Insets.EMPTY)));

        VBox panel = UiFactory.card(18);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(640);
        panel.setBackground(new Background(new BackgroundFill(Color.web("#F8FAFC"), new CornerRadii(8), Insets.EMPTY)));

        Label alert = new Label("Distraction detected. Take control and return to your goal.");
        alert.setTextFill(UiFactory.RED);
        alert.setFont(Font.font("System", FontWeight.BOLD, 28));
        alert.setWrapText(true);
        alert.setAlignment(Pos.CENTER);

        Label detected = UiFactory.body("Detected app: " + appName);
        detected.setFont(Font.font("System", FontWeight.BOLD, 16));
        detected.setAlignment(Pos.CENTER);

        countdownLabel = UiFactory.metricValue(Integer.toString(remainingSeconds));
        Label countdownHint = UiFactory.muted("Wait for the cooldown, then write a short commitment line.");
        countdownHint.setAlignment(Pos.CENTER);

        commitmentArea = new TextArea();
        commitmentArea.setPromptText("What should you be studying right now?");
        commitmentArea.setPrefRowCount(3);
        commitmentArea.setWrapText(true);
        UiFactory.styleTextInput(commitmentArea);

        returnButton = UiFactory.primaryButton("Return to Focus");
        returnButton.setDisable(true);
        returnButton.setOnAction(event -> close());

        panel.getChildren().addAll(alert, detected, countdownLabel, countdownHint, commitmentArea, returnButton);
        root.setCenter(panel);
        BorderPane.setAlignment(panel, Pos.CENTER);

        Scene scene = new Scene(root, 900, 620);
        setScene(scene);
        setFullScreen(true);
        setFullScreenExitHint("FocusGuard strict mode warning");
        startCountdown();
    }

    private void startCountdown() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            remainingSeconds--;
            countdownLabel.setText(Integer.toString(Math.max(0, remainingSeconds)));
            updateButtonState();
        }));
        timeline.setCycleCount(remainingSeconds);
        commitmentArea.textProperty().addListener((observable, oldValue, newValue) -> updateButtonState());
        timeline.setOnFinished(event -> updateButtonState());
        timeline.play();
    }

    private void updateButtonState() {
        boolean hasCommitment = commitmentArea.getText() != null && !commitmentArea.getText().trim().isEmpty();
        returnButton.setDisable(remainingSeconds > 0 || !hasCommitment);
    }
}
