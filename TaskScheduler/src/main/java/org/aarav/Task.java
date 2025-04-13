package org.aarav;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a task in the scheduler with priority, description, and status tracking.
 */
public class Task implements Comparable<Task> {
    private final UUID id;
    private String name;
    private String description;
    private int priority;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledFor;
    private LocalDateTime completedAt;
    private TaskStatus status;

    public enum TaskStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    // Constructor for new tasks
    public Task(String name, String description, int priority, LocalDateTime scheduledFor) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.scheduledFor = scheduledFor;
        this.status = TaskStatus.PENDING;
    }

    // Constructor for loading tasks from database
    public Task(UUID id, String name, String description, int priority,
                LocalDateTime createdAt, LocalDateTime scheduledFor,
                LocalDateTime completedAt, TaskStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.createdAt = createdAt;
        this.scheduledFor = scheduledFor;
        this.completedAt = completedAt;
        this.status = status;
    }

    @Override
    public int compareTo(Task other) {
        // First compare by scheduled time for tasks that need to run at specific times
        if (this.scheduledFor != null && other.scheduledFor != null) {
            int timeComparison = this.scheduledFor.compareTo(other.scheduledFor);
            if (timeComparison != 0) {
                return timeComparison;
            }
        } else if (this.scheduledFor != null) {
            return -1; // This task has a schedule, other doesn't
        } else if (other.scheduledFor != null) {
            return 1; // Other task has a schedule, this doesn't
        }

        // If scheduled times are equal or null, compare by priority (higher priority comes first)
        return Integer.compare(other.priority, this.priority);
    }

    public void markInProgress() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void markCompleted() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = TaskStatus.FAILED;
        this.completedAt = LocalDateTime.now();
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public TaskStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", priority=" + priority +
                ", scheduledFor=" + scheduledFor +
                ", status=" + status +
                '}';
    }
}