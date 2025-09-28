package com.example.demo;

import java.io.Serializable;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String dueDate;
    private String priority;
    private boolean completed;

    public Task(String title, String dueDate, String priority) {
        this.title = title;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
    }

    public void markAsCompleted() {
        this.completed = true;
    }

    @Override
    public String toString() {
        String status = completed ? "[Done]" : "[Not Done]";
        return "[" + priority + "] " + title + " (Due: " + dueDate + ") - " + status;
    }

    public String getTitle() { return title; }
    public String getDueDate() { return dueDate; }
    public String getPriority() { return priority; }
    public boolean isCompleted() { return completed; }
}
