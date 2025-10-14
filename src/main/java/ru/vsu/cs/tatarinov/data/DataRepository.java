package ru.vsu.cs.tatarinov.data;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataRepository {
    private List<User> users;
    private List<Photo> photos;
    private int nextUserId;

    public DataRepository() {
        this.users = new ArrayList<>();
        this.photos = new ArrayList<>();
        this.nextUserId = 1;
    }

    public User createUser(String name, String gender, int age, String zodiacSign) {
        User user = new User(nextUserId++, name, gender, age, zodiacSign);
        users.add(user);
        return user;
    }

    public Optional<User> getUserById(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    public boolean deleteUser(int id) {
        return users.removeIf(u -> u.getId() == id);
    }

    public void addPhotoToUser(int userId, String photoPath) {
        getUserById(userId).ifPresent(user -> {
            user.addPhoto(photoPath);
            photos.add(new Photo(photoPath));
        });
    }

    public Optional<Photo> getPhotoByPath(String filePath) {
        return photos.stream().filter(p -> p.getFilePath().equals(filePath)).findFirst();
    }

    public void addRelationship(int sourceUserId, int targetUserId, Relationship.RelationshipType type) {
        getUserById(sourceUserId).ifPresent(sourceUser -> {
            Relationship relationship = new Relationship(sourceUserId, targetUserId, type);
            sourceUser.addRelationship(relationship);
        });
    }

    public Optional<Relationship> getRelationship(int sourceUserId, int targetUserId) {
        return getUserById(sourceUserId)
                .map(user -> user.getRelationshipWith(targetUserId));
    }
}