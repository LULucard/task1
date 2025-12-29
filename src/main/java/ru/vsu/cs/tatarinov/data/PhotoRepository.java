package ru.vsu.cs.tatarinov.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PhotoRepository {

    public int save(Photo photo) {
        String sql = "INSERT INTO photos (user_id, file_name, file_data, file_size, mime_type) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, photo.getUserId());
            stmt.setString(2, photo.getFileName());
            stmt.setBytes(3, photo.getFileData());
            stmt.setInt(4, photo.getFileSize());
            stmt.setString(5, photo.getMimeType());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new SQLException("Failed to get photo ID");

        } catch (SQLException e) {
            throw new RuntimeException("Error saving photo", e);
        }
    }

    public Optional<Photo> findById(int id) {
        String sql = "SELECT * FROM photos WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapPhoto(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding photo by ID", e);
        }

        return Optional.empty();
    }

    public List<Photo> findByUserId(int userId) {
        List<Photo> photos = new ArrayList<>();
        String sql = "SELECT * FROM photos WHERE user_id = ? ORDER BY uploaded_at DESC";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                photos.add(mapPhoto(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding photos by user ID", e);
        }

        return photos;
    }

    public byte[] getFileData(int photoId) {
        String sql = "SELECT file_data FROM photos WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, photoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBytes("file_data");
            }

            return new byte[0];

        } catch (SQLException e) {
            throw new RuntimeException("Error getting photo data", e);
        }
    }

    public boolean delete(int photoId) {
        String sql = "DELETE FROM photos WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, photoId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting photo", e);
        }
    }

    public boolean deleteByUserId(int userId) {
        String sql = "DELETE FROM photos WHERE user_id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user photos", e);
        }
    }

    private Photo mapPhoto(ResultSet rs) throws SQLException {
        return new Photo(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("file_name"),
                rs.getBytes("file_data"),
                rs.getInt("file_size"),
                rs.getString("mime_type")
        );
    }
}