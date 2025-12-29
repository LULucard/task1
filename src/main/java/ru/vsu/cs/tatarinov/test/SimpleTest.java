package ru.vsu.cs.tatarinov.test;

import java.sql.*;

public class SimpleTest {
    public static void main(String[] args) {
        // Попробуйте разные комбинации
        String[] testUsers = {
                "root:78905423121", // замените на ваш пароль root
                "tatkr:170505"
        };

        for (String userPass : testUsers) {
            String[] parts = userPass.split(":");
            String user = parts[0];
            String password = parts[1];

            System.out.println("\n🔧 Testing with user: " + user);

            String url = "jdbc:mysql://localhost:3306/mysql?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                System.out.println("✅ SUCCESS: Connected as " + user);

                // Проверяем создание БД
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("CREATE DATABASE IF NOT EXISTS social_network_test");
                    System.out.println("✅ Database created successfully");

                    // Убираем тестовую БД
                    stmt.execute("DROP DATABASE IF EXISTS social_network_test");
                }
                break; // Успешно, выходим

            } catch (SQLException e) {
                System.out.println("❌ FAILED: " + e.getMessage());
            }
        }
    }
}