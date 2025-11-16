package ru.vsu.cs.tatarinov.presentation;


import ru.vsu.cs.tatarinov.business.SocialNetworkService;
import ru.vsu.cs.tatarinov.data.User;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleUI {
    private SocialNetworkService socialNetworkService;
    private Scanner scanner;
    private int currentUserId;

    public ConsoleUI(SocialNetworkService socialNetworkService) {
        this.socialNetworkService = socialNetworkService;
        this.scanner = new Scanner(System.in);
        this.currentUserId = -1;
    }

    public void start() {
        System.out.println("=== Добро пожаловать в социальную сеть! ===");

        while (true) {
            if (currentUserId == -1) {
                showMainMenu();
            } else {
                showUserMenu();
            }
        }
    }

    private void showMainMenu() {
        System.out.println("\n--- Главное меню ---");
        System.out.println("1. Регистрация");
        System.out.println("2. Вход");
        System.out.println("3. Выход");
        System.out.print("Выберите действие: ");

        int choice = readIntInput();

        switch (choice) {
            case 1:
                registerUser();
                break;
            case 2:
                login();
                break;
            case 3:
                System.out.println("До свидания!");
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private void showUserMenu() {
        System.out.println("\n--- Личный кабинет ---");
        System.out.println("1. Просмотреть профиль");
        System.out.println("2. Редактировать профиль");
        System.out.println("3. Добавить фото");
        System.out.println("4. Просмотреть других пользователей");
        System.out.println("5. Подписаться на пользователя");
        System.out.println("6. Заблокировать пользователя");
        System.out.println("7. Просмотреть фото пользователя");
        System.out.println("8. Оценить фото");
        System.out.println("9. Выйти из аккаунта");
        System.out.print("Выберите действие: ");

        int choice = readIntInput();

        switch (choice) {
            case 1:
                viewMyProfile();
                break;
            case 2:
                editProfile();
                break;
            case 3:
                addPhoto();
                break;
            case 4:
                viewAllUsers();
                break;
            case 5:
                subscribeToUser();
                break;
            case 6:
                blockUser();
                break;
            case 7:
                viewUserPhotos();
                break;
            case 8:
                reactToPhoto();
                break;
            case 9:
                currentUserId = -1;
                System.out.println("Вы вышли из аккаунта.");
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private void registerUser() {
        System.out.println("\n--- Регистрация ---");
        System.out.print("Введите имя: ");
        String name = scanner.nextLine();

        System.out.print("Введите пол: ");
        String gender = scanner.nextLine();

        System.out.print("Введите возраст: ");
        int age = readIntInput();

        System.out.print("Введите знак зодиака: ");
        String zodiacSign = scanner.nextLine();

        User user = socialNetworkService.createUser(name, gender, age, zodiacSign);
        System.out.println("Пользователь создан! ID: " + user.getId());
    }

    private void login() {
        System.out.println("\n--- Вход ---");
        System.out.print("Введите ID пользователя: ");
        int id = readIntInput();

        Optional<User> user = socialNetworkService.getUserById(id);
        if (user.isPresent()) {
            currentUserId = id;
            System.out.println("Добро пожаловать, " + user.get().getName() + "!");
        } else {
            System.out.println("Пользователь не найден!");
        }
    }

    private void viewMyProfile() {
        Optional<User> user = socialNetworkService.getUserById(currentUserId);
        user.ifPresent(this::displayUserProfile);
    }

    private void editProfile() {
        // Implementation for profile editing
        System.out.println("Функция редактирования профиля в разработке...");
    }

    private void addPhoto() {
        System.out.print("Введите путь к файлу фото: ");
        String photoPath = scanner.nextLine();

        if (socialNetworkService.addPhotoToUser(currentUserId, photoPath)) {
            System.out.println("Фото добавлено!");
        } else {
            System.out.println("Ошибка при добавлении фото!");
        }
    }

    private void viewAllUsers() {
        List<User> users = socialNetworkService.getAllUsers();
        System.out.println("\n--- Все пользователи ---");
        for (User user : users) {
            if (socialNetworkService.canUserViewProfile(currentUserId, user.getId())) {
                System.out.println("ID: " + user.getId() + ", Имя: " + user.getName());
            } else {
                System.out.println("ID: " + user.getId() + ", Имя: [скрыто]");
            }
        }
    }

    private void subscribeToUser() {
        System.out.print("Введите ID пользователя для подписки: ");
        int targetId = readIntInput();

        if (socialNetworkService.subscribe(currentUserId, targetId)) {
            System.out.println("Подписка оформлена!");
        } else {
            System.out.println("Ошибка при оформлении подписки!");
        }
    }

    private void blockUser() {
        System.out.print("Введите ID пользователя для блокировки: ");
        int targetId = readIntInput();

        if (socialNetworkService.blockUser(currentUserId, targetId)) {
            System.out.println("Пользователь заблокирован!");
        } else {
            System.out.println("Ошибка при блокировке пользователя!");
        }
    }

    private void viewUserPhotos() {
        System.out.print("Введите ID пользователя: ");
        int targetId = readIntInput();

        Optional<User> user = socialNetworkService.getUserById(targetId);
        if (user.isPresent() && socialNetworkService.canUserViewProfile(currentUserId, targetId)) {
            if (socialNetworkService.canUserViewPhoto(currentUserId, "")) { // Simplified check
                System.out.println("Фото пользователя " + user.get().getName() + ":");
                for (String photoPath : user.get().getPhotos()) {
                    System.out.println(" - " + photoPath);
                }
            } else {
                System.out.println("У вас нет доступа к фото этого пользователя!");
            }
        } else {
            System.out.println("Пользователь не найден или доступ запрещен!");
        }
    }

    private void reactToPhoto() {
        System.out.print("Введите путь к файлу фото: ");
        String photoPath = scanner.nextLine();

        System.out.println("1. Лайк");
        System.out.println("2. Дизлайк");
        System.out.print("Выберите действие: ");
        int choice = readIntInput();

        boolean success = false;
        switch (choice) {
            case 1:
                success = socialNetworkService.likePhoto(currentUserId, photoPath);
                break;
            case 2:
                success = socialNetworkService.dislikePhoto(currentUserId, photoPath);
                break;
            default:
                System.out.println("Неверный выбор!");
                return;
        }

        if (success) {
            System.out.println("Реакция добавлена!");
        } else {
            System.out.println("Ошибка! Возможно, у вас нет доступа к этому фото.");
        }
    }

    private void displayUserProfile(User user) {
        System.out.println("\n--- Профиль ---");
        System.out.println("ID: " + user.getId());
        System.out.println("Имя: " + user.getName());
        System.out.println("Пол: " + user.getGender());
        System.out.println("Возраст: " + user.getAge());
        System.out.println("Знак зодиака: " + user.getZodiacSign());
        System.out.println("Фото: " + user.getPhotos().size() + " шт.");
    }

    private int readIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Пожалуйста, введите число: ");
            }
        }
    }
}
