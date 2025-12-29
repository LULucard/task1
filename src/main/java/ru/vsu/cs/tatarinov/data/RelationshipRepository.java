package ru.vsu.cs.tatarinov.data;

import ru.vsu.cs.tatarinov.data.Relationship.RelationshipType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RelationshipRepository {

    public void save(Relationship relationship) {
        String sql = "INSERT INTO relationships (source_user_id, target_user_id, relationship_type) " +
                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE relationship_type = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, relationship.getSourceUserId());
            stmt.setInt(2, relationship.getTargetUserId());
            stmt.setString(3, relationship.getType().toString());
            stmt.setString(4, relationship.getType().toString());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error saving relationship", e);
        }
    }

    public Optional<Relationship> find(int sourceUserId, int targetUserId) {
        String sql = "SELECT * FROM relationships WHERE source_user_id = ? AND target_user_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, sourceUserId);
            stmt.setInt(2, targetUserId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRelationship(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding relationship", e);
        }

        return Optional.empty();
    }

    public List<Relationship> findBySourceUser(int sourceUserId) {
        List<Relationship> relationships = new ArrayList<>();
        String sql = "SELECT * FROM relationships WHERE source_user_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, sourceUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                relationships.add(mapRelationship(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding relationships by source user", e);
        }

        return relationships;
    }

    public List<Relationship> findByTargetUser(int targetUserId) {
        List<Relationship> relationships = new ArrayList<>();
        String sql = "SELECT * FROM relationships WHERE target_user_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, targetUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                relationships.add(mapRelationship(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding relationships by target user", e);
        }

        return relationships;
    }

    public boolean delete(int sourceUserId, int targetUserId) {
        String sql = "DELETE FROM relationships WHERE source_user_id = ? AND target_user_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, sourceUserId);
            stmt.setInt(2, targetUserId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting relationship", e);
        }
    }

    public boolean deleteByUser(int userId) {
        String sql = "DELETE FROM relationships WHERE source_user_id = ? OR target_user_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user relationships", e);
        }
    }

    private Relationship mapRelationship(ResultSet rs) throws SQLException {
        return new Relationship(
                rs.getInt("source_user_id"),
                rs.getInt("target_user_id"),
                RelationshipType.valueOf(rs.getString("relationship_type"))
        );
    }
}