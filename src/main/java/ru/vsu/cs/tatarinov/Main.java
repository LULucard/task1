package ru.vsu.cs.tatarinov;

import ru.vsu.cs.tatarinov.data.DataRepository;
import ru.vsu.cs.tatarinov.business.SocialNetworkService;
import ru.vsu.cs.tatarinov.presentation.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        DataRepository dataRepository = new DataRepository();
        SocialNetworkService socialNetworkService = new SocialNetworkService(dataRepository);
        ConsoleUI consoleUI = new ConsoleUI(socialNetworkService);

        consoleUI.start();
    }
}