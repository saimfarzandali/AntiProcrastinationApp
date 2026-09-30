package com.focusguard.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public class StudyTask {
    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    public enum Status {
        PENDING, IN_PROGRESS, COMPLETED
    }

    private final String id;
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty subject = new SimpleStringProperty();
    private final ObjectProperty<Priority> priority = new SimpleObjectProperty<>();
    private final IntegerProperty estimatedMinutes = new SimpleIntegerProperty();
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>();
    private final LocalDateTime createdAt;

    public StudyTask(String title, String subject, Priority priority, int estimatedMinutes) {
        this(UUID.randomUUID().toString(), title, subject, priority, estimatedMinutes, Status.PENDING, LocalDateTime.now());
    }

    public StudyTask(String id, String title, String subject, Priority priority, int estimatedMinutes,
                     Status status, LocalDateTime createdAt) {
        this.id = id;
        setTitle(title);
        setSubject(subject);
        setPriority(priority);
        setEstimatedMinutes(estimatedMinutes);
        setStatus(status);
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title == null ? "" : title);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getSubject() {
        return subject.get();
    }

    public void setSubject(String subject) {
        this.subject.set(subject == null ? "" : subject);
    }

    public StringProperty subjectProperty() {
        return subject;
    }

    public Priority getPriority() {
        return priority.get();
    }

    public void setPriority(Priority priority) {
        this.priority.set(priority == null ? Priority.MEDIUM : priority);
    }

    public ObjectProperty<Priority> priorityProperty() {
        return priority;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes.get();
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes.set(Math.max(1, estimatedMinutes));
    }

    public IntegerProperty estimatedMinutesProperty() {
        return estimatedMinutes;
    }

    public Status getStatus() {
        return status.get();
    }

    public void setStatus(Status status) {
        this.status.set(status == null ? Status.PENDING : status);
    }

    public ObjectProperty<Status> statusProperty() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
