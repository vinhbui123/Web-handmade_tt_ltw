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

    // Lấy Coupon dựa trên Code và ĐANG TRONG THỜI GIAN CÓ HIỆU LỰC
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

    // Lấy tất cả các Coupon đang có hiệu lực ngay lúc này
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

    // Hàm tiện ích để map dữ liệu từ ResultSet sang Object
    private Coupon extractCoupon(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setType(rs.getInt("type"));
        c.setDiscountValue(rs.getInt("discount_value"));
        c.setMinOrderAmount(rs.getInt("min_order_amount"));

        // Xử lý cột max_discount_value có thể bị NULL
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
}