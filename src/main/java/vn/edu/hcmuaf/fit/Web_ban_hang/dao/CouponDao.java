package vn.edu.hcmuaf.fit.Web_ban_hang.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.db.DBConnect;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Coupon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CouponDao {

    private static final Logger log = LoggerFactory.getLogger(CouponDao.class);

    public Coupon getByCode(String code) {
        String sql = "SELECT * FROM coupons WHERE code = ? AND expired_at > NOW()";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractCoupon(rs);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public List<Coupon> getAllValid() {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT * FROM coupons WHERE expired_at > NOW()";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                coupons.add(extractCoupon(rs));
            }
        } catch (Exception e) {
            log.error("getAllValid", e);
        }

        return coupons;
    }

    private Coupon extractCoupon(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDescription(rs.getString("description"));
        c.setDiscountValue(rs.getInt("discount_value"));
        c.setDiscountPercent(rs.getInt("discount_percent"));
        c.setMinOrderAmount(rs.getInt("min_order_amount"));

        int maxValue = rs.getInt("max_discount_value");
        c.setMaxDiscountValue(rs.wasNull() ? null : maxValue);

        Timestamp expired = rs.getTimestamp("expired_at");
        if (expired != null) {
            c.setExpiredAt(expired.toLocalDateTime());
        }

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            c.setCreatedAt(created.toLocalDateTime());
        }

        return c;
    }
}