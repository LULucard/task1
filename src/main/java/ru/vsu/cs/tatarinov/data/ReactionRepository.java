package ru.vsu.cs.tatarinov.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReactionRepository {

    public void saveReaction(int userId, int photoId, String reactionType) {
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
            throw new RuntimeException("Error saving reaction", e);
        }
    }

    public void deleteReaction(int userId, int photoId) {
        String sql = "DELETE FROM photo_reactions WHERE user_id = ? AND photo_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, photoId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting reaction", e);
        }
    }

    public List<Integer> getLikes(int photoId) {
        return getReactions(photoId, "LIKE");
    }

    public List<Integer> getDislikes(int photoId) {
        return getReactions(photoId, "DISLIKE");
    }

    private List<Integer> getReactions(int photoId, String reactionType) {
        List<Integer> userIds = new ArrayList<>();
        String sql = "SELECT user_id FROM photo_reactions WHERE photo_id = ? AND reaction_type = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, photoId);
            stmt.setString(2, reactionType);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                userIds.add(rs.getInt("user_id"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error getting " + reactionType.toLowerCase() + "s", e);
        }

        return userIds;
    }

    public boolean deleteByPhoto(int photoId) {
        String sql = "DELETE FROM photo_reactions WHERE photo_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, photoId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting photo reactions", e);
        }
    }

    public boolean deleteByUser(int userId) {
        String sql = "DELETE FROM photo_reactions WHERE user_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user reactions", e);
        }
    }
}