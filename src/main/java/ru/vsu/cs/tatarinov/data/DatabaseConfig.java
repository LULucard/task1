package ru.vsu.cs.tatarinov.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final String PROPERTIES_FILE = "database.properties";

    public static DatabaseConnection getConnection() {
        Properties properties = loadProperties();

        String url = properties.getProperty("db.url");
        String user = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        if (user == null) user = "tatkr";
        if (password == null) password = "170505";
        //if (user == null || password == null) {
            //throw new RuntimeException(
             //       "Database credentials not found in environment variables.\n" +
                           // "Please set DB_USERNAME and DB_PASSWORD environment variables."
            //);
        //}

        return new DatabaseConnection(url, user, password);
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                // Если файл не найден, используем значения по умолчанию
                properties.setProperty("db.url", "jdbc:mysql://localhost:3306/social_network?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
                return properties;
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading database properties", e);
        }
        return properties;
    }
}