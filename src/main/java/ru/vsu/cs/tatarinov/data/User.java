package ru.vsu.cs.tatarinov.data;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String gender;
    private int age;
    private String zodiacSign;
    private String login;
    private List<Photo> photos;
    private List<Relationship> relationships;

    public User(int id, String name, String gender, int age, String zodiacSign, String login) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.zodiacSign = zodiacSign;
        this.login = login;
        this.photos = new ArrayList<>();
        this.relationships = new ArrayList<>();
    }

    // Getters and setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getZodiacSign() { return zodiacSign; }
    public String getLogin() { return login; }
    public List<Photo> getPhotos() { return photos; }
    public List<Relationship> getRelationships() { return relationships; }

    public void setPhotos(List<Photo> photos) { this.photos = photos; }
    public void setRelationships(List<Relationship> relationships) { this.relationships = relationships; }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    public void removePhoto(int photoId) {
        photos.removeIf(p -> p.getId() == photoId);
    }

    public Photo getPhotoById(int photoId) {
        return photos.stream()
                .filter(p -> p.getId() == photoId)
                .findFirst()
                .orElse(null);
    }

    public void addRelationship(Relationship relationship) {
        relationships.removeIf(r -> r.getTargetUserId() == relationship.getTargetUserId());
        relationships.add(relationship);
    }

    public Relationship getRelationshipWith(int targetUserId) {
        return relationships.stream()
                .filter(r -> r.getTargetUserId() == targetUserId)
                .findFirst()
                .orElse(null);
    }

    // Вспомогательный метод для получения путей к файлам (для обратной совместимости)
    public List<String> getPhotoFileNames() {
        List<String> fileNames = new ArrayList<>();
        for (Photo photo : photos) {
            fileNames.add(photo.getFileName());
        }
        return fileNames;
    }
}