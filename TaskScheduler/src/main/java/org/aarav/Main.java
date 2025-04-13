package org.aarav;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static TaskScheduler scheduler;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("mm:dd-MM-yyyy");

    public static void main(String[] args) {
        System.out.println("Starting Task Scheduler...");

        // Create scheduler with 4 worker threads
        scheduler = new TaskScheduler(4);
        scheduler.start();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        addTask();
                        break;
                    case "2":
                        listTasks();
                        break;
                    case "3":
                        viewTask();
                        break;
                    case "4":
                        cancelTask();
                        break;
                    case "5":
                        running = false;
                        scheduler.stop();
                        System.out.println("Task Scheduler stopped. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }

            // Add a small pause before showing the menu again
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== TASK SCHEDULER =====");
        System.out.println("1. Add new task");
        System.out.println("2. List tasks");
        System.out.println("3. View task details");
        System.out.println("4. Cancel task");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }


    private static void addTask() {
        System.out.println("\n----- ADD NEW TASK -----");

        System.out.print("Task name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Task description: ");
        String description = scanner.nextLine().trim();

        int priority = promptForInt("Priority (1-10, with 10 being highest): ", 1, 10);

        LocalDateTime scheduledFor = null;
        System.out.print("Schedule task for a specific time? (Y/N): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            scheduledFor = promptForDateTime("Enter date and time (yyyy-MM-dd HH:mm): ");
        }

        Task task = new Task(name, description, priority, scheduledFor);
        boolean added = scheduler.addTask(task);

        if (added) {
            System.out.println("Task added successfully! Task ID: " + task.getId());
        } else {
            System.out.println("Failed to add task.");
        }
    }


    private static void listTasks() {
        System.out.println("\n----- LIST TASKS -----");
        System.out.println("1. Pending tasks");
        System.out.println("2. In-progress tasks");
        System.out.println("3. Completed tasks");
        System.out.println("4. Failed tasks");
        System.out.print("Enter your choice: ");

        String choice = scanner.nextLine().trim();
        List<Task> tasks = null;

        switch (choice) {
            case "1":
                tasks = scheduler.getPendingTasks();
                System.out.println("\nPENDING TASKS:");
                break;
            case "2":
                tasks = scheduler.getInProgressTasks();
                System.out.println("\nIN-PROGRESS TASKS:");
                break;
            case "3":
                tasks = scheduler.getCompletedTasks();
                System.out.println("\nCOMPLETED TASKS:");
                break;
            case "4":
                tasks = scheduler.getFailedTasks();
                System.out.println("\nFAILED TASKS:");
                break;
            default:
                System.out.println("Invalid option.");
                return;
        }

        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        System.out.println("--------------------------------------------");
        System.out.printf("%-36s | %-20s | %s\n", "ID", "NAME", "PRIORITY");
        System.out.println("--------------------------------------------");

        for (Task task : tasks) {
            System.out.printf("%-36s | %-20s | %d\n",
                    task.getId(),
                    limitString(task.getName(), 20),
                    task.getPriority());
        }
        System.out.println("--------------------------------------------");
        System.out.println("Total: " + tasks.size() + " tasks");
    }


    private static void viewTask() {
        System.out.println("\n----- VIEW TASK DETAILS -----");
        System.out.print("Enter task ID: ");
        String idString = scanner.nextLine().trim();

        try {
            UUID id = UUID.fromString(idString);
            Task task = scheduler.getTask(id);

            if (task == null) {
                System.out.println("Task not found.");
                return;
            }

            System.out.println("\nTASK DETAILS:");
            System.out.println("ID: " + task.getId());
            System.out.println("Name: " + task.getName());
            System.out.println("Description: " + task.getDescription());
            System.out.println("Priority: " + task.getPriority());
            System.out.println("Status: " + task.getStatus());
            System.out.println("Created: " + formatDateTime(task.getCreatedAt()));

            if (task.getScheduledFor() != null) {
                System.out.println("Scheduled for: " + formatDateTime(task.getScheduledFor()));
            }

            if (task.getCompletedAt() != null) {
                System.out.println("Completed/Failed at: " + formatDateTime(task.getCompletedAt()));
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid task ID format.");
        }
    }


    private static void cancelTask() {
        System.out.println("\n----- CANCEL TASK -----");
        System.out.print("Enter task ID: ");
        String idString = scanner.nextLine().trim();

        try {
            UUID id = UUID.fromString(idString);
            boolean cancelled = scheduler.cancelTask(id);

            if (cancelled) {
                System.out.println("Task cancelled successfully.");
            } else {
                System.out.println("Failed to cancel task. Task may not exist.");
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid task ID format.");
        }
    }


    private static int promptForInt(String prompt, int min, int max) {
        int value;
        while (true) {
            System.out.print(prompt);
            try {
                value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a value between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }


    private static LocalDateTime promptForDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                return LocalDateTime.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid format. Please use yyyy-MM-dd HH:mm (e.g., 2025-04-20 15:30)");
            }
        }
    }


    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "N/A";
    }

    private static String limitString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}