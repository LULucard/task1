package ru.vsu.cs.tatarinov.business;

import ru.vsu.cs.tatarinov.data.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public class SocialNetworkService {
    private DataRepository dataRepository;

    public SocialNetworkService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    // User management (без изменений)
    public UserRegistrationResult createUser(String name, String gender, int age, String zodiacSign,
                                             String login, String password) {
        // Business logic validation
        if (name == null || name.trim().isEmpty()) {
            return new UserRegistrationResult(false, "Имя не может быть пустым");
        }

        if (login == null || login.trim().isEmpty()) {
            return new UserRegistrationResult(false, "Логин не может быть пустым");
        }

        if (password == null || password.length() < 6) {
            return new UserRegistrationResult(false, "Пароль должен содержать минимум 6 символов");
        }

        // Check if login already exists
        Optional<User> existingUser = dataRepository.getUserByLogin(login);
        if (existingUser.isPresent()) {
            return new UserRegistrationResult(false, "Пользователь с таким логином уже существует");
        }

        try {
            String passwordHash = hashPassword(password);
            int userId = dataRepository.createUser(name, gender, age, zodiacSign, login, passwordHash);
            User user = dataRepository.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден после создания"));

            return new UserRegistrationResult(true, "Пользователь успешно создан", user);
        } catch (Exception e) {
            return new UserRegistrationResult(false, "Ошибка при создании пользователя: " + e.getMessage());
        }
    }

    public UserLoginResult loginUser(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            return new UserLoginResult(false, "Логин не может быть пустым");
        }

        if (password == null || password.isEmpty()) {
            return new UserLoginResult(false, "Пароль не может быть пустым");
        }

        try {
            String passwordHash = hashPassword(password);
            boolean isValid = dataRepository.validateUserCredentials(login, passwordHash);

            if (isValid) {
                Optional<User> user = dataRepository.getUserByLogin(login);
                if (user.isPresent()) {
                    return new UserLoginResult(true, "Вход выполнен успешно", user.get());
                }
            }

            return new UserLoginResult(false, "Неверный логин или пароль");
        } catch (Exception e) {
            return new UserLoginResult(false, "Ошибка при входе: " + e.getMessage());
        }
    }

    public Optional<User> getUserById(int id) {
        return dataRepository.getUserById(id);
    }

    public List<User> getAllUsers() {
        return dataRepository.getAllUsers();
    }

    public boolean deleteUser(int id) {
        return dataRepository.deleteUser(id);
    }

    // Photo management (полностью переписаны)
    public PhotoUploadResult addPhotoToUser(int userId, String fileName, byte[] fileData, String mimeType) {
        // Business logic validation
        if (fileName == null || fileName.trim().isEmpty()) {
            return new PhotoUploadResult(false, "Имя файла не может быть пустым");
        }

        if (fileData == null || fileData.length == 0) {
            return new PhotoUploadResult(false, "Файл не может быть пустым");
        }

        if (fileData.length > 10 * 1024 * 1024) { // 10MB limit
            return new PhotoUploadResult(false, "Размер файла не должен превышать 10MB");
        }

        // Check if user exists
        Optional<User> user = getUserById(userId);
        if (!user.isPresent()) {
            return new PhotoUploadResult(false, "Пользователь не найден");
        }

        try {
            int photoId = dataRepository.addPhotoToUser(userId, fileName, fileData, mimeType);
            Optional<Photo> photo = dataRepository.getPhotoById(photoId);

            if (photo.isPresent()) {
                return new PhotoUploadResult(true, "Фото успешно загружено", photo.get());
            } else {
                return new PhotoUploadResult(false, "Ошибка при загрузке фото");
            }
        } catch (Exception e) {
            return new PhotoUploadResult(false, "Ошибка при загрузке фото: " + e.getMessage());
        }
    }

    public Optional<Photo> getPhotoById(int photoId) {
        return dataRepository.getPhotoById(photoId);
    }

    public byte[] getPhotoData(int photoId) {
        return dataRepository.getPhotoData(photoId);
    }

    public boolean deletePhoto(int photoId) {
        return dataRepository.deletePhoto(photoId);
    }

    public List<Photo> getUserPhotos(int userId) {
        return dataRepository.getUserPhotos(userId);
    }

    // Relationship management (без изменений)
    public boolean subscribe(int subscriberId, int targetUserId) {
        if (subscriberId == targetUserId) return false;

        Optional<User> subscriber = getUserById(subscriberId);
        Optional<User> targetUser = getUserById(targetUserId);

        if (subscriber.isPresent() && targetUser.isPresent()) {
            // Check if target user blocked subscriber
            Optional<Relationship> targetToSubscriber = dataRepository.getRelationship(targetUserId, subscriberId);
            if (targetToSubscriber.isPresent() &&
                    targetToSubscriber.get().getType() == Relationship.RelationshipType.BLOCKED) {
                return false;
            }

            dataRepository.addRelationship(subscriberId, targetUserId, Relationship.RelationshipType.SUBSCRIPTION);

            // Check if mutual subscription exists (friendship)
            Optional<Relationship> mutual = dataRepository.getRelationship(targetUserId, subscriberId);
            if (mutual.isPresent() && mutual.get().getType() == Relationship.RelationshipType.SUBSCRIPTION) {
                dataRepository.addRelationship(subscriberId, targetUserId, Relationship.RelationshipType.FRIENDSHIP);
                dataRepository.addRelationship(targetUserId, subscriberId, Relationship.RelationshipType.FRIENDSHIP);
            }

            return true;
        }
        return false;
    }

    public boolean blockUser(int blockerId, int targetUserId) {
        if (getUserById(blockerId).isPresent() && getUserById(targetUserId).isPresent()) {
            dataRepository.addRelationship(blockerId, targetUserId, Relationship.RelationshipType.BLOCKED);
            return true;
        }
        return false;
    }

    public boolean unfriend(int userId, int friendId) {
        Optional<Relationship> relationship = dataRepository.getRelationship(userId, friendId);
        if (relationship.isPresent() && relationship.get().getType() == Relationship.RelationshipType.FRIENDSHIP) {
            dataRepository.addRelationship(userId, friendId, Relationship.RelationshipType.SUBSCRIPTION);
            dataRepository.addRelationship(friendId, userId, Relationship.RelationshipType.SUBSCRIPTION);
            return true;
        }
        return false;
    }

    // Photo reactions (обновлены для работы с photo_id)
    public boolean likePhoto(int userId, int photoId) {
        Optional<Photo> photo = getPhotoById(photoId);
        if (photo.isPresent()) {
            if (canUserViewPhoto(userId, photoId)) {
                dataRepository.addPhotoReaction(userId, photoId, "LIKE");
                return true;
            }
        }
        return false;
    }

    public boolean dislikePhoto(int userId, int photoId) {
        Optional<Photo> photo = getPhotoById(photoId);
        if (photo.isPresent()) {
            if (canUserViewPhoto(userId, photoId)) {
                dataRepository.addPhotoReaction(userId, photoId, "DISLIKE");
                return true;
            }
        }
        return false;
    }

    public boolean removePhotoReaction(int userId, int photoId) {
        Optional<Photo> photo = getPhotoById(photoId);
        if (photo.isPresent()) {
            dataRepository.removePhotoReaction(userId, photoId);
            return true;
        }
        return false;
    }

    // Permission checks (обновлены для работы с photo_id)
    public boolean canUserViewPhoto(int viewerId, int photoId) {
        Optional<Photo> photo = getPhotoById(photoId);
        if (!photo.isPresent()) {
            return false;
        }

        int ownerId = photo.get().getUserId();

        if (viewerId == ownerId) return true;

        Optional<Relationship> relationship = dataRepository.getRelationship(ownerId, viewerId);
        if (relationship.isPresent()) {
            Relationship.RelationshipType type = relationship.get().getType();
            return type == Relationship.RelationshipType.SUBSCRIPTION ||
                    type == Relationship.RelationshipType.FRIENDSHIP;
        }
        return false;
    }

    public boolean canUserViewProfile(int viewerId, int targetUserId) {
        if (viewerId == targetUserId) return true;

        Optional<Relationship> relationship = dataRepository.getRelationship(targetUserId, viewerId);
        if (relationship.isPresent()) {
            return relationship.get().getType() != Relationship.RelationshipType.BLOCKED;
        }
        return true;
    }

    public Optional<Relationship> getRelationship(int sourceUserId, int targetUserId) {
        return dataRepository.getRelationship(sourceUserId, targetUserId);
    }

    // Password hashing (без изменений)
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // Result classes
    public static class UserRegistrationResult {
        private final boolean success;
        private final String message;
        private final User user;

        public UserRegistrationResult(boolean success, String message) {
            this(success, message, null);
        }

        public UserRegistrationResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }

    public static class UserLoginResult {
        private final boolean success;
        private final String message;
        private final User user;

        public UserLoginResult(boolean success, String message) {
            this(success, message, null);
        }

        public UserLoginResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }

    public static class PhotoUploadResult {
        private final boolean success;
        private final String message;
        private final Photo photo;

        public PhotoUploadResult(boolean success, String message) {
            this(success, message, null);
        }

        public PhotoUploadResult(boolean success, String message, Photo photo) {
            this.success = success;
            this.message = message;
            this.photo = photo;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Photo getPhoto() { return photo; }
    }
}