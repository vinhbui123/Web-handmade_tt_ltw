package vn.edu.hcmuaf.fit.Web_ban_hang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import vn.edu.hcmuaf.fit.Web_ban_hang.db.DBConnect;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.session.CartProduct;

public class CartDbDao {

    public Map<Integer, CartProduct> getCartByUserId(int userId) {
        Map<Integer, CartProduct> dbCart = new HashMap<>();
        String sql = "SELECT c.product_id, c.quantity, c.selected, p.name, p.price, p.discount, p.img " +
                "FROM cart_items c LEFT JOIN products p ON c.product_id = p.id WHERE c.user_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartProduct cp = new CartProduct();
                    int pId = rs.getInt("product_id");
                    cp.setId(pId);

                    String pName = rs.getString("name");
                    cp.setName(pName != null ? pName : "Sản phẩm đã bị xóa hoặc không tồn tại");

                    cp.setPrice(rs.getInt("price"));
                    cp.setDiscount(rs.getInt("discount"));
                    cp.setImg(rs.getString("img"));
                    cp.setQuantity(rs.getInt("quantity"));
                    cp.setSelected(rs.getBoolean("selected"));
                    dbCart.put(pId, cp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dbCart;
    }

    public void saveOrUpdate(int userId, int productId, int quantity) {
        String sql = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setInt(4, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateQuantity(int userId, int productId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSelection(int userId, int productId, boolean selected) {
        String sql = "UPDATE cart_items SET selected = ? WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, selected);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void toggleAllSelection(int userId, boolean selected) {
        String sql = "UPDATE cart_items SET selected = ? WHERE user_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, selected);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(int userId, int productId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ? AND product_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearCart(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}