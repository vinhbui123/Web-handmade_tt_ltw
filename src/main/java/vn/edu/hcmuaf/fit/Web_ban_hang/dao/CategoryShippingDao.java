package vn.edu.hcmuaf.fit.Web_ban_hang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import vn.edu.hcmuaf.fit.Web_ban_hang.db.DBConnect;

public class CategoryShippingDao {

    public int[] getShippingDefaults(int categoryId) {
        int[] defaults = {50, 10, 8, 5};

        String sql = "SELECT weight, length, width, height FROM category_shipping_defaults WHERE category_id = ?";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    defaults[0] = rs.getInt("weight");
                    defaults[1] = rs.getInt("length");
                    defaults[2] = rs.getInt("width");
                    defaults[3] = rs.getInt("height");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thông số vận chuyển mặc định: " + e.getMessage());
        }
        return defaults;
    }
}