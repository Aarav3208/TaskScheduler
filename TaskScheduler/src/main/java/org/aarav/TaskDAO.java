package org.aarav;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class TaskDAO {

    public boolean saveTask(Task task) {
        String sql = "INSERT INTO tasks (id, name, description, priority, created_at, scheduled_for, completed_at, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, task.getId());
            pstmt.setString(2, task.getName());
            pstmt.setString(3, task.getDescription());
            pstmt.setInt(4, task.getPriority());
            pstmt.setTimestamp(5, Timestamp.valueOf(task.getCreatedAt()));
            pstmt.setTimestamp(6, task.getScheduledFor() != null ? Timestamp.valueOf(task.getScheduledFor()) : null);
            pstmt.setTimestamp(7, task.getCompletedAt() != null ? Timestamp.valueOf(task.getCompletedAt()) : null);
            pstmt.setString(8, task.getStatus().name());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error saving task: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET name = ?, description = ?, priority = ?, " +
                "scheduled_for = ?, completed_at = ?, status = ? WHERE id = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.getName());
            pstmt.setString(2, task.getDescription());
            pstmt.setInt(3, task.getPriority());
            pstmt.setTimestamp(4, task.getScheduledFor() != null ? Timestamp.valueOf(task.getScheduledFor()) : null);
            pstmt.setTimestamp(5, task.getCompletedAt() != null ? Timestamp.valueOf(task.getCompletedAt()) : null);
            pstmt.setString(6, task.getStatus().name());
            pstmt.setObject(7, task.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating task: " + e.getMessage());
            return false;
        }
    }


    public Optional<Task> getTaskById(UUID id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Task task = mapResultSetToTask(rs);
                return Optional.of(task);
            }

            return Optional.empty();
        } catch (SQLException e) {
            System.err.println("Error getting task by ID: " + e.getMessage());
            return Optional.empty();
        }
    }


    public List<Task> getPendingTasks() {
        String sql = "SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY scheduled_for ASC, priority DESC";
        return getTasksByStatus(sql);
    }


    public List<Task> getInProgressTasks() {
        String sql = "SELECT * FROM tasks WHERE status = 'IN_PROGRESS' ORDER BY scheduled_for ASC, priority DESC";
        return getTasksByStatus(sql);
    }


    public List<Task> getCompletedTasks() {
        String sql = "SELECT * FROM tasks WHERE status = 'COMPLETED' ORDER BY completed_at DESC";
        return getTasksByStatus(sql);
    }


    public List<Task> getFailedTasks() {
        String sql = "SELECT * FROM tasks WHERE status = 'FAILED' ORDER BY completed_at DESC";
        return getTasksByStatus(sql);
    }


    private List<Task> getTasksByStatus(String sql) {
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = JDBC.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Task task = mapResultSetToTask(rs);
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Error getting tasks: " + e.getMessage());
        }

        return tasks;
    }

    public boolean deleteTask(UUID id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = JDBC.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting task: " + e.getMessage());
            return false;
        }
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        int priority = rs.getInt("priority");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

        Timestamp scheduledForTs = rs.getTimestamp("scheduled_for");
        LocalDateTime scheduledFor = scheduledForTs != null ? scheduledForTs.toLocalDateTime() : null;

        Timestamp completedAtTs = rs.getTimestamp("completed_at");
        LocalDateTime completedAt = completedAtTs != null ? completedAtTs.toLocalDateTime() : null;

        Task.TaskStatus status = Task.TaskStatus.valueOf(rs.getString("status"));

        return new Task(id, name, description, priority, createdAt, scheduledFor, completedAt, status);
    }
}