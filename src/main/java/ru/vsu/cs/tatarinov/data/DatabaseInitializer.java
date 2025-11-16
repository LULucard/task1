package ru.vsu.cs.tatarinov.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        String url = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&AllowPublicKeyRetrieval=true";
        String user = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        if (user == null) user = "tatkr";
        if (password == null) password = "170505";

        //if (user == null || password == null) {
            //throw new RuntimeException("Database credentials not found in environment variables");
        //}

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            // Создаем базу данных если не существует
            stmt.execute("CREATE DATABASE IF NOT EXISTS social_network");
            stmt.execute("USE social_network");

            // Создаем таблицы
            createTables(stmt);

            System.out.println("Database initialized successfully!");

        } catch (Exception e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }

    private static void createTables(Statement stmt) throws Exception {
        // Таблица пользователей
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "gender VARCHAR(20), " +
                        "age INT, " +
                        "zodiac_sign VARCHAR(50), " +
                        "login VARCHAR(50) UNIQUE NOT NULL, " +
                        "password_hash VARCHAR(255) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        );

        // Таблица фотографий
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS photos (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id INT NOT NULL, " +
                        "file_name VARCHAR(255) NOT NULL, " +
                        "file_data LONGBLOB NOT NULL, " +
                        "file_size INT NOT NULL, " +
                        "mime_type VARCHAR(100) NOT NULL, " +
                        "uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)"
        );

        // Таблица отношений
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS relationships (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "source_user_id INT NOT NULL, " +
                        "target_user_id INT NOT NULL, " +
                        "relationship_type ENUM('SUBSCRIPTION', 'FRIENDSHIP', 'BLOCKED') NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (source_user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "UNIQUE KEY unique_relationship (source_user_id, target_user_id))"
        );

        // Таблица реакций
        stmt.execute(
                "CREATE TABLE IF NOT EXISTS photo_reactions (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id INT NOT NULL, " +
                        "photo_id INT NOT NULL, " +
                        "reaction_type ENUM('LIKE', 'DISLIKE') NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (photo_id) REFERENCES photos(id) ON DELETE CASCADE, " +
                        "UNIQUE KEY unique_user_photo_reaction (user_id, photo_id))"
        );
    }
}