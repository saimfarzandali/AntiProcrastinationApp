package com.focusguard.service;

import com.focusguard.model.UserSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class SettingsService {
    private final FileStorageService storageService;

    public SettingsService(FileStorageService storageService) {
        this.storageService = storageService;
    }

    public UserSettings loadSettings() {
        UserSettings settings = new UserSettings();
        if (java.nio.file.Files.notExists(storageService.settingsPath())) {
            saveSettings(settings);
            return settings;
        }

        Properties properties = new Properties();
        try (InputStream input = java.nio.file.Files.newInputStream(storageService.settingsPath())) {
            properties.load(input);
            settings.setIdleTimeoutSeconds(parseInt(properties.getProperty("idleTimeoutSeconds"), settings.getIdleTimeoutSeconds()));
            settings.setStrictMode(Boolean.parseBoolean(properties.getProperty("strictMode", Boolean.toString(settings.isStrictMode()))));
            settings.setDefaultSessionDuration(parseInt(properties.getProperty("defaultSessionDuration"), settings.getDefaultSessionDuration()));
            settings.setBreakReminderMinutes(parseInt(properties.getProperty("breakReminderMinutes"), settings.getBreakReminderMinutes()));
            settings.setBlacklistedKeywords(parseKeywords(properties.getProperty("blacklist", String.join(",", settings.getBlacklistedKeywords()))));
        } catch (IOException ex) {
            saveSettings(settings);
        }
        return settings;
    }

    public void saveSettings(UserSettings settings) {
        Properties properties = new Properties();
        properties.setProperty("idleTimeoutSeconds", Integer.toString(settings.getIdleTimeoutSeconds()));
        properties.setProperty("strictMode", Boolean.toString(settings.isStrictMode()));
        properties.setProperty("defaultSessionDuration", Integer.toString(settings.getDefaultSessionDuration()));
        properties.setProperty("breakReminderMinutes", Integer.toString(settings.getBreakReminderMinutes()));
        properties.setProperty("blacklist", String.join(",", settings.getBlacklistedKeywords()));
        try {
            java.nio.file.Files.createDirectories(storageService.getDataDirectory());
            try (OutputStream output = java.nio.file.Files.newOutputStream(storageService.settingsPath())) {
                properties.store(output, "FocusGuard settings");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to save settings", ex);
        }
    }

    public static List<String> parseKeywords(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String normalized = raw.replace("\n", ",").replace("\r", ",");
        List<String> keywords = new ArrayList<>();
        Arrays.stream(normalized.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(text -> !text.isEmpty())
                .forEach(text -> {
                    if (!keywords.contains(text)) {
                        keywords.add(text);
                    }
                });
        return keywords;
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
