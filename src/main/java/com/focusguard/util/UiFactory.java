package com.focusguard.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public final class UiFactory {
    public static final Color APP_BACKGROUND = Color.web("#F3F6F8");
    public static final Color SURFACE = Color.web("#FFFFFF");
    public static final Color SURFACE_ALT = Color.web("#EEF4F2");
    public static final Color TEXT = Color.web("#17212B");
    public static final Color MUTED = Color.web("#637083");
    public static final Color PRIMARY = Color.web("#2563EB");
    public static final Color PRIMARY_DARK = Color.web("#1D4ED8");
    public static final Color TEAL = Color.web("#0F9F8F");
    public static final Color GREEN = Color.web("#16A34A");
    public static final Color ORANGE = Color.web("#F59E0B");
    public static final Color RED = Color.web("#DC2626");
    public static final Color BORDER = Color.web("#D8E0E7");
    public static final Color DARK = Color.web("#111827");

    private UiFactory() {
    }

    public static Label title(String text) {
        Label label = new Label(text);
        label.setTextFill(TEXT);
        label.setFont(Font.font("System", FontWeight.BOLD, 30));
        return label;
    }

    public static Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setTextFill(TEXT);
        label.setFont(Font.font("System", FontWeight.BOLD, 19));
        return label;
    }

    public static Label body(String text) {
        Label label = new Label(text);
        label.setTextFill(TEXT);
        label.setFont(Font.font("System", 14));
        label.setWrapText(true);
        return label;
    }

    public static Label muted(String text) {
        Label label = new Label(text);
        label.setTextFill(MUTED);
        label.setFont(Font.font("System", 13));
        label.setWrapText(true);
        return label;
    }

    public static Label metricValue(String text) {
        Label label = new Label(text);
        label.setTextFill(TEXT);
        label.setFont(Font.font("System", FontWeight.BOLD, 28));
        return label;
    }

    public static Label badge(String text, Color fill, Color textColor) {
        Label label = new Label(text);
        label.setTextFill(textColor);
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        label.setPadding(new Insets(6, 10, 6, 10));
        label.setBackground(new Background(new BackgroundFill(fill, new CornerRadii(999), Insets.EMPTY)));
        return label;
    }

    public static VBox card(double spacing) {
        VBox box = new VBox(spacing);
        box.setPadding(new Insets(18));
        box.setBackground(new Background(new BackgroundFill(SURFACE, new CornerRadii(8), Insets.EMPTY)));
        box.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
        box.setEffect(cardShadow());
        return box;
    }

    public static VBox metricCard(String title, String value, Color accent) {
        VBox box = card(8);
        Label titleLabel = muted(title);
        Label valueLabel = metricValue(value);
        Region accentLine = new Region();
        accentLine.setPrefHeight(3);
        accentLine.setMaxWidth(Double.MAX_VALUE);
        accentLine.setBackground(new Background(new BackgroundFill(accent, new CornerRadii(999), Insets.EMPTY)));
        box.getChildren().addAll(titleLabel, valueLabel, accentLine);
        VBox.setVgrow(accentLine, Priority.NEVER);
        return box;
    }

    public static Button actionCard(String title, String subtitle, Color accent) {
        Button button = new Button();
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(104);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 17));
        titleLabel.setTextFill(TEXT);
        Label subtitleLabel = muted(subtitle);
        Region accentLine = new Region();
        accentLine.setPrefHeight(3);
        accentLine.setMaxWidth(Double.MAX_VALUE);
        accentLine.setBackground(new Background(new BackgroundFill(accent, new CornerRadii(999), Insets.EMPTY)));
        VBox graphic = new VBox(8, titleLabel, subtitleLabel, accentLine);
        graphic.setAlignment(Pos.CENTER_LEFT);
        graphic.setPadding(new Insets(4));
        button.setGraphic(graphic);
        styleButton(button, SURFACE, TEXT, BORDER, Color.web("#F7FAFC"));
        button.setEffect(cardShadow());
        return button;
    }

    public static Button primaryButton(String text) {
        Button button = new Button(text);
        styleButton(button, PRIMARY, Color.WHITE, PRIMARY, PRIMARY_DARK);
        return button;
    }

    public static Button secondaryButton(String text) {
        Button button = new Button(text);
        styleButton(button, SURFACE, TEXT, BORDER, Color.web("#F7FAFC"));
        return button;
    }

    public static Button dangerButton(String text) {
        Button button = new Button(text);
        styleButton(button, RED, Color.WHITE, RED, Color.web("#B91C1C"));
        return button;
    }

    public static Button navButton(String text) {
        Button button = new Button(text);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setFont(Font.font("System", FontWeight.BOLD, 13));
        button.setTextFill(Color.web("#D8E0E7"));
        button.setPadding(new Insets(10, 14, 10, 14));
        button.setMinHeight(38);
        button.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(8), Insets.EMPTY)));
        button.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
        button.getProperties().put("active", false);
        button.setOnMouseEntered(event -> {
            if (!Boolean.TRUE.equals(button.getProperties().get("active"))) {
                button.setBackground(new Background(new BackgroundFill(Color.web("#1F2937"), new CornerRadii(8), Insets.EMPTY)));
            }
        });
        button.setOnMouseExited(event -> {
            if (!Boolean.TRUE.equals(button.getProperties().get("active"))) {
                button.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(8), Insets.EMPTY)));
            }
        });
        return button;
    }

    public static void markNavButtonActive(Button button, boolean active) {
        button.getProperties().put("active", active);
        if (active) {
            button.setBackground(new Background(new BackgroundFill(Color.web("#263244"), new CornerRadii(8), Insets.EMPTY)));
            button.setTextFill(Color.WHITE);
            button.setBorder(new Border(new BorderStroke(Color.web("#344155"), BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
        } else {
            button.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(8), Insets.EMPTY)));
            button.setTextFill(Color.web("#D8E0E7"));
            button.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
        }
    }

    public static void styleTextInput(TextInputControl control) {
        control.setFont(Font.font("System", 14));
        control.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(7), Insets.EMPTY)));
        control.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID, new CornerRadii(7), new BorderWidths(1))));
        control.setPadding(new Insets(9, 10, 9, 10));
    }

    public static void styleCombo(ComboBox<?> comboBox) {
        comboBox.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(7), Insets.EMPTY)));
        comboBox.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID, new CornerRadii(7), new BorderWidths(1))));
        comboBox.setMinHeight(38);
    }

    public static void styleSpinner(Spinner<?> spinner) {
        spinner.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(7), Insets.EMPTY)));
        spinner.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID, new CornerRadii(7), new BorderWidths(1))));
        spinner.setMinHeight(38);
    }

    public static void styleTable(TableView<?> tableView) {
        tableView.setBackground(new Background(new BackgroundFill(SURFACE, new CornerRadii(8), Insets.EMPTY)));
        tableView.setBorder(new Border(new BorderStroke(BORDER, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(1))));
    }

    public static void stretch(Node node) {
        if (node instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
    }

    public static DropShadow cardShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(16);
        shadow.setOffsetY(4);
        shadow.setColor(Color.rgb(31, 41, 55, 0.08));
        return shadow;
    }

    private static void styleButton(Button button, Color background, Color text, Color border, Color hover) {
        button.setFont(Font.font("System", FontWeight.BOLD, 13));
        button.setTextFill(text);
        button.setPadding(new Insets(10, 14, 10, 14));
        button.setMinHeight(38);
        button.setBackground(new Background(new BackgroundFill(background, new CornerRadii(7), Insets.EMPTY)));
        button.setBorder(new Border(new BorderStroke(border, BorderStrokeStyle.SOLID, new CornerRadii(7), new BorderWidths(1))));
        button.setOnMouseEntered(event -> button.setBackground(new Background(new BackgroundFill(hover, new CornerRadii(7), Insets.EMPTY))));
        button.setOnMouseExited(event -> button.setBackground(new Background(new BackgroundFill(background, new CornerRadii(7), Insets.EMPTY))));
    }
}
