package ru.vsu.cs.tatarinov.business;

import ru.vsu.cs.tatarinov.data.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

public class SocialNetworkService {
    private UserRepository userRepository;
    private PhotoRepository photoRepository;
    private RelationshipRepository relationshipRepository;
    private ReactionRepository reactionRepository;

    public SocialNetworkService() {
        this.userRepository = new UserRepository();
        this.photoRepository = new PhotoRepository();
        this.relationshipRepository = new RelationshipRepository();
        this.reactionRepository = new ReactionRepository();
    }

    // Конструктор с инъекцией зависимостей (для тестирования)
    public SocialNetworkService(UserRepository userRepository,
                                PhotoRepository photoRepository,
                                RelationshipRepository relationshipRepository,
                                ReactionRepository reactionRepository) {
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
        this.relationshipRepository = relationshipRepository;
        this.reactionRepository = reactionRepository;
    }

    // User management
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
        Optional<User> existingUser = userRepository.findByLogin(login);
        if (existingUser.isPresent()) {
            return new UserRegistrationResult(false, "Пользователь с таким логином уже существует");
        }

        try {
            String passwordHash = hashPassword(password);
            User user = new User(0, name, gender, age, zodiacSign, login);
            int userId = userRepository.create(user, passwordHash);

            User createdUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден после создания"));

            return new UserRegistrationResult(true, "Пользователь успешно создан", createdUser);
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
            boolean isValid = userRepository.validateCredentials(login, passwordHash);

            if (isValid) {
                Optional<User> user = userRepository.findByLogin(login);
                if (user.isPresent()) {
                    // Load relationships and photos for the user
                    loadUserRelationships(user.get());
                    loadUserPhotos(user.get());
                    return new UserLoginResult(true, "Вход выполнен успешно", user.get());
                }
            }

            return new UserLoginResult(false, "Неверный логин или пароль");
        } catch (Exception e) {
            return new UserLoginResult(false, "Ошибка при входе: " + e.getMessage());
        }
    }

    public Optional<User> getUserById(int id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            loadUserRelationships(user.get());
            loadUserPhotos(user.get());
        }
        return user;
    }

    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            loadUserRelationships(user);
            loadUserPhotos(user);
        }
        return users;
    }

    public boolean deleteUser(int id) {
        return userRepository.delete(id);
    }

    public boolean updateUser(User user) {
        return userRepository.update(user);
    }

    // Photo management
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
        if (!userRepository.findById(userId).isPresent()) {
            return new PhotoUploadResult(false, "Пользователь не найден");
        }

        try {
            Photo photo = new Photo(userId, fileName, fileData, mimeType);
            int photoId = photoRepository.save(photo);

            Photo savedPhoto = photoRepository.findById(photoId)
                    .orElseThrow(() -> new RuntimeException("Фото не найдено после сохранения"));

            // Load reactions for the photo
            loadPhotoReactions(savedPhoto);

            return new PhotoUploadResult(true, "Фото успешно загружено", savedPhoto);
        } catch (Exception e) {
            return new PhotoUploadResult(false, "Ошибка при загрузке фото: " + e.getMessage());
        }
    }

    public Optional<Photo> getPhotoById(int photoId) {
        Optional<Photo> photo = photoRepository.findById(photoId);
        if (photo.isPresent()) {
            loadPhotoReactions(photo.get());
        }
        return photo;
    }

    public byte[] getPhotoData(int photoId) {
        return photoRepository.getFileData(photoId);
    }

    public boolean deletePhoto(int photoId) {
        return photoRepository.delete(photoId);
    }

    public List<Photo> getUserPhotos(int userId) {
        List<Photo> photos = photoRepository.findByUserId(userId);
        for (Photo photo : photos) {
            loadPhotoReactions(photo);
        }
        return photos;
    }

    // Relationship management
    public boolean subscribe(int subscriberId, int targetUserId) {
        if (subscriberId == targetUserId) return false;

        Optional<User> subscriber = userRepository.findById(subscriberId);
        Optional<User> targetUser = userRepository.findById(targetUserId);

        if (subscriber.isPresent() && targetUser.isPresent()) {
            // Check if target user blocked subscriber
            Optional<Relationship> targetToSubscriber = relationshipRepository.find(targetUserId, subscriberId);
            if (targetToSubscriber.isPresent() &&
                    targetToSubscriber.get().getType() == Relationship.RelationshipType.BLOCKED) {
                return false;
            }

            Relationship relationship = new Relationship(subscriberId, targetUserId,
                    Relationship.RelationshipType.SUBSCRIPTION);
            relationshipRepository.save(relationship);

            // Check if mutual subscription exists (friendship)
            Optional<Relationship> mutual = relationshipRepository.find(targetUserId, subscriberId);
            if (mutual.isPresent() && mutual.get().getType() == Relationship.RelationshipType.SUBSCRIPTION) {
                relationshipRepository.save(new Relationship(subscriberId, targetUserId,
                        Relationship.RelationshipType.FRIENDSHIP));
                relationshipRepository.save(new Relationship(targetUserId, subscriberId,
                        Relationship.RelationshipType.FRIENDSHIP));
            }

            return true;
        }
        return false;
    }

    public boolean blockUser(int blockerId, int targetUserId) {
        if (userRepository.findById(blockerId).isPresent() &&
                userRepository.findById(targetUserId).isPresent()) {

            Relationship relationship = new Relationship(blockerId, targetUserId,
                    Relationship.RelationshipType.BLOCKED);
            relationshipRepository.save(relationship);
            return true;
        }
        return false;
    }

    public boolean unfriend(int userId, int friendId) {
        Optional<Relationship> relationship = relationshipRepository.find(userId, friendId);
        if (relationship.isPresent() && relationship.get().getType() == Relationship.RelationshipType.FRIENDSHIP) {
            relationshipRepository.save(new Relationship(userId, friendId,
                    Relationship.RelationshipType.SUBSCRIPTION));
            relationshipRepository.save(new Relationship(friendId, userId,
                    Relationship.RelationshipType.SUBSCRIPTION));
            return true;
        }
        return false;
    }

    // Photo reactions
    public boolean likePhoto(int userId, int photoId) {
        Optional<Photo> photo = getPhotoById(photoId);
        if (photo.isPresent()) {
            if (canUserViewPhoto(userId, photoId)) {
                reactionRepository.saveReaction(userId, photoId, "LIKE");
                // Обновляем реакции в объекте фото
                photo.get().addLike(userId);
                return true;
            }
        }
        return false;
    }

    public boolean dislikePhoto(int userId, int photoId) {
        Optional<Photo> photo = getPhotoById(photoId);
        if (photo.isPresent()) {
            if (canUserViewPhoto(userId, photoId)) {
                reactionRepository.saveReaction(userId, photoId, "DISLIKE");
                // Обновляем реакции в объекте фото
                photo.get().addDislike(userId);
                return true;
            }
        }
        return false;
    }

    public boolean removePhotoReaction(int userId, int photoId) {
        Optional<Photo> photo = getPhotoById(photoId);
        if (photo.isPresent()) {
            reactionRepository.deleteReaction(userId, photoId);
            // Обновляем реакции в объекте фото
            photo.get().removeReaction(userId);
            return true;
        }
        return false;
    }

    // Permission checks
    public boolean canUserViewPhoto(int viewerId, int photoId) {
        Optional<Photo> photo = getPhotoById(photoId);
        if (!photo.isPresent()) {
            return false;
        }

        int ownerId = photo.get().getUserId();

        if (viewerId == ownerId) return true;

        Optional<Relationship> relationship = relationshipRepository.find(ownerId, viewerId);
        if (relationship.isPresent()) {
            Relationship.RelationshipType type = relationship.get().getType();
            return type == Relationship.RelationshipType.SUBSCRIPTION ||
                    type == Relationship.RelationshipType.FRIENDSHIP;
        }
        return false;
    }

    public boolean canUserViewProfile(int viewerId, int targetUserId) {
        if (viewerId == targetUserId) return true;

        Optional<Relationship> relationship = relationshipRepository.find(targetUserId, viewerId);
        if (relationship.isPresent()) {
            return relationship.get().getType() != Relationship.RelationshipType.BLOCKED;
        }
        return true;
    }

    public Optional<Relationship> getRelationship(int sourceUserId, int targetUserId) {
        return relationshipRepository.find(sourceUserId, targetUserId);
    }

    // Helper methods
    private void loadUserRelationships(User user) {
        List<Relationship> relationships = relationshipRepository.findBySourceUser(user.getId());
        user.setRelationships(relationships);
    }

    private void loadUserPhotos(User user) {
        List<Photo> photos = photoRepository.findByUserId(user.getId());
        for (Photo photo : photos) {
            loadPhotoReactions(photo);
        }
        user.setPhotos(photos);
    }

    private void loadPhotoReactions(Photo photo) {
        List<Integer> likes = reactionRepository.getLikes(photo.getId());
        List<Integer> dislikes = reactionRepository.getDislikes(photo.getId());
        photo.setLikes(likes);
        photo.setDislikes(dislikes);
    }

    // Password hashing
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

    // Result classes (без изменений)
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