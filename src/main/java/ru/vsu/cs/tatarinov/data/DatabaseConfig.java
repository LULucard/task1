package ru.vsu.cs.tatarinov.data;

import java.io.InputStream;
import java.util.Properties;


public class DatabaseConfig {

    public static DatabaseConnection getConnection() {
        try {
            Properties properties = new Properties();

            // Попробуем загрузить из файла
            try (InputStream input = DatabaseConfig.class.getClassLoader()
                    .getResourceAsStream("database.properties")) {
                if (input != null) {
                    properties.load(input);
                }
            }

            // URL по умолчанию если файл не найден
            String url = properties.getProperty("db.url",
                    "jdbc:mysql://localhost:3306/social_network?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");

            String user = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");

            // Значения по умолчанию
            if (user == null) user = "tatkr";
            if (password == null) password = "170505";

            return new DatabaseConnection(url, user, password);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create database connection: " + e.getMessage(), e);
        }
    }
}