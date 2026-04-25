package vn.edu.hcmuaf.fit.Web_ban_hang.dao;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.db.DBConnect;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.HashUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);
    private static UserDao instance;

    public static UserDao getInstance() {
        if (instance == null)
            instance = new UserDao();
        return instance;
    }

    public boolean isEmailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email.trim().toLowerCase());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean isUsernameExists(String username) {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public User getById(int id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, id);
            return fetchSingleUser(statement);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        return fetchUserByStringParam(username, query);
    }

    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        return fetchUserByStringParam(email, query);
    }

    // HELPER: Fetches a user based on a single string parameter (username or email)
    private User fetchUserByStringParam(String paramValue, String query) {
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, paramValue);
            return fetchSingleUser(statement);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    // HELPER: Executes the statement and maps the first result to a User
    private User fetchSingleUser(PreparedStatement statement) throws SQLException {
        try (ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return users;
    }

    // HELPER: Maps a single ResultSet row to a User object
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setStatus(rs.getInt("status"));
        user.setAddress(rs.getString("address"));
        user.setBio(rs.getString("bio"));
        user.setAvatar(rs.getString("avatar"));
        user.setRole(rs.getInt("role"));
        user.setPassword(rs.getString("password"));
        user.setAuthProvider(rs.getString("auth_provider"));
        return user;
    }

    public User authenticateUser(String username, String currentPassword) {
        String query = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String dbPassword = rs.getString("password");

                    if (StringUtils.isNotBlank(dbPassword)) {
                        String inputHash = HashUtil.toSHA256(currentPassword);

                        if (dbPassword.equals(inputHash)) {
                            return mapRowToUser(rs);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error during authentication: {}", e.getMessage(), e);
        }

        log.info("Authentication failed for: {}", username);
        return null;
    }

    public boolean registerUser(User user) {
        String query = "INSERT INTO users (username, password, first_name, last_name, avatar, birthday, email, phone_number, address, role, status, bio, create_at, update_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            int roleId = user.getRole() != 0 ? user.getRole() : 0;

            statement.setString(1, user.getUsername());
            statement.setString(2, HashUtil.toSHA256(user.getPassword()));
            statement.setString(3, user.getFirstName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getAvatar());
            statement.setDate(6, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            statement.setString(7, user.getEmail());
            statement.setString(8, user.getPhoneNumber());
            statement.setString(9, user.getAddress());
            statement.setInt(10, roleId);
            statement.setInt(11, user.getStatus());
            statement.setString(12, user.getBio());
            statement.setTimestamp(13, now);
            statement.setTimestamp(14, now);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean insertSocialUser(User user) {
        String query = "INSERT INTO users (email, username, first_name, last_name, role, status, password, create_at, update_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            setBasicUserParams(user, statement);

            String randomPassHash = HashUtil.toSHA256(UUID.randomUUID().toString());
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            statement.setString(7, randomPassHash);
            statement.setTimestamp(8, now);
            statement.setTimestamp(9, now);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean insertGoogleUser(User user) {
        String sql = "INSERT INTO users (email, username, first_name, last_name, role, status, auth_provider, password, create_at, update_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setBasicUserParams(user, stmt);

            String randomPassword = UUID.randomUUID().toString();
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            stmt.setString(7, user.getAuthProvider());
            stmt.setString(8, HashUtil.toSHA256(randomPassword));
            stmt.setTimestamp(9, now);
            stmt.setTimestamp(10, now);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    // HELPER: Sets common parameters used in social/Google inserts
    private void setBasicUserParams(User user, PreparedStatement stmt) throws SQLException {
        stmt.setString(1, user.getEmail());
        stmt.setString(2, user.getUsername());
        stmt.setString(3, user.getFirstName());
        stmt.setString(4, user.getLastName());
        stmt.setInt(5, user.getRole());
        stmt.setInt(6, user.getStatus());
    }

    public boolean updateUser(User user) {
        String query = "UPDATE users SET first_name = ?, last_name = ?, phone_number = ?, address = ?, bio = ?, avatar = ? WHERE username = ?";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getPhoneNumber());
            statement.setString(4, user.getAddress());
            statement.setString(5, user.getBio());
            statement.setString(6, user.getAvatar());
            statement.setString(7, user.getUsername());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean updatePassword(String username, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE username = ?";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, HashUtil.toSHA256(newPassword));
            statement.setString(2, username);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean updateUserRoleAndStatus(int userId, int newRole, int newStatus) {
        String query = "UPDATE users SET role = ?, status = ? WHERE id = ?";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, newRole);
            statement.setInt(2, newStatus);
            statement.setInt(3, userId);

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    public String getLoggedSessionId(int userId) {
        String query = "SELECT logged_session_id FROM users WHERE id = ?";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("logged_session_id");
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public void insertFacebookUser(User user) {
        String sql = "INSERT INTO users (email, username, first_name, last_name, role, status, auth_provider, password, create_at, update_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String randomPassword = UUID.randomUUID().toString();
            user.setPassword(HashUtil.toSHA256(randomPassword));

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            user.setRole(0);
            stmt.setInt(5, user.getRole());
            stmt.setInt(6, user.getStatus());
            stmt.setString(7, user.getAuthProvider());
            stmt.setString(8, user.getPassword());
            stmt.setTimestamp(9, now);
            stmt.setTimestamp(10, now);

            stmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void updateAuthProvider(String email, String provider) {
        String sql = "UPDATE users SET auth_provider = ? WHERE email = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, provider);
            stmt.setString(2, email);

            stmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}