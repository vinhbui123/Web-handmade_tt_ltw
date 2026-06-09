package vn.edu.hcmuaf.fit.Web_ban_hang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vn.edu.hcmuaf.fit.Web_ban_hang.db.DBConnect;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Coupon;

public class CouponDao {

    private static final Logger log = LoggerFactory.getLogger(CouponDao.class);

    public Coupon getByCode(String code) {
        String sql = "SELECT * FROM coupons WHERE code = ? AND NOW() BETWEEN start_date AND end_date";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCoupon(rs);
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi tìm Coupon theo code: {}", code, e);
        }
        return null;
    }

    public List<Coupon> getAllValid() {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM coupons WHERE NOW() BETWEEN start_date AND end_date";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                coupons.add(extractCoupon(rs));
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách Coupon hợp lệ", e);
        }

        return coupons;
    }

    private Coupon extractCoupon(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setType(rs.getInt("type"));
        int discountValue = rs.getInt("discount_value");
        c.setDiscountValue(rs.wasNull() ? null : discountValue);
        int discountPercent = rs.getInt("discount_percent");
        c.setDiscountPercent(rs.wasNull() ? null : discountPercent);
        c.setMinOrderAmount(rs.getInt("min_order_amount"));

        int maxValue = rs.getInt("max_discount_value");
        c.setMaxDiscountValue(rs.wasNull() ? null : maxValue);

        Timestamp start = rs.getTimestamp("start_date");
        if (start != null) {
            c.setStartDate(start.toLocalDateTime());
        }

        Timestamp end = rs.getTimestamp("end_date");
        if (end != null) {
            c.setEndDate(end.toLocalDateTime());
        }

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            c.setCreatedAt(created.toLocalDateTime());
        }

        return c;
    }

    public List<Coupon> getAllCoupons() {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM coupons ORDER BY id DESC"; 

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                coupons.add(extractCoupon(rs));
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy toàn bộ danh sách Coupon cho Admin", e);
        }
        return coupons;
    }

    public boolean addCoupon(Coupon c) {
        String sql = "INSERT INTO coupons (code, type, discount_value, discount_percent, max_discount_value, min_order_amount, start_date, end_date, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getCode());
            stmt.setInt(2, c.getType());

            if (c.getType() == 0) {
                stmt.setInt(3, c.getDiscountValue());
                stmt.setNull(4, java.sql.Types.INTEGER); 
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
                stmt.setInt(4, c.getDiscountPercent()); 
            }

            if (c.getMaxDiscountValue() != null && c.getMaxDiscountValue() > 0) {
                stmt.setInt(5, c.getMaxDiscountValue());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            stmt.setInt(6, c.getMinOrderAmount());
            stmt.setTimestamp(7, Timestamp.valueOf(c.getStartDate()));
            stmt.setTimestamp(8, Timestamp.valueOf(c.getEndDate()));

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Lỗi khi thêm Coupon mới", e);
        }
        return false;
    }

    public boolean updateCoupon(Coupon c) {
        String sql = "UPDATE coupons SET code=?, type=?, discount_value=?, discount_percent=?, max_discount_value=?, min_order_amount=?, start_date=?, end_date=? WHERE id=?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, c.getCode());
            stmt.setInt(2, c.getType());

            if (c.getType() == 0) {
                stmt.setInt(3, c.getDiscountValue());
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
                stmt.setInt(4, c.getDiscountPercent());
            }

            if (c.getMaxDiscountValue() != null && c.getMaxDiscountValue() > 0) {
                stmt.setInt(5, c.getMaxDiscountValue());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }

            stmt.setInt(6, c.getMinOrderAmount());
            stmt.setTimestamp(7, Timestamp.valueOf(c.getStartDate()));
            stmt.setTimestamp(8, Timestamp.valueOf(c.getEndDate()));
            stmt.setInt(9, c.getId());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật Coupon ID: " + c.getId(), e);
        }
        return false;
    }

    public boolean deleteCoupon(int id) {
        String sql = "DELETE FROM coupons WHERE id=?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            log.error("Lỗi khi xóa Coupon ID: " + id, e);
        }
        return false;
    }
}
