package com.focusguard.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileStorageService {
    private final Path dataDirectory = Paths.get("data");

    public FileStorageService() {
        initialize();
    }

    public void initialize() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create data directory", ex);
        }
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Path sessionsPath() {
        return dataDirectory.resolve("sessions.csv");
    }

    public Path tasksPath() {
        return dataDirectory.resolve("tasks.csv");
    }

    public Path settingsPath() {
        return dataDirectory.resolve("settings.properties");
    }

    public Path eventsPath() {
        return dataDirectory.resolve("events.csv");
    }

    public Path reportPath() {
        return dataDirectory.resolve("focusguard_report.txt");
    }

    public void ensureFile(Path path, String header) {
        try {
            if (Files.notExists(path) || Files.size(path) == 0) {
                Files.write(path, Collections.singletonList(header), StandardCharsets.UTF_8);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to initialize file: " + path, ex);
        }
    }

    public List<String> readLines(Path path) {
        try {
            if (Files.notExists(path)) {
                return new ArrayList<>();
            }
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    public void writeLines(Path path, List<String> lines) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to write file: " + path, ex);
        }
    }

    public void appendLine(Path path, String line, String header) {
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path) || Files.size(path) == 0) {
                Files.write(path, Arrays.asList(header, line), StandardCharsets.UTF_8);
            } else {
                Files.write(path, Collections.singletonList(line), StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to append file: " + path, ex);
        }
    }

    public static String csvLine(String... values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values) {
            escaped.add(escape(value));
        }
        return String.join(",", escaped);
    }

    public static String escape(String value) {
        String text = value == null ? "" : value;
        boolean quote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
        text = text.replace("\"", "\"\"");
        return quote ? "\"" + text + "\"" : text;
    }

    public static List<String> parseCsv(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int index = 0; index < line.length(); index++) {
            char ch = line.charAt(index);
            if (ch == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }
}
