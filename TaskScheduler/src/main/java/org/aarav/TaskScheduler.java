package org.aarav;

import java.time.LocalDateTime;
import java.util.List;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TaskScheduler {
    private final PriorityQueue<Task> taskQueue;
    private final TaskDAO taskDAO;
    private final TaskRunner taskRunner;
    private final ScheduledExecutorService schedulerService;
    private volatile boolean isRunning = false;


    public TaskScheduler(int threadPoolSize) {
        this.taskQueue = new PriorityQueue<>();
        this.taskDAO = new TaskDAO();
        this.taskRunner = new TaskRunner(threadPoolSize);
        this.schedulerService = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        if (isRunning) {
            System.out.println("Scheduler is already running");
            return;
        }

        try {
            // Initialize database
            JDBC.initDatabase();

            // Start task runner
            taskRunner.start();

            // Load pending tasks from database
            loadPendingTasks();

            // Start scheduler to check for tasks periodically
            schedulerService.scheduleAtFixedRate(this::processTasks, 0, 1, TimeUnit.SECONDS);

            isRunning = true;
            System.out.println("Task Scheduler started");
        } catch (Exception e) {
            System.err.println("Failed to start scheduler: " + e.getMessage());
        }
    }

    public void stop() {
        if (!isRunning) {
            System.out.println("Scheduler is not running");
            return;
        }

        isRunning = false;

        // Stop scheduler service
        schedulerService.shutdown();
        try {
            if (!schedulerService.awaitTermination(5, TimeUnit.SECONDS)) {
                schedulerService.shutdownNow();
            }
        } catch (InterruptedException e) {
            schedulerService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // Stop task runner
        taskRunner.stop(10);

        // Close database connection
        JDBC.closeConnection();

        System.out.println("Task Scheduler stopped");
    }


    public boolean addTask(Task task) {
        boolean savedToDb = taskDAO.saveTask(task);
        if (savedToDb) {
            synchronized (taskQueue) {
                taskQueue.offer(task);
            }
            System.out.println("Task added: " + task.getName());
            return true;
        }
        return false;
    }

    public Task getTask(UUID id) {
        return taskDAO.getTaskById(id).orElse(null);
    }


    public boolean cancelTask(UUID id) {
        synchronized (taskQueue) {
            // Remove from queue if present
            taskQueue.removeIf(task -> task.getId().equals(id));
        }

        // Delete from database
        return taskDAO.deleteTask(id);
    }


    private void processTasks() {
        try {
            // Check for new tasks in database that aren't in our queue
            loadPendingTasks();

            synchronized (taskQueue) {
                // Process due tasks
                while (!taskQueue.isEmpty()) {
                    Task nextTask = taskQueue.peek();

                    // If task is scheduled for the future, stop processing
                    if (nextTask.getScheduledFor() != null &&
                            nextTask.getScheduledFor().isAfter(LocalDateTime.now())) {
                        break;
                    }

                    // Remove from queue and submit to runner
                    taskQueue.poll();
                    taskRunner.submitTask(nextTask);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in processTasks: " + e.getMessage());
        }
    }

    private void loadPendingTasks() {
        List<Task> pendingTasks = taskDAO.getPendingTasks();
        synchronized (taskQueue) {
            for (Task task : pendingTasks) {
                // Only add if not already in queue
                if (taskQueue.stream().noneMatch(t -> t.getId().equals(task.getId()))) {
                    taskQueue.offer(task);
                }
            }
        }
    }


    public List<Task> getPendingTasks() {
        return taskDAO.getPendingTasks();
    }

    public List<Task> getInProgressTasks() {
        return taskDAO.getInProgressTasks();
    }


    public List<Task> getCompletedTasks() {
        return taskDAO.getCompletedTasks();
    }

    public List<Task> getFailedTasks() {
        return taskDAO.getFailedTasks();
    }

    /**
     * Checks if the scheduler is currently running.
     *
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
}