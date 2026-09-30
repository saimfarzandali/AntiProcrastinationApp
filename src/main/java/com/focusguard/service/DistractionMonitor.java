package com.focusguard.service;

import javafx.application.Platform;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DistractionMonitor {
    private final List<String> blacklist;
    private final Consumer<String> distractionHandler;
    private final Map<String, Long> recentWarnings = new HashMap<>();
    private ScheduledExecutorService executorService;

    public DistractionMonitor(List<String> blacklist, Consumer<String> distractionHandler) {
        this.blacklist = blacklist;
        this.distractionHandler = distractionHandler;
    }

    public void start() {
        stop();
        executorService = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "focusguard-distraction-monitor");
            thread.setDaemon(true);
            return thread;
        });
        executorService.scheduleAtFixedRate(this::scanProcessesSafely, 3, 8, TimeUnit.SECONDS);
    }

    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    private void scanProcessesSafely() {
        if (blacklist == null || blacklist.isEmpty()) {
            return;
        }
        try {
            Optional<String> match = ProcessHandle.allProcesses()
                    .map(this::readProcessName)
                    .filter(name -> !name.isEmpty())
                    .filter(this::matchesBlacklist)
                    .findFirst();
            match.ifPresent(this::warnIfAllowed);
        } catch (RuntimeException ignored) {
            // Process inspection is best-effort and must never crash the app.
        }
    }

    private String readProcessName(ProcessHandle processHandle) {
        try {
            String command = processHandle.info().command().orElse("");
            if (!command.isEmpty()) {
                return command.toLowerCase(Locale.ROOT);
            }
            return processHandle.info().commandLine().orElse("").toLowerCase(Locale.ROOT);
        } catch (RuntimeException ex) {
            return "";
        }
    }

    private boolean matchesBlacklist(String processName) {
        for (String keyword : blacklist) {
            if (keyword != null && !keyword.isBlank() && processName.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void warnIfAllowed(String processName) {
        long now = System.currentTimeMillis();
        Long previous = recentWarnings.get(processName);
        if (previous != null && now - previous < 30_000) {
            return;
        }
        recentWarnings.put(processName, now);
        Platform.runLater(() -> distractionHandler.accept(trimProcessName(processName)));
    }

    private String trimProcessName(String processName) {
        int slash = Math.max(processName.lastIndexOf('/'), processName.lastIndexOf('\\'));
        if (slash >= 0 && slash + 1 < processName.length()) {
            return processName.substring(slash + 1);
        }
        return processName;
    }
}
