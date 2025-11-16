package ru.vsu.cs.tatarinov.data;

import java.sql.*;

public class DatabaseConnection implements AutoCloseable {
    private Connection connection;

    public DatabaseConnection(String url, String user, String password) {
        try {
            // Регистрируем драйвер (для совместимости со старыми версиями JDBC)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Устанавливаем соединение с базой данных
            this.connection = DriverManager.getConnection(url, user, password);

            // Проверяем, что соединение действительно
            if (this.connection == null || this.connection.isClosed()) {
                throw new SQLException("Failed to establish database connection");
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        } catch (SQLException e) {
            throw new RuntimeException("Error connecting to database: " + e.getMessage(), e);
        }
    }

    /**
     * Возвращает JDBC Connection для выполнения SQL запросов
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Выполняет тестовый запрос для проверки соединения
     */
    public boolean testConnection() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1");
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Начинает транзакцию
     */
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    /**
     * Подтверждает транзакцию
     */
    public void commitTransaction() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Откатывает транзакцию
     */
    public void rollbackTransaction() throws SQLException {
        connection.rollback();
        connection.setAutoCommit(true);
    }

    /**
     * Закрывает соединение с базой данных
     * Реализует AutoCloseable для использования в try-with-resources
     */
    @Override
    public void close() {
        if (connection != null) {
            try {
                // Проверяем, не закрыто ли уже соединение
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
                // Не бросаем исключение, чтобы не маскировать оригинальную ошибку
            }
        }
    }

    /**
     * Проверяет, открыто ли соединение
     */
    public boolean isOpen() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Возвращает информацию о соединении
     */
    public String getConnectionInfo() {
        try {
            if (connection == null || connection.isClosed()) {
                return "Connection is closed";
            }

            DatabaseMetaData metaData = connection.getMetaData();
            return String.format(
                    "Database: %s %s, URL: %s",
                    metaData.getDatabaseProductName(),
                    metaData.getDatabaseProductVersion(),
                    metaData.getURL()
            );
        } catch (SQLException e) {
            return "Unable to get connection info: " + e.getMessage();
        }
    }
}