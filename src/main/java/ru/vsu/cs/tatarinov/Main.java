package ru.vsu.cs.tatarinov;

import ru.vsu.cs.tatarinov.data.DataRepository;
import ru.vsu.cs.tatarinov.business.SocialNetworkService;
import ru.vsu.cs.tatarinov.presentation.ConsoleUI;
import ru.vsu.cs.tatarinov.data.DatabaseInitializer;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== Social Network Application ===");

            String dbUser = System.getenv("DB_USERNAME");
            String dbPassword = System.getenv("DB_PASSWORD");

            if (dbUser == null || dbPassword == null) {
                System.err.println(
                        "ERROR: Database credentials not found!\n" +
                                "Please set environment variables:\n" +
                                "  DB_USERNAME=your_username\n" +
                                "  DB_PASSWORD=your_password\n\n" +
                                "Windows (CMD): setx DB_USERNAME \"social_user\" && setx DB_PASSWORD \"your_password\"\n" +
                                "macOS/Linux: export DB_USERNAME=\"social_user\" && export DB_PASSWORD=\"your_password\""
                );
                return;
            }
            System.out.println("Initializing database...");
            DatabaseInitializer.initializeDatabase();
            DataRepository dataRepository = new DataRepository();
            SocialNetworkService socialNetworkService = new SocialNetworkService(dataRepository);
            ConsoleUI consoleUI = new ConsoleUI(socialNetworkService);
            System.out.println("Application started successfully!");
            consoleUI.start();
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
            System.err.println("\nTroubleshooting tips:");
            System.err.println("1. Make sure MySQL server is running");
            System.err.println("2. Check DB_USERNAME and DB_PASSWORD environment variables");
            System.err.println("3. Verify database user has proper privileges");
            System.err.println("4. Check if database 'social_network' exists");
        }
    }
}