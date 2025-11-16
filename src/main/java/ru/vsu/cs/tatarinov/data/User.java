package ru.vsu.cs.tatarinov.data;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String gender;
    private int age;
    private String zodiacSign;
    private List<String> photos;
    private List<Relationship> relationships;

    public User(int id, String name, String gender, int age, String zodiacSign) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.zodiacSign = zodiacSign;
        this.photos = new ArrayList<>();
        this.relationships = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getGender() { return gender; }
    public int getAge() { return age; }
    public String getZodiacSign() { return zodiacSign; }
    public List<String> getPhotos() { return photos; }
    public List<Relationship> getRelationships() { return relationships; }

    public void addPhoto(String photoPath) {
        photos.add(photoPath);
    }

    public void removePhoto(String photoPath) {
        photos.remove(photoPath);
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
}