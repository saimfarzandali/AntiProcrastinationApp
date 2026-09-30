package com.focusguard.view;

import com.focusguard.model.ProductivityStats;
import com.focusguard.service.AnalyticsService;
import com.focusguard.util.UiFactory;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class AnalyticsView extends BorderPane {
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEE");

    public AnalyticsView(AnalyticsService analyticsService) {
        setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        ProductivityStats stats = analyticsService.buildStats();

        VBox content = new VBox(20);
        content.setPadding(new Insets(26));
        content.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));

        Label title = UiFactory.title("Analytics");
        Label subtitle = UiFactory.muted("Review focus minutes, distraction impact, inactivity, and session scores.");

        HBox statCards = new HBox(14);
        statCards.getChildren().addAll(
                UiFactory.metricCard("Best session score", Integer.toString(stats.getBestScore()), UiFactory.GREEN),
                UiFactory.metricCard("Average score", String.format("%.1f", stats.getAverageScore()), UiFactory.PRIMARY),
                UiFactory.metricCard("Total distractions", Integer.toString(stats.getTotalDistractions()), UiFactory.ORANGE),
                UiFactory.metricCard("Inactive minutes", Integer.toString(stats.getTotalInactiveMinutes()), UiFactory.RED)
        );
        for (javafx.scene.Node node : statCards.getChildren()) {
            HBox.setHgrow(node, Priority.ALWAYS);
            UiFactory.stretch(node);
        }

        HBox charts = new HBox(18);
        VBox barCard = UiFactory.card(12);
        barCard.getChildren().addAll(UiFactory.sectionTitle("Focus Minutes By Day"), createBarChart(stats));
        VBox pieCard = UiFactory.card(12);
        pieCard.getChildren().addAll(UiFactory.sectionTitle("Productive vs Interrupted Time"), createPieChart(stats));
        charts.getChildren().addAll(barCard, pieCard);
        HBox.setHgrow(barCard, Priority.ALWAYS);
        HBox.setHgrow(pieCard, Priority.ALWAYS);
        UiFactory.stretch(barCard);
        UiFactory.stretch(pieCard);

        content.getChildren().addAll(title, subtitle, statCards, charts);
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(new Background(new BackgroundFill(UiFactory.APP_BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        setCenter(scrollPane);
    }

    private BarChart<String, Number> createBarChart(ProductivityStats stats) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Day");
        yAxis.setLabel("Minutes");
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(330);
        chart.setBackground(new Background(new BackgroundFill(UiFactory.SURFACE, CornerRadii.EMPTY, Insets.EMPTY)));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        stats.getFocusMinutesByDay().forEach((day, minutes) -> series.getData().add(new XYChart.Data<>(day.format(DAY_FORMAT), minutes)));
        chart.getData().add(series);
        return chart;
    }

    private PieChart createPieChart(ProductivityStats stats) {
        PieChart chart = new PieChart();
        int distractionMinutes = stats.getTotalDistractions();
        int inactiveMinutes = stats.getTotalInactiveMinutes();
        int productiveMinutes = Math.max(0, stats.getTotalFocusMinutes() - inactiveMinutes - distractionMinutes);
        if (productiveMinutes + inactiveMinutes + distractionMinutes == 0) {
            chart.getData().add(new PieChart.Data("No data", 1));
        } else {
            chart.getData().add(new PieChart.Data("Productive", productiveMinutes));
            chart.getData().add(new PieChart.Data("Inactive", inactiveMinutes));
            chart.getData().add(new PieChart.Data("Distraction", distractionMinutes));
        }
        chart.setPrefHeight(330);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setBackground(new Background(new BackgroundFill(UiFactory.SURFACE, CornerRadii.EMPTY, Insets.EMPTY)));
        return chart;
    }
}
