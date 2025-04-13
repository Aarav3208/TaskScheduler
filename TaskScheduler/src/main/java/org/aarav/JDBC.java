package org.aarav;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles database connections and initialization for the task scheduler.
 */
public class JDBC {
    private static final String HOST = "localhost";
    private static final int PORT = 5432;
    private static final String DB_NAME = "task_scheduler";
    private static final String DEFAULT_DB = "postgres"; // Default database to connect to first
    private static final String USER = "postgres";
    private static final String PASSWORD = null;

    private static Connection connection = null;

    /**
     * Ensures the database exists, creating it if necessary.
     * @throws SQLException if a database access error occurs
     */
    public static void ensureDatabaseExists() throws SQLException {
        String defaultUrl = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DEFAULT_DB;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found", e);
        }

        // Connect to default database to check if our database exists
        try (Connection defaultConn = DriverManager.getConnection(defaultUrl, USER, PASSWORD);
             Statement stmt = defaultConn.createStatement()) {

            // Check if our database exists
            ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM pg_database WHERE datname = '" + DB_NAME + "'");

            boolean dbExists = rs.next();

            if (!dbExists) {
                // Create the database if it doesn't exist
                stmt.execute("CREATE DATABASE " + DB_NAME);
                System.out.println("Database '" + DB_NAME + "' created successfully");
            }
        }
    }

    /**
     * Establishes a connection to the PostgreSQL database.
     * @return A database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            ensureDatabaseExists();

            String dbUrl = "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME;
            connection = DriverManager.getConnection(dbUrl, USER, PASSWORD);
        }
        return connection;
    }

    /**
     * Initializes the database with required tables if they don't exist.
     * @throws SQLException if a database access error occurs
     */
    public static void initDatabase() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // First, drop the table if it exists (for clean initialization)
            stmt.execute("DROP TABLE IF EXISTS tasks");

            // Create tasks table
            String createTaskTable = "CREATE TABLE tasks (" +
                    "id UUID PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "priority INTEGER NOT NULL, " +
                    "created_at TIMESTAMP NOT NULL, " +
                    "scheduled_for TIMESTAMP, " +
                    "completed_at TIMESTAMP, " +
                    "status VARCHAR(20) NOT NULL" +
                    ")";
            stmt.execute(createTaskTable);

            System.out.println("Database tables initialized successfully");
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
}