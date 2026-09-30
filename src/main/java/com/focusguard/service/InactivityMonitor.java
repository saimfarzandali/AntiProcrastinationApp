package com.focusguard.service;

import javafx.application.Platform;

import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InactivityMonitor {
    private final int idleLimitSeconds;
    private final Runnable inactivityHandler;
    private ScheduledExecutorService executorService;
    private Point lastPoint;
    private int unchangedSeconds;

    public InactivityMonitor(int idleLimitSeconds, Runnable inactivityHandler) {
        this.idleLimitSeconds = Math.max(10, idleLimitSeconds);
        this.inactivityHandler = inactivityHandler;
    }

    public void start() {
        stop();
        unchangedSeconds = 0;
        lastPoint = readMousePoint();
        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "focusguard-inactivity-monitor");
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleAtFixedRate(this::checkActivity, 5, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    private void checkActivity() {
        Point current = readMousePoint();
        if (current == null) {
            return;
        }
        if (lastPoint != null && current.equals(lastPoint)) {
            unchangedSeconds += 5;
        } else {
            unchangedSeconds = 0;
            lastPoint = current;
        }

        if (unchangedSeconds >= idleLimitSeconds) {
            unchangedSeconds = 0;
            Platform.runLater(inactivityHandler);
        }
    }

    private Point readMousePoint() {
        try {
            return MouseInfo.getPointerInfo() == null ? null : MouseInfo.getPointerInfo().getLocation();
        } catch (HeadlessException | SecurityException ex) {
            return null;
        }
    }
}
