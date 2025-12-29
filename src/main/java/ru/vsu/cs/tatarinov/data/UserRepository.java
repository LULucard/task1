package ru.vsu.cs.tatarinov.data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public int create(User user, String passwordHash) {
        String sql = "INSERT INTO users (name, gender, age, zodiac_sign, login, password_hash) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getGender());
            stmt.setInt(3, user.getAge());
            stmt.setString(4, user.getZodiacSign());
            stmt.setString(5, user.getLogin());
            stmt.setString(6, passwordHash);

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

            throw new SQLException("Failed to get user ID");

        } catch (SQLException e) {
            throw new RuntimeException("Error creating user", e);
        }
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by ID", e);
        }

        return Optional.empty();
    }

    public Optional<User> findByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by login", e);
        }

        return Optional.empty();
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        }

        return users;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public boolean validateCredentials(String login, String passwordHash) {
        String sql = "SELECT id FROM users WHERE login = ? AND password_hash = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, passwordHash);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error validating credentials", e);
        }
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET name = ?, gender = ?, age = ?, zodiac_sign = ? WHERE id = ?";

        try (DatabaseConnection db = DatabaseConfig.getConnection();
             PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getGender());
            stmt.setInt(3, user.getAge());
            stmt.setString(4, user.getZodiacSign());
            stmt.setInt(5, user.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("gender"),
                rs.getInt("age"),
                rs.getString("zodiac_sign"),
                rs.getString("login")
        );
    }
}
