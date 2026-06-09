package vn.edu.hcmuaf.fit.Web_ban_hang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.edu.hcmuaf.fit.Web_ban_hang.db.DBConnect;

public class InventoryDao {

    private static final Logger log = LoggerFactory.getLogger(InventoryDao.class);

    public int getStock(int productId) {
        String sql = "SELECT quantity FROM inventory WHERE product_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                } else {
                    throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId);
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi khi lấy tồn kho cho product: {}", productId, e);
            throw new RuntimeException("Lỗi kết nối cơ sở dữ liệu", e);
        }
    }
    public boolean updateInventory(Connection conn, int productId, int quantityIn, int quantityOut) {

        String sql = """
            INSERT INTO inventory (product_id, quantity_in, quantity_out)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                quantity_in = quantity_in + ?,
                quantity_out = quantity_out + ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, quantityIn);
            stmt.setInt(3, quantityOut);
            stmt.setInt(4, quantityIn);
            stmt.setInt(5, quantityOut);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Lỗi SQL tại updateInventory:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertTransaction(Connection conn, int productId, int userId, int quantity, String type) {
        String sql = "INSERT INTO inventory_transactions (product_id, user_id, quantity, type, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setInt(2, userId);
            stmt.setInt(3, quantity);
            stmt.setString(4, type);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Cảnh báo: Không thể lưu lịch sử giao dịch (Có thể bảng inventory_transactions chưa được tạo). Bỏ qua bước này.");
            return false;
        }
    }

    public boolean importProduct(int productId, int quantity, int userId) {
        Connection conn = null;
        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false);

            boolean transOk = insertTransaction(conn, productId, userId, quantity, "import");
            boolean updateOk = updateInventory(conn, productId, quantity, 0);

            System.out.println("updateInventory: " + updateOk);

            if (!transOk || !updateOk) {
                System.out.println("Một trong hai thao tác thất bại!");
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            if (conn != null) try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean exportProduct(int productId, int quantity, int userId, String type) {
        Connection conn = null;
        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false);

            int stock = getStockForUpdate(conn, productId);
            if (stock < quantity) {
                System.out.println("Không đủ hàng! (Tồn: " + stock + ", Yêu cầu: " + quantity + ")");
                conn.rollback();
                return false;
            }

            boolean updateOk = updateInventory(conn, productId, 0, quantity);
            if (!updateOk) {
                conn.rollback();
                return false;
            }

            insertTransaction(conn, productId, userId, quantity, type);
            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
        return false;
    }

    public int getStockForUpdate(Connection conn, int productId) throws SQLException {
        String sql = "SELECT quantity FROM inventory WHERE product_id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                } else {
                    throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId);
                }
            }
        }
    }

    public List<Map<String, Object>> getTransactionHistory() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
                    SELECT t.id, p.name AS product_name, t.type, t.quantity, 
                           u.username, u.first_name, u.last_name, u.role, t.created_at
                    FROM inventory_transactions t
                    JOIN products p ON t.product_id = p.id
                    JOIN users u ON t.user_id = u.id
                    ORDER BY t.created_at DESC
                """;
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("product_name", rs.getString("product_name"));
                map.put("type", rs.getString("type"));
                map.put("quantity", rs.getInt("quantity"));
                map.put("username", rs.getString("username"));
                map.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                map.put("role", rs.getInt("role"));
                map.put("created_at", rs.getTimestamp("created_at"));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}