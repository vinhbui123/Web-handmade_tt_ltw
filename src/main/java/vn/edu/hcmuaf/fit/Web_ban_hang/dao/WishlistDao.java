package vn.edu.hcmuaf.fit.Web_ban_hang.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.db.DBConnect;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WishlistDao {
    private static final Logger log = LoggerFactory.getLogger(WishlistDao.class);

    // Thêm sản phẩm vào danh sách yêu thích
    public boolean add(int userId, int productId) {
        String query = "INSERT IGNORE INTO wishlist (user_id, product_id) VALUES (?, ?)";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Lỗi khi thêm vào wishlist: {}", e.getMessage(), e);
        }
        return false;
    }

    // Xóa sản phẩm khỏi danh sách yêu thích
    public boolean remove(int userId, int productId) {
        String query = "DELETE FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("Lỗi khi xóa khỏi wishlist: {}", e.getMessage(), e);
        }
        return false;
    }

    // Kiểm tra sản phẩm đã có trong wishlist chưa
    public boolean exists(int userId, int productId) {
        String query = "SELECT 1 FROM wishlist WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            log.error("Lỗi khi kiểm tra wishlist: {}", e.getMessage(), e);
        }
        return false;
    }

    // Lấy danh sách product IDs trong wishlist của user (dùng để render heart icon)
    public Set<Integer> getProductIdsByUserId(int userId) {
        Set<Integer> ids = new HashSet<>();
        String query = "SELECT product_id FROM wishlist WHERE user_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("product_id"));
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi khi lấy danh sách wishlist IDs: {}", e.getMessage(), e);
        }
        return ids;
    }

    // Lấy danh sách sản phẩm trong wishlist (JOIN với bảng products)
    public List<Product> getProductsByUserId(int userId) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.id, p.name, p.price, p.discount, p.view, p.img, p.weight " +
                "FROM wishlist w " +
                "JOIN products p ON w.product_id = p.id " +
                "WHERE w.user_id = ? " +
                "ORDER BY w.created_at DESC";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setName(rs.getString("name"));
                    p.setPrice(rs.getInt("price"));
                    p.setDiscount(rs.getInt("discount"));
                    p.setView(rs.getInt("view"));
                    p.setImg(rs.getString("img"));
                    p.setWeight(rs.getInt("weight"));
                    products.add(p);
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi khi lấy danh sách sản phẩm wishlist: {}", e.getMessage(), e);
        }
        return products;
    }

    // Đếm số sản phẩm trong wishlist
    public int countByUserId(int userId) {
        String query = "SELECT COUNT(*) FROM wishlist WHERE user_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Lỗi khi đếm wishlist: {}", e.getMessage(), e);
        }
        return 0;
    }
}
