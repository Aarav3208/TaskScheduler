# Task Scheduler 

A multithreaded **Java Task Scheduler** that allows users to create, manage, and execute tasks with time-based or priority-based scheduling. Designed for learning and experimenting with Java concurrency, task management, and clean code design.

---

##  Features

- Add tasks with name, description, priority (1-10), and optional scheduled time
- List tasks by status: Pending, In-Progress, Completed, or Failed
- View full task details via Task ID
- Cancel tasks before they are executed
- Multithreaded execution using a thread pool
- Intelligent scheduling: executes by scheduled time, then priority

---

## Tech Stack

- Java 17+
- Java Threads and Synchronization
- `UUID` for task identity
- `LocalDateTime` for time management
- Command Line Interface (CLI)
- PostgreSQL (optional, for persistent storage)

---

## Project Structure

```
org.aarav/
├── Main.java           # Entry point, handles CLI and user interaction
├── Task.java           # Task entity, status tracking, and comparison logic
├── TaskScheduler.java  # Core scheduler with queues and worker threads
```

---

##  Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/yourusername/task-scheduler-java.git
cd task-scheduler-java
```

### 2. Install PostgreSQL (Optional for DB Integration)
- Download PostgreSQL from [https://www.postgresql.org/download/](https://www.postgresql.org/download/)
- Install it and set a password for the default user `postgres`
- Create a database (e.g., `task_scheduler`)

### 3. Configure Database Connection
Update your Java code (if using JDBC) with the correct credentials:
```java
String url = "jdbc:postgresql://localhost:5432/task_scheduler";
String user = "postgres";
String password = "your_password_here";
```

### 4. Compile and Run
```bash
javac org/aarav/*.java
java org.aarav.Main
```

---

##  Sample CLI Output
```
===== TASK SCHEDULER =====
1. Add new task
2. List tasks
3. View task details
4. Cancel task
5. Exit
```

---

##  Future Improvements

- Database integration using JDBC (e.g., PostgreSQL)
- REST API with Spring Boot
- GUI with JavaFX or Swing
- Export task logs to files
- Email notifications for scheduled task completions

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

