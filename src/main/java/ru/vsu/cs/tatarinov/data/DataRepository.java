package ru.vsu.cs.tatarinov.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataRepository {

    // User operations (без изменений)
    public int createUser(String name, String gender, int age, String zodiacSign, String login, String passwordHash) {
        String sql = "INSERT INTO users (name, gender, age, zodiac_sign, login, password_hash) VALUES (?, ?, ?, ?, ?, ?)";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, gender);
            stmt.setInt(3, age);
            stmt.setString(4, zodiacSign);
            stmt.setString(5, login);
            stmt.setString(6, passwordHash);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating user", e);
        }
    }

    public Optional<User> getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapUserFromResultSet(rs);
                loadUserPhotos(user);
                loadUserRelationships(user);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting user by ID", e);
        }
        return Optional.empty();
    }

    public Optional<User> getUserByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapUserFromResultSet(rs);
                loadUserPhotos(user);
                loadUserRelationships(user);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting user by login", e);
        }
        return Optional.empty();
    }

    public boolean validateUserCredentials(String login, String passwordHash) {
        String sql = "SELECT id FROM users WHERE login = ? AND password_hash = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error validating user credentials", e);
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = mapUserFromResultSet(rs);
                loadUserPhotos(user);
                loadUserRelationships(user);
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting all users", e);
        }
        return users;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    // Photo operations (полностью переписаны для работы с бинарными данными)
    public int addPhotoToUser(int userId, String fileName, byte[] fileData, String mimeType) {
        String sql = "INSERT INTO photos (user_id, file_name, file_data, file_size, mime_type) VALUES (?, ?, ?, ?, ?)";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, userId);
            stmt.setString(2, fileName);
            stmt.setBytes(3, fileData);
            stmt.setInt(4, fileData.length);
            stmt.setString(5, mimeType);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Adding photo failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Adding photo failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adding photo to user", e);
        }
    }

    public Optional<Photo> getPhotoById(int photoId) {
        String sql = "SELECT * FROM photos WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, photoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Photo photo = mapPhotoFromResultSet(rs);
                loadPhotoReactions(photo);
                return Optional.of(photo);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting photo by ID", e);
        }
        return Optional.empty();
    }

    public byte[] getPhotoData(int photoId) {
        String sql = "SELECT file_data FROM photos WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, photoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBytes("file_data");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting photo data", e);
        }
        return new byte[0];
    }

    public boolean deletePhoto(int photoId) {
        String sql = "DELETE FROM photos WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, photoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting photo", e);
        }
    }

    public List<Photo> getUserPhotos(int userId) {
        List<Photo> photos = new ArrayList<>();
        String sql = "SELECT * FROM photos WHERE user_id = ? ORDER BY uploaded_at DESC";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Photo photo = mapPhotoFromResultSet(rs);
                loadPhotoReactions(photo);
                photos.add(photo);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting user photos", e);
        }
        return photos;
    }

    // Relationship operations (без изменений)
    public void addRelationship(int sourceUserId, int targetUserId, Relationship.RelationshipType type) {
        String sql = "INSERT INTO relationships (source_user_id, target_user_id, relationship_type) " +
                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE relationship_type = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, sourceUserId);
            stmt.setInt(2, targetUserId);
            stmt.setString(3, type.toString());
            stmt.setString(4, type.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding relationship", e);
        }
    }

    public Optional<Relationship> getRelationship(int sourceUserId, int targetUserId) {
        String sql = "SELECT * FROM relationships WHERE source_user_id = ? AND target_user_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, sourceUserId);
            stmt.setInt(2, targetUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Relationship.RelationshipType type = Relationship.RelationshipType.valueOf(
                        rs.getString("relationship_type")
                );
                return Optional.of(new Relationship(sourceUserId, targetUserId, type));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting relationship", e);
        }
        return Optional.empty();
    }

    // Photo reaction operations (обновлены для работы с photo_id)
    public void addPhotoReaction(int userId, int photoId, String reactionType) {
        String sql = "INSERT INTO photo_reactions (user_id, photo_id, reaction_type) " +
                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE reaction_type = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, photoId);
            stmt.setString(3, reactionType);
            stmt.setString(4, reactionType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding photo reaction", e);
        }
    }

    public void removePhotoReaction(int userId, int photoId) {
        String sql = "DELETE FROM photo_reactions WHERE user_id = ? AND photo_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, photoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing photo reaction", e);
        }
    }

    // Helper methods
    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("gender"),
                rs.getInt("age"),
                rs.getString("zodiac_sign"),
                rs.getString("login")
        );
    }

    private Photo mapPhotoFromResultSet(ResultSet rs) throws SQLException {
        return new Photo(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("file_name"),
                rs.getBytes("file_data"),
                rs.getInt("file_size"),
                rs.getString("mime_type")
        );
    }

    private void loadUserPhotos(User user) throws SQLException {
        List<Photo> photos = getUserPhotos(user.getId());
        user.setPhotos(photos);
    }

    private void loadUserRelationships(User user) throws SQLException {
        String sql = "SELECT target_user_id, relationship_type FROM relationships WHERE source_user_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            List<Relationship> relationships = new ArrayList<>();
            while (rs.next()) {
                int targetUserId = rs.getInt("target_user_id");
                Relationship.RelationshipType type = Relationship.RelationshipType.valueOf(
                        rs.getString("relationship_type")
                );
                relationships.add(new Relationship(user.getId(), targetUserId, type));
            }
            user.setRelationships(relationships);
        }
    }

    private void loadPhotoReactions(Photo photo) throws SQLException {
        String sql = "SELECT user_id, reaction_type FROM photo_reactions WHERE photo_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, photo.getId());
            ResultSet rs = stmt.executeQuery();

            List<Integer> likes = new ArrayList<>();
            List<Integer> dislikes = new ArrayList<>();

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String reactionType = rs.getString("reaction_type");

                if ("LIKE".equals(reactionType)) {
                    likes.add(userId);
                } else if ("DISLIKE".equals(reactionType)) {
                    dislikes.add(userId);
                }
            }

            photo.setLikes(likes);
            photo.setDislikes(dislikes);
        }
    }
}