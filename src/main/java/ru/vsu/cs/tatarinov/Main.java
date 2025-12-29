package ru.vsu.cs.tatarinov;

import ru.vsu.cs.tatarinov.data.*;
import ru.vsu.cs.tatarinov.business.SocialNetworkService;
import ru.vsu.cs.tatarinov.presentation.ConsoleUI;


public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== Social Network Application ===");

            // 1. Инициализация базы данных
            initializeDatabase();

            // 2. Инициализация репозиториев
            UserRepository userRepository = new UserRepository();
            PhotoRepository photoRepository = new PhotoRepository();
            RelationshipRepository relationshipRepository = new RelationshipRepository();
            ReactionRepository reactionRepository = new ReactionRepository();

            // 3. Создание сервиса (можно добавить фабрику позже)
            SocialNetworkService socialNetworkService = new SocialNetworkService(
                    userRepository,
                    photoRepository,
                    relationshipRepository,
                    reactionRepository
            );

            // 4. Запуск UI
            ConsoleUI consoleUI = new ConsoleUI(socialNetworkService);
            consoleUI.start();

        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeDatabase() {
        System.out.println("Initializing database...");

        String baseUrl = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "social_user";
        String password = "password123";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (var conn = java.sql.DriverManager.getConnection(baseUrl, user, password);
                 var stmt = conn.createStatement()) {

                System.out.println("Connected to MySQL successfully!");

                // Создаем базу данных если не существует
                stmt.execute("CREATE DATABASE IF NOT EXISTS social_network");
                stmt.execute("USE social_network");

                // Создаем таблицы
                createTables(stmt);

                System.out.println("Database initialized successfully!");
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing database: " + e.getMessage(), e);
        }
    }

    private static void createTables(java.sql.Statement stmt) throws Exception {
        // Users table
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

        // Photos table
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

        // Relationships table
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

        // Photo reactions table
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

        System.out.println("All tables created successfully!");
    }
}