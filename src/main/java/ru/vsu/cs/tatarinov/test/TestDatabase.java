package ru.vsu.cs.tatarinov.test;

import ru.vsu.cs.tatarinov.data.*;


public class TestDatabase {
    public static void main(String[] args) {
        try {
            System.out.println("=== Database Test ===");

            // Проверка переменных окружения
            String dbUser = System.getenv("DB_USERNAME");
            String dbPassword = System.getenv("DB_PASSWORD");

            System.out.println("DB_USERNAME: " + (dbUser != null ? "***" : "NOT SET"));
            System.out.println("DB_PASSWORD: " + (dbPassword != null ? "***" : "NOT SET"));

            if (dbUser == null || dbPassword == null) {
                System.out.println("\n⚠️  Environment variables not set!");
                System.out.println("Trying with default credentials...");
            }

            System.out.println("\nInitializing database...");
            DatabaseInitializer.initializeDatabase();
            System.out.println("✅ Database initialized successfully!");

            testConnection();

        } catch (Exception e) {
            System.err.println("❌ Database test failed: " + e.getMessage());
            e.printStackTrace();

            System.out.println("\n🔧 Troubleshooting tips:");
            System.out.println("1. Make sure MySQL server is running");
            System.out.println("2. Set environment variables:");
            System.out.println("   - DB_USERNAME=your_username");
            System.out.println("   - DB_PASSWORD=your_password");
            System.out.println("3. Create database user in MySQL:");
            System.out.println("   CREATE USER 'social_user'@'localhost' IDENTIFIED BY 'password123';");
            System.out.println("   GRANT ALL PRIVILEGES ON social_network.* TO 'social_user'@'localhost';");
        }
    }

    private static void testConnection() {
        try {
            DatabaseConnection connection = DatabaseConfig.getConnection();
            var stmt = connection.getConnection().createStatement();
            var rs = stmt.executeQuery("SELECT 1 as test");
            if (rs.next()) {
                System.out.println("✅ Database connection test passed!");
            }
            connection.close();
        } catch (Exception e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
        }
    }
}