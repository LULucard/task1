package ru.vsu.cs.tatarinov.business;

import ru.vsu.cs.tatarinov.data.*;

import java.util.List;
import java.util.Optional;

public class SocialNetworkService {
    private DataRepository dataRepository;

    public SocialNetworkService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public User createUser(String name, String gender, int age, String zodiacSign) {
        return dataRepository.createUser(name, gender, age, zodiacSign);
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

    public boolean addPhotoToUser(int userId, String photoPath) {
        if (getUserById(userId).isPresent()) {
            dataRepository.addPhotoToUser(userId, photoPath);
            return true;
        }
        return false;
    }

    public boolean subscribe(int subscriberId, int targetUserId) {
        if (subscriberId == targetUserId) return false;

        Optional<User> subscriber = getUserById(subscriberId);
        Optional<User> targetUser = getUserById(targetUserId);

        if (subscriber.isPresent() && targetUser.isPresent()) {

            Optional<Relationship> targetToSubscriber = dataRepository.getRelationship(targetUserId, subscriberId);
            if (targetToSubscriber.isPresent() &&
                    targetToSubscriber.get().getType() == Relationship.RelationshipType.BLOCKED) {
                return false;
            }

            dataRepository.addRelationship(subscriberId, targetUserId, Relationship.RelationshipType.SUBSCRIPTION);

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

    public boolean likePhoto(int userId, String photoPath) {
        Optional<Photo> photo = dataRepository.getPhotoByPath(photoPath);
        if (photo.isPresent()) {
            if (canUserViewPhoto(userId, photoPath)) {
                photo.get().addLike(userId);
                return true;
            }
        }
        return false;
    }

    public boolean dislikePhoto(int userId, String photoPath) {
        Optional<Photo> photo = dataRepository.getPhotoByPath(photoPath);
        if (photo.isPresent()) {
            if (canUserViewPhoto(userId, photoPath)) {
                photo.get().addDislike(userId);
                return true;
            }
        }
        return false;
    }

    public boolean canUserViewPhoto(int viewerId, String photoPath) {
        Optional<User> photoOwner = dataRepository.getAllUsers().stream()
                .filter(user -> user.getPhotos().contains(photoPath))
                .findFirst();

        if (photoOwner.isPresent()) {
            int ownerId = photoOwner.get().getId();

            if (viewerId == ownerId) return true;

            Optional<Relationship> relationship = dataRepository.getRelationship(ownerId, viewerId);
            if (relationship.isPresent()) {
                Relationship.RelationshipType type = relationship.get().getType();
                return type == Relationship.RelationshipType.SUBSCRIPTION ||
                        type == Relationship.RelationshipType.FRIENDSHIP;
            }
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
}