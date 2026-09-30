package com.focusguard.service;

import com.focusguard.model.StudyTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class TaskService {
    private static final String TASK_HEADER = "id,title,subject,priority,estimatedMinutes,status,createdAt";
    private final FileStorageService storageService;

    public TaskService(FileStorageService storageService) {
        this.storageService = storageService;
        storageService.ensureFile(storageService.tasksPath(), TASK_HEADER);
    }

    public synchronized List<StudyTask> loadTasks() {
        List<StudyTask> tasks = new ArrayList<>();
        List<String> lines = storageService.readLines(storageService.tasksPath());
        for (int index = 1; index < lines.size(); index++) {
            String line = lines.get(index);
            if (line.trim().isEmpty()) {
                continue;
            }
            List<String> parts = FileStorageService.parseCsv(line);
            if (parts.size() < 7) {
                continue;
            }
            try {
                tasks.add(new StudyTask(
                        parts.get(0),
                        parts.get(1),
                        parts.get(2),
                        StudyTask.Priority.valueOf(parts.get(3)),
                        parseInt(parts.get(4), 25),
                        StudyTask.Status.valueOf(parts.get(5)),
                        LocalDateTime.parse(parts.get(6))
                ));
            } catch (RuntimeException ignored) {
                // Skip damaged task rows without breaking the planner.
            }
        }
        tasks.sort(Comparator.comparing(StudyTask::getCreatedAt).reversed());
        return tasks;
    }

    public synchronized void saveTasks(Collection<StudyTask> tasks) {
        List<String> lines = new ArrayList<>();
        lines.add(TASK_HEADER);
        for (StudyTask task : tasks) {
            lines.add(FileStorageService.csvLine(
                    task.getId(),
                    task.getTitle(),
                    task.getSubject(),
                    task.getPriority().name(),
                    Integer.toString(task.getEstimatedMinutes()),
                    task.getStatus().name(),
                    task.getCreatedAt().toString()
            ));
        }
        storageService.writeLines(storageService.tasksPath(), lines);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
