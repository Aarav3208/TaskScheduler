package org.aarav;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskRunner {
    private final ExecutorService executorService;
    private final TaskDAO taskDAO;
    private volatile boolean isRunning = false;

    public TaskRunner(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.taskDAO = new TaskDAO();
    }

    public void submitTask(Task task) {
        if (!isRunning) {
            throw new IllegalStateException("TaskRunner is not running");
        }

        // Skip tasks already completed or failed
        if (task.getStatus() == Task.TaskStatus.COMPLETED ||
                task.getStatus() == Task.TaskStatus.FAILED) {
            return;
        }

        // Check if task is scheduled for the future
        if (task.getScheduledFor() != null && task.getScheduledFor().isAfter(LocalDateTime.now())) {
            return; // This task is scheduled for later, don't execute it yet
        }

        executorService.submit(() -> executeTask(task));
    }


    private void executeTask(Task task) {
        try {
            // Mark as in progress in database
            task.markInProgress();
            taskDAO.updateTask(task);

            System.out.println("Executing task: " + task.getName());

            // Simulate task execution
            simulateTaskExecution(task);

            // Mark as completed
            task.markCompleted();
            taskDAO.updateTask(task);

            System.out.println("Task completed: " + task.getName());
        } catch (Exception e) {
            // Handle execution failure
            System.err.println("Task execution failed: " + task.getName() + " - " + e.getMessage());
            task.markFailed();
            taskDAO.updateTask(task);
        }
    }


    private void simulateTaskExecution(Task task) throws Exception {
        // Simulate execution time based on priority
        int executionTimeMs = (11 - task.getPriority()) * 500; // Higher priority = shorter execution

        try {
            Thread.sleep(executionTimeMs);

            // Simulate random failure (10% chance)
            if (Math.random() < 0.1) {
                throw new Exception("Random task failure");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Exception("Task interrupted", e);
        }
    }

    public void start() {
        isRunning = true;
        System.out.println("TaskRunner started with thread pool");
    }


    public void stop(int timeoutSeconds) {
        isRunning = false;
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            System.out.println("TaskRunner stopped");
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            System.err.println("TaskRunner shutdown interrupted");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}