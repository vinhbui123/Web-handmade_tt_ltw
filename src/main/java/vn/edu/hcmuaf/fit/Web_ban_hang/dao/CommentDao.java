package vn.edu.hcmuaf.fit.Web_ban_hang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.edu.hcmuaf.fit.Web_ban_hang.db.DBConnect;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Comment;

public class CommentDao {

    private static final Logger log = LoggerFactory.getLogger(CommentDao.class);

    public List<Comment> getCommentsByProductId(int productId) {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT c.*, u.username " +
                "FROM comments c " +
                "JOIN ( " +
                "    SELECT user_id, MAX(create_at) AS latest_time " +
                "    FROM comments " +
                "    WHERE product_id = ? " +
                "    GROUP BY user_id " +
                ") latest ON c.user_id = latest.user_id AND c.create_at = latest.latest_time " +
                "LEFT JOIN users u ON c.user_id = u.id " +
                "WHERE c.product_id = ? " +
                "ORDER BY c.create_at DESC";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, productId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Comment cmt = new Comment();
                cmt.setId(rs.getInt("id"));
                cmt.setProductId(rs.getInt("product_id"));
                cmt.setUserId(rs.getInt("user_id"));

                cmt.setContent(rs.getString("comment"));

                cmt.setRating(rs.getInt("rating"));

                cmt.setCreatedAt(rs.getTimestamp("create_at"));

                String username = rs.getString("username");
                cmt.setUserName(username);


                comments.add(cmt);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return comments;
    }

    public double getAverageRatingByProductId(int productId) {
        String query = "SELECT AVG(rating) AS avg FROM comments WHERE product_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public void addComment(Comment comment) {
        String updateQuery = "UPDATE comments SET comment = ?, rating = ?, create_at = ? " +
                "WHERE product_id = ? AND user_id = ?";

        String insertQuery = "INSERT INTO comments (product_id, user_id, comment, rating, create_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnect.getConnection()) {
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setString(1, comment.getContent());
                updateStmt.setInt(2, comment.getRating());
                updateStmt.setTimestamp(3, comment.getCreatedAt());
                updateStmt.setInt(4, comment.getProductId());
                updateStmt.setInt(5, comment.getUserId());

                int rowsAffected = updateStmt.executeUpdate();

                if (rowsAffected == 0) {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, comment.getProductId());
                        insertStmt.setInt(2, comment.getUserId());
                        insertStmt.setString(3, comment.getContent());
                        insertStmt.setInt(4, comment.getRating());
                        insertStmt.setTimestamp(5, comment.getCreatedAt());
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public List<Comment> getAllComments() {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.*, u.username FROM comments c LEFT JOIN users u ON c.user_id = u.id ORDER BY c.create_at DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Comment c = new Comment();
                c.setId(rs.getInt("id"));
                c.setProductId(rs.getInt("product_id"));
                c.setUserId(rs.getInt("user_id"));
                c.setRating(rs.getInt("rating"));
                c.setContent(rs.getString("comment"));
                c.setCreatedAt(rs.getTimestamp("create_at"));

                c.setUserName(rs.getString("username"));
                list.add(c);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return list;
    }

    public void deleteCommentById(int id) {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    // Đếm tổng số lượng comment
    public int getTotalCommentsCount() {
        String sql = "SELECT COUNT(*) FROM comments";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Lỗi đếm comment: " + e.getMessage());
        }
        return 0;
    }

    // Lấy danh sách comment theo trang
    public List<Comment> getCommentsByPage(int offset, int limit) {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.*, u.username FROM comments c LEFT JOIN users u ON c.user_id = u.id ORDER BY c.create_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment c = new Comment();
                    c.setId(rs.getInt("id"));
                    c.setProductId(rs.getInt("product_id"));
                    c.setUserId(rs.getInt("user_id"));
                    c.setRating(rs.getInt("rating"));
                    c.setContent(rs.getString("comment"));
                    c.setCreatedAt(rs.getTimestamp("create_at"));
                    c.setUserName(rs.getString("username"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi phân trang comment: " + e.getMessage());
        }
        return list;
    }
}