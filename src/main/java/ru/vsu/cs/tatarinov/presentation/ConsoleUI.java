package ru.vsu.cs.tatarinov.presentation;

import ru.vsu.cs.tatarinov.business.SocialNetworkService;
import ru.vsu.cs.tatarinov.data.User;
import ru.vsu.cs.tatarinov.data.Photo;
import ru.vsu.cs.tatarinov.data.Relationship;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        System.out.println("4. Просмотреть мои фото");
        System.out.println("5. Удалить фото");
        System.out.println("6. Просмотреть других пользователей");
        System.out.println("7. Подписаться на пользователя");
        System.out.println("8. Заблокировать пользователя");
        System.out.println("9. Просмотреть фото пользователя");
        System.out.println("10. Оценить фото");
        System.out.println("11. Скачать фото");
        System.out.println("12. Просмотреть друзей");
        System.out.println("13. Удалить из друзей");
        System.out.println("14. Выйти из аккаунта");
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
                viewMyPhotos();
                break;
            case 5:
                deletePhoto();
                break;
            case 6:
                viewAllUsers();
                break;
            case 7:
                subscribeToUser();
                break;
            case 8:
                blockUser();
                break;
            case 9:
                viewUserPhotos();
                break;
            case 10:
                reactToPhoto();
                break;
            case 11:
                downloadPhoto();
                break;
            case 12:
                viewFriends();
                break;
            case 13:
                unfriendUser();
                break;
            case 14:
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

        System.out.print("Введите логин: ");
        String login = scanner.nextLine();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        SocialNetworkService.UserRegistrationResult result =
                socialNetworkService.createUser(name, gender, age, zodiacSign, login, password);

        System.out.println(result.getMessage());
        if (result.isSuccess()) {
            System.out.println("ID вашего профиля: " + result.getUser().getId());
        }
    }

    private void login() {
        System.out.println("\n--- Вход ---");
        System.out.print("Введите логин: ");
        String login = scanner.nextLine();

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine();

        SocialNetworkService.UserLoginResult result = socialNetworkService.loginUser(login, password);

        System.out.println(result.getMessage());
        if (result.isSuccess()) {
            currentUserId = result.getUser().getId();
            System.out.println("Добро пожаловать, " + result.getUser().getName() + "!");
        }
    }

    private void viewMyProfile() {
        Optional<User> user = socialNetworkService.getUserById(currentUserId);
        if (user.isPresent()) {
            displayUserProfile(user.get());
        } else {
            System.out.println("Ошибка: профиль не найден!");
        }
    }

    private void editProfile() {
        System.out.println("\n--- Редактирование профиля ---");
        Optional<User> currentUser = socialNetworkService.getUserById(currentUserId);

        if (!currentUser.isPresent()) {
            System.out.println("Ошибка: пользователь не найден!");
            return;
        }

        User user = currentUser.get();

        System.out.println("Текущие данные:");
        System.out.println("1. Имя: " + user.getName());
        System.out.println("2. Пол: " + user.getGender());
        System.out.println("3. Возраст: " + user.getAge());
        System.out.println("4. Знак зодиака: " + user.getZodiacSign());
        System.out.println("5. Отмена");

        System.out.print("Выберите поле для редактирования: ");
        int choice = readIntInput();

        switch (choice) {
            case 1:
                System.out.print("Введите новое имя: ");
                String newName = scanner.nextLine();
                // Здесь должна быть логика обновления в базе данных
                System.out.println("Имя обновлено на: " + newName);
                break;
            case 2:
                System.out.print("Введите новый пол: ");
                String newGender = scanner.nextLine();
                // Логика обновления
                System.out.println("Пол обновлен на: " + newGender);
                break;
            case 3:
                System.out.print("Введите новый возраст: ");
                int newAge = readIntInput();
                // Логика обновления
                System.out.println("Возраст обновлен на: " + newAge);
                break;
            case 4:
                System.out.print("Введите новый знак зодиака: ");
                String newZodiac = scanner.nextLine();
                // Логика обновления
                System.out.println("Знак зодиака обновлен на: " + newZodiac);
                break;
            case 5:
                System.out.println("Редактирование отменено.");
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private void addPhoto() {
        System.out.println("\n--- Добавление фото ---");
        System.out.print("Введите путь к файлу фото: ");
        String filePath = scanner.nextLine();

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                System.out.println("Файл не найден!");
                return;
            }

            String fileName = path.getFileName().toString();
            byte[] fileData = Files.readAllBytes(path);
            String mimeType = Files.probeContentType(path);

            if (mimeType == null || !mimeType.startsWith("image/")) {
                System.out.println("Файл не является изображением!");
                return;
            }

            SocialNetworkService.PhotoUploadResult result =
                    socialNetworkService.addPhotoToUser(currentUserId, fileName, fileData, mimeType);

            System.out.println(result.getMessage());
            if (result.isSuccess()) {
                System.out.println("ID фото: " + result.getPhoto().getId());
            }

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке фото: " + e.getMessage());
        }
    }

    private void viewMyPhotos() {
        System.out.println("\n--- Мои фото ---");
        List<Photo> photos = socialNetworkService.getUserPhotos(currentUserId);

        if (photos.isEmpty()) {
            System.out.println("У вас пока нет фото.");
            return;
        }

        for (int i = 0; i < photos.size(); i++) {
            Photo photo = photos.get(i);
            System.out.printf("%d. %s (ID: %d, Размер: %d KB, Лайков: %d, Дизлайков: %d)\n",
                    i + 1, photo.getFileName(), photo.getId(),
                    photo.getFileSize() / 1024, photo.getLikes().size(), photo.getDislikes().size());
        }
    }

    private void deletePhoto() {
        System.out.println("\n--- Удаление фото ---");
        List<Photo> photos = socialNetworkService.getUserPhotos(currentUserId);

        if (photos.isEmpty()) {
            System.out.println("У вас нет фото для удаления.");
            return;
        }

        viewMyPhotos();
        System.out.print("Введите номер фото для удаления: ");
        int photoIndex = readIntInput() - 1;

        if (photoIndex >= 0 && photoIndex < photos.size()) {
            Photo photo = photos.get(photoIndex);
            if (socialNetworkService.deletePhoto(photo.getId())) {
                System.out.println("Фото удалено!");
            } else {
                System.out.println("Ошибка при удалении фото!");
            }
        } else {
            System.out.println("Неверный номер фото!");
        }
    }

    private void viewAllUsers() {
        System.out.println("\n--- Все пользователи ---");
        List<User> users = socialNetworkService.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("Нет зарегистрированных пользователей.");
            return;
        }

        for (User user : users) {
            if (user.getId() == currentUserId) continue; // Пропускаем текущего пользователя

            if (socialNetworkService.canUserViewProfile(currentUserId, user.getId())) {
                String relationshipStatus = getRelationshipStatus(user.getId());
                System.out.printf("ID: %d, Имя: %s, Логин: %s %s\n",
                        user.getId(), user.getName(), user.getLogin(), relationshipStatus);
            } else {
                System.out.printf("ID: %d, Имя: [скрыто]\n", user.getId());
            }
        }
    }

    private void subscribeToUser() {
        System.out.println("\n--- Подписка на пользователя ---");
        System.out.print("Введите ID пользователя для подписки: ");
        int targetId = readIntInput();

        if (targetId == currentUserId) {
            System.out.println("Нельзя подписаться на самого себя!");
            return;
        }

        if (socialNetworkService.subscribe(currentUserId, targetId)) {
            System.out.println("Подписка оформлена!");
        } else {
            System.out.println("Ошибка при оформлении подписки! Возможно, пользователь заблокировал вас.");
        }
    }

    private void blockUser() {
        System.out.println("\n--- Блокировка пользователя ---");
        System.out.print("Введите ID пользователя для блокировки: ");
        int targetId = readIntInput();

        if (targetId == currentUserId) {
            System.out.println("Нельзя заблокировать самого себя!");
            return;
        }

        if (socialNetworkService.blockUser(currentUserId, targetId)) {
            System.out.println("Пользователь заблокирован!");
        } else {
            System.out.println("Ошибка при блокировке пользователя!");
        }
    }

    private void viewUserPhotos() {
        System.out.println("\n--- Просмотр фото пользователя ---");
        System.out.print("Введите ID пользователя: ");
        int targetId = readIntInput();

        Optional<User> user = socialNetworkService.getUserById(targetId);
        if (!user.isPresent()) {
            System.out.println("Пользователь не найден!");
            return;
        }

        if (!socialNetworkService.canUserViewProfile(currentUserId, targetId)) {
            System.out.println("У вас нет доступа к профилю этого пользователя!");
            return;
        }

        List<Photo> photos = socialNetworkService.getUserPhotos(targetId);
        if (photos.isEmpty()) {
            System.out.println("У пользователя нет фото.");
            return;
        }

        System.out.println("Фото пользователя " + user.get().getName() + ":");
        for (int i = 0; i < photos.size(); i++) {
            Photo photo = photos.get(i);
            if (socialNetworkService.canUserViewPhoto(currentUserId, photo.getId())) {
                System.out.printf("%d. %s (ID: %d, Лайков: %d, Дизлайков: %d)\n",
                        i + 1, photo.getFileName(), photo.getId(),
                        photo.getLikes().size(), photo.getDislikes().size());
            } else {
                System.out.printf("%d. %s [нет доступа]\n", i + 1, photo.getFileName());
            }
        }
    }

    private void reactToPhoto() {
        System.out.println("\n--- Оценка фото ---");
        System.out.print("Введите ID фото: ");
        int photoId = readIntInput();

        Optional<Photo> photo = socialNetworkService.getPhotoById(photoId);
        if (!photo.isPresent()) {
            System.out.println("Фото не найдено!");
            return;
        }

        if (!socialNetworkService.canUserViewPhoto(currentUserId, photoId)) {
            System.out.println("У вас нет доступа к этому фото!");
            return;
        }

        System.out.println("Фото: " + photo.get().getFileName());
        System.out.println("1. Лайк");
        System.out.println("2. Дизлайк");
        System.out.println("3. Удалить реакцию");
        System.out.print("Выберите действие: ");

        int choice = readIntInput();
        boolean success = false;

        switch (choice) {
            case 1:
                success = socialNetworkService.likePhoto(currentUserId, photoId);
                System.out.println(success ? "Лайк поставлен!" : "Ошибка!");
                break;
            case 2:
                success = socialNetworkService.dislikePhoto(currentUserId, photoId);
                System.out.println(success ? "Дизлайк поставлен!" : "Ошибка!");
                break;
            case 3:
                success = socialNetworkService.removePhotoReaction(currentUserId, photoId);
                System.out.println(success ? "Реакция удалена!" : "Ошибка!");
                break;
            default:
                System.out.println("Неверный выбор!");
        }
    }

    private void downloadPhoto() {
        System.out.println("\n--- Скачивание фото ---");
        System.out.print("Введите ID фото: ");
        int photoId = readIntInput();

        Optional<Photo> photo = socialNetworkService.getPhotoById(photoId);
        if (!photo.isPresent()) {
            System.out.println("Фото не найдено!");
            return;
        }

        if (!socialNetworkService.canUserViewPhoto(currentUserId, photoId)) {
            System.out.println("У вас нет доступа к этому фото!");
            return;
        }

        System.out.print("Введите путь для сохранения (с именем файла): ");
        String savePath = scanner.nextLine();

        try {
            byte[] fileData = socialNetworkService.getPhotoData(photoId);
            Files.write(Paths.get(savePath), fileData);
            System.out.println("Фото успешно сохранено: " + savePath);
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении фото: " + e.getMessage());
        }
    }

    private void viewFriends() {
        System.out.println("\n--- Мои друзья ---");
        List<User> allUsers = socialNetworkService.getAllUsers();
        boolean hasFriends = false;

        for (User user : allUsers) {
            if (user.getId() == currentUserId) continue;

            Optional<Relationship> relationship =
                    socialNetworkService.getUserById(currentUserId)
                            .flatMap(u -> u.getRelationships().stream()
                                    .filter(r -> r.getTargetUserId() == user.getId() &&
                                            r.getType() == Relationship.RelationshipType.FRIENDSHIP)
                                    .findFirst());

            if (relationship.isPresent()) {
                System.out.printf("ID: %d, Имя: %s, Логин: %s\n",
                        user.getId(), user.getName(), user.getLogin());
                hasFriends = true;
            }
        }

        if (!hasFriends) {
            System.out.println("У вас пока нет друзей.");
        }
    }

    private void unfriendUser() {
        System.out.println("\n--- Удаление из друзей ---");
        System.out.print("Введите ID пользователя: ");
        int targetId = readIntInput();

        if (socialNetworkService.unfriend(currentUserId, targetId)) {
            System.out.println("Пользователь удален из друзей!");
        } else {
            System.out.println("Ошибка! Возможно, этот пользователь не в ваших друзьях.");
        }
    }

    private void displayUserProfile(User user) {
        System.out.println("\n--- Профиль ---");
        System.out.println("ID: " + user.getId());
        System.out.println("Имя: " + user.getName());
        System.out.println("Пол: " + user.getGender());
        System.out.println("Возраст: " + user.getAge());
        System.out.println("Знак зодиака: " + user.getZodiacSign());
        System.out.println("Логин: " + user.getLogin());
        System.out.println("Количество фото: " + user.getPhotos().size());

        // Статистика по отношениям
        int subscriptions = 0;
        int friends = 0;
        int blocked = 0;

        for (Relationship rel : user.getRelationships()) {
            switch (rel.getType()) {
                case SUBSCRIPTION:
                    subscriptions++;
                    break;
                case FRIENDSHIP:
                    friends++;
                    break;
                case BLOCKED:
                    blocked++;
                    break;
            }
        }

        System.out.println("Подписки: " + subscriptions);
        System.out.println("Друзья: " + friends);
        System.out.println("Заблокировано: " + blocked);
    }

    private String getRelationshipStatus(int targetUserId) {
        Optional<User> currentUser = socialNetworkService.getUserById(currentUserId);
        if (!currentUser.isPresent()) return "";

        Optional<Relationship> relationship = currentUser.get().getRelationships().stream()
                .filter(r -> r.getTargetUserId() == targetUserId)
                .findFirst();

        if (relationship.isPresent()) {
            switch (relationship.get().getType()) {
                case FRIENDSHIP:
                    return "[Друг]";
                case SUBSCRIPTION:
                    return "[Подписка]";
                case BLOCKED:
                    return "[Заблокирован]";
            }
        }
        return "";
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

    // Вспомогательный метод для отображения информации о фото
    private void displayPhotoInfo(Photo photo) {
        System.out.printf("Фото: %s\n", photo.getFileName());
        System.out.printf("Размер: %d KB\n", photo.getFileSize() / 1024);
        System.out.printf("Тип: %s\n", photo.getMimeType());
        System.out.printf("Лайков: %d\n", photo.getLikes().size());
        System.out.printf("Дизлайков: %d\n", photo.getDislikes().size());
    }
}