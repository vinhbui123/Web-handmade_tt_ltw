package vn.edu.hcmuaf.fit.Web_ban_hang.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.db.DBConnect;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Order;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.OrderDetail;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDao {

    private static final Logger log = LoggerFactory.getLogger(OrderDao.class);

    // Lấy danh sách tất cả đơn hàng theo uid
    public List<Order> getAllOrders(int uid) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE user_id = ?";

        try (Connection connection = DBConnect.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, uid);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setStatus(rs.getByte("status"));
                o.setUserId(rs.getInt("user_id"));
                o.setShippingFee(rs.getInt("shipping_fee"));
                o.setPaymentTypeId(rs.getInt("payment_type_id"));
                orders.add(o);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return orders;
    }

    // Thêm đơn hàng và chi tiết vào database
    public void addOrder(Order order, List<OrderDetail> details) {
        String query = "INSERT INTO orders (status, user_id, shipping_fee, payment_type_id) VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnect.getConnection()) { // chỉ mở 1 connection
            try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, order.getStatus());
                statement.setInt(2, order.getUserId());
                statement.setInt(3, order.getShippingFee());
                statement.setInt(4, order.getPaymentTypeId());
                statement.executeUpdate();

                // Lấy order_id vừa tạo
                ResultSet generatedKeys = statement.getGeneratedKeys();
                int orderId = -1;
                if (generatedKeys.next()) {
                    orderId = generatedKeys.getInt(1);
                    order.setId(orderId); // Đưa ID ngược lại object
                } else {
                    throw new SQLException("Không lấy được order_id!");
                }

                // Thêm các chi tiết đơn hàng
                addDetailsOrder(connection, details, orderId, order.getStatus());
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    private void addDetailsOrder(Connection connection, List<OrderDetail> details, int orderId, int status) {
        String query = "INSERT INTO order_details(order_id, product_id, price, quantity, total_money, discount_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (OrderDetail detail : details) {
                statement.setInt(1, orderId);
                statement.setInt(2, detail.getProductId());
                statement.setInt(3, detail.getPrice());
                statement.setInt(4, detail.getQuantity());
                statement.setInt(5, detail.getTotalMoney());
                statement.setInt(6, detail.getDiscountAmount());
                statement.setInt(7, status);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public List<Map<String, Object>> getAllOrdersForAdmin() {
        List<Map<String, Object>> result = new ArrayList<>();

        String query = """
            SELECT 
                o.id AS order_id,
                u.username,
                p.id AS product_id,
                p.name AS product_name,
                od.quantity,
                od.total_money,
                od.discount_amount,
                o.shipping_fee,
                o.status,
                o.create_at,
                o.updated_at,
                pt.payment_name AS payment_method,
                pt.payment_code AS payment_code
               FROM orders o
            JOIN users u ON o.user_id = u.id
            JOIN order_details od ON o.id = od.order_id
            JOIN products p ON od.product_id = p.id
            JOIN payment_types pt ON o.payment_type_id = pt.id
            ORDER BY o.id DESC
        """;

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("order_id", rs.getInt("order_id"));
                row.put("username", rs.getString("username"));
                row.put("product_id", rs.getInt("product_id"));
                row.put("product_name", rs.getString("product_name"));
                row.put("quantity", rs.getInt("quantity"));
                row.put("total_money", rs.getInt("total_money"));
                row.put("discount_amount", rs.getInt("discount_amount"));
                row.put("shipping_fee", rs.getInt("shipping_fee"));
                row.put("status", rs.getByte("status"));
                row.put("create_at", rs.getTimestamp("create_at"));
                row.put("updated_at", rs.getTimestamp("updated_at"));
                row.put("payment_method", rs.getString("payment_method"));
                row.put("payment_code", rs.getString("payment_code"));
                result.add(row);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }

        return result;
    }


    public boolean cancelOrder(int orderId, int userId) {
        String updateOrderSql = "UPDATE orders SET status = 4 WHERE id = ? AND status IN (0, 1)";
        String selectDetailsSql = "SELECT product_id, quantity FROM order_details WHERE order_id = ?";

        Connection conn = null;
        try {
            conn = DBConnect.getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            // Bước 1: Lấy danh sách sản phẩm trong đơn
            Map<Integer, Integer> productMap = new HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(selectDetailsSql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int productId = rs.getInt("product_id");
                        int quantity = rs.getInt("quantity");
                        productMap.put(productId, quantity);
                    }
                }
            }

            // Bước 2: Cập nhật trạng thái đơn hàng
            try (PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
                ps.setInt(1, orderId);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false; // không cập nhật được trạng thái
                }
            }

            // Bước 3: Hoàn lại kho và ghi log hủy từng sản phẩm
            InventoryDao inventoryDao = new InventoryDao();
            for (Map.Entry<Integer, Integer> entry : productMap.entrySet()) {
                int productId = entry.getKey();
                int quantity = entry.getValue();

                boolean stockOk = inventoryDao.updateInventory(conn, productId, quantity, 0);
                boolean transOk = inventoryDao.insertTransaction(conn, productId, userId, quantity, "cancel");

                if (!stockOk || !transOk) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit(); // tất cả OK
            return true;

        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            log.error(e.getMessage());
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
        return false;
    }

    public boolean confirmOrder(int orderId) {
        // Chỉ cho phép xác nhận nếu đơn đang ở trạng thái 0
        String sql = "UPDATE orders SET status = 1, updated_at = NOW() WHERE id = ? AND status = 0";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }
    // Hàm tạo yêu cầu hoàn trả đơn hàng
    public boolean createReturnRequest(int orderId, String reason, String description, String proofImg) {
        String insertReturn = "INSERT INTO return_requests (order_id, reason, description, proof_img, status) VALUES (?, ?, ?, ?, 0)";
        // Chuyển trạng thái đơn hàng sang 5 (Đang yêu cầu hoàn trả)
        String updateOrderStatus = "UPDATE orders SET status = 5 WHERE id = ?";

        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction để đảm bảo tính toàn vẹn dữ liệu

            // Lưu yêu cầu vào bảng return_requests
            try (PreparedStatement stmt1 = conn.prepareStatement(insertReturn)) {
                stmt1.setInt(1, orderId);
                stmt1.setString(2, reason);
                stmt1.setString(3, description);
                stmt1.setString(4, proofImg);
                stmt1.executeUpdate();
            }

            // Cập nhật trạng thái đơn hàng trong bảng orders
            try (PreparedStatement stmt2 = conn.prepareStatement(updateOrderStatus)) {
                stmt2.setInt(1, orderId);
                stmt2.executeUpdate();
            }

            conn.commit(); // Thành công cả 2 thao tác thì mới lưu vào DB
            return true;
        } catch (SQLException e) {
            log.error("Lỗi khi tạo yêu cầu hoàn trả cho đơn hàng " + orderId + ": " + e.getMessage());
        }
        return false;
    }
    public Map<String, String> getReturnDetails(int orderId) {
        Map<String, String> details = new HashMap<>();
        String sql = "SELECT reason, description, proof_img FROM return_requests WHERE order_id = ? ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    details.put("reason", rs.getString("reason"));
                    details.put("description", rs.getString("description"));
                    details.put("proofImg", rs.getString("proof_img"));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return details;
    }

    // Xử lý quyết định của Admin: Chấp nhận hoặc Từ chối hoàn trả
    public boolean processReturnRequest(int orderId, String action) {
        int newOrderStatus = "accept".equals(action) ? 6 : 7;
        int returnRequestStatus = "accept".equals(action) ? 1 : 2;

        String updateOrder = "UPDATE orders SET status = ?, updated_at = NOW() WHERE id = ?";
        String updateReturn = "UPDATE return_requests SET status = ? WHERE order_id = ? AND status = 0";

        // Lấy chi tiết sản phẩm cần hoàn
        String selectDetails = "SELECT od.product_id, od.quantity, o.user_id FROM order_details od JOIN orders o ON od.order_id = o.id WHERE od.order_id = ?";

        // Lấy lý do hoàn trả để phân loại
        String selectReason = "SELECT reason FROM return_requests WHERE order_id = ? ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DBConnect.getConnection()) {
            conn.setAutoCommit(false);

            // Cập nhật trạng thái đơn
            try (PreparedStatement psOrder = conn.prepareStatement(updateOrder)) {
                psOrder.setInt(1, newOrderStatus);
                psOrder.setInt(2, orderId);
                psOrder.executeUpdate();
            }

            // Cập nhật trạng thái yêu cầu hoàn trả
            try (PreparedStatement psReturn = conn.prepareStatement(updateReturn)) {
                psReturn.setInt(1, returnRequestStatus);
                psReturn.setInt(2, orderId);
                psReturn.executeUpdate();
            }

            // HOÀN KHO (Chỉ chạy khi Chấp nhận)
            if ("accept".equals(action)) {

                // Lấy lý do hoàn trả
                String reason = "";
                try (PreparedStatement psReason = conn.prepareStatement(selectReason)) {
                    psReason.setInt(1, orderId);
                    try (ResultSet rsReason = psReason.executeQuery()) {
                        if (rsReason.next()) {
                            reason = rsReason.getString("reason");
                        }
                    }
                }

                // Kiểm tra xem hàng có bị hỏng không
                boolean isDamaged = "Sản phẩm bị lỗi, hỏng hóc do vận chuyển".equals(reason);

                // SQL cộng kho tương ứng
                String updateInventory = isDamaged ?
                        "UPDATE inventory SET quantity_damaged = quantity_damaged + ? WHERE product_id = ?" :
                        "UPDATE inventory SET quantity_returned = quantity_returned + ? WHERE product_id = ?";

                // Chọn loại giao dịch ghi vào lịch sử
                String transType = isDamaged ? "damaged" : "return";
                String insertTrans = "INSERT INTO inventory_transactions (product_id, user_id, quantity, type, created_at) VALUES (?, ?, ?, ?, NOW())";

                //Tiến hành cập nhật
                try (PreparedStatement psSelect = conn.prepareStatement(selectDetails)) {
                    psSelect.setInt(1, orderId);
                    try (ResultSet rs = psSelect.executeQuery()) {
                        try (PreparedStatement psInv = conn.prepareStatement(updateInventory);
                             PreparedStatement psTrans = conn.prepareStatement(insertTrans)) {

                            while (rs.next()) {
                                int productId = rs.getInt("product_id");
                                int qty = rs.getInt("quantity");
                                int userId = rs.getInt("user_id");

                                // Cập nhật kho (Hư hỏng hoặc Trả lại)
                                psInv.setInt(1, qty);
                                psInv.setInt(2, productId);
                                psInv.addBatch();

                                // Ghi log giao dịch với type tương ứng
                                psTrans.setInt(1, productId);
                                psTrans.setInt(2, userId);
                                psTrans.setInt(3, qty);
                                psTrans.setString(4, transType); // Ghi 'damaged' hoặc 'return'
                                psTrans.addBatch();
                            }

                            psInv.executeBatch();
                            psTrans.executeBatch();
                        }
                    }
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("LỖI SQL KHI HOÀN TRẢ ĐƠN HÀNG #" + orderId + ":");
            e.printStackTrace();
            return false;
        }
    }
    // 1. Đếm tổng số lượng đơn hàng
    public int getTotalOrdersCount() {
        String sql = "SELECT COUNT(id) FROM orders";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Lỗi đếm đơn hàng: " + e.getMessage());
        }
        return 0;
    }

    // 2. Lấy danh sách chi tiết đơn hàng THEO TRANG (Xử lý thông minh tránh cắt ngang đơn)
    public List<Map<String, Object>> getOrdersByPageForAdmin(int offset, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Integer> orderIds = new ArrayList<>();

        // Bước A: Lấy danh sách ID đơn hàng cho trang hiện tại
        String sqlIds = "SELECT id FROM orders ORDER BY id DESC LIMIT ? OFFSET ?";
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlIds)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orderIds.add(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi lấy ID đơn hàng: " + e.getMessage());
        }

        if (orderIds.isEmpty()) return result;

        // Bước B: Lấy chi tiết các sản phẩm thuộc các ID vừa tìm được
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < orderIds.size(); i++) {
            placeholders.append("?");
            if (i < orderIds.size() - 1) placeholders.append(",");
        }

        String query = "SELECT o.id AS order_id, u.username, p.id AS product_id, p.name AS product_name, " +
                "od.quantity, od.total_money, od.discount_amount, o.shipping_fee, " +
                "o.status, o.create_at, o.updated_at, pt.payment_name AS payment_method, pt.payment_code AS payment_code " +
                "FROM orders o " +
                "JOIN users u ON o.user_id = u.id " +
                "JOIN order_details od ON o.id = od.order_id " +
                "JOIN products p ON od.product_id = p.id " +
                "JOIN payment_types pt ON o.payment_type_id = pt.id " +
                "WHERE o.id IN (" + placeholders.toString() + ") " +
                "ORDER BY o.id DESC";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            for (int i = 0; i < orderIds.size(); i++) {
                ps.setInt(i + 1, orderIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("order_id", rs.getInt("order_id"));
                    row.put("username", rs.getString("username"));
                    row.put("product_id", rs.getInt("product_id"));
                    row.put("product_name", rs.getString("product_name"));
                    row.put("quantity", rs.getInt("quantity"));
                    row.put("total_money", rs.getInt("total_money"));
                    row.put("discount_amount", rs.getInt("discount_amount"));
                    row.put("shipping_fee", rs.getInt("shipping_fee"));
                    row.put("status", rs.getByte("status"));
                    row.put("create_at", rs.getTimestamp("create_at"));
                    row.put("updated_at", rs.getTimestamp("updated_at"));
                    row.put("payment_method", rs.getString("payment_method"));
                    row.put("payment_code", rs.getString("payment_code"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi lấy chi tiết đơn hàng: " + e.getMessage());
        }
        return result;
    }
    // Đếm tổng số lượng đơn hàng (lọc và tìm kiếm)
    public int getTotalOrdersCountUnified(String keyword, Integer statusFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(o.id) FROM orders o JOIN users u ON o.user_id = u.id WHERE 1=1 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (u.username LIKE ? OR o.id = ?) ");
        }
        if (statusFilter != null && statusFilter >= 0) {
            sql.append(" AND o.status = ? ");
        }

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + keyword.trim() + "%");
                int searchId = -1;
                try { searchId = Integer.parseInt(keyword.trim()); } catch (Exception e) {}
                ps.setInt(paramIndex++, searchId);
            }
            if (statusFilter != null && statusFilter >= 0) {
                ps.setInt(paramIndex++, statusFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error("Lỗi đếm đơn hàng Unified: " + e.getMessage());
        }
        return 0;
    }

    // Lấy danh sách chi tiết đơn hàng THEO TRANG + TÌM KIẾM + LỌC TRẠNG THÁI
    public List<Map<String, Object>> getOrdersUnified(String keyword, Integer statusFilter, int offset, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Integer> orderIds = new ArrayList<>();

        // Tìm các ID đơn hàng khớp với điều kiện tìm kiếm/lọc
        StringBuilder sqlIds = new StringBuilder("SELECT o.id FROM orders o JOIN users u ON o.user_id = u.id WHERE 1=1 ");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sqlIds.append(" AND (u.username LIKE ? OR o.id = ?) ");
        }
        if (statusFilter != null && statusFilter >= 0) {
            sqlIds.append(" AND o.status = ? ");
        }
        sqlIds.append(" ORDER BY o.id DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlIds.toString())) {

            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + keyword.trim() + "%");
                int searchId = -1;
                try { searchId = Integer.parseInt(keyword.trim()); } catch (Exception e) {}
                ps.setInt(paramIndex++, searchId);
            }
            if (statusFilter != null && statusFilter >= 0) {
                ps.setInt(paramIndex++, statusFilter);
            }
            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex++, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orderIds.add(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi lấy ID đơn hàng Unified: " + e.getMessage());
        }

        if (orderIds.isEmpty()) return result;

        // Lấy chi tiết các sản phẩm thuộc các ID vừa tìm được
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < orderIds.size(); i++) {
            placeholders.append("?");
            if (i < orderIds.size() - 1) placeholders.append(",");
        }

        String query = "SELECT o.id AS order_id, u.username, p.id AS product_id, p.name AS product_name, " +
                "od.quantity, od.total_money, od.discount_amount, o.shipping_fee, " +
                "o.status, o.create_at, o.updated_at, pt.payment_name AS payment_method, pt.payment_code AS payment_code " +
                "FROM orders o " +
                "JOIN users u ON o.user_id = u.id " +
                "JOIN order_details od ON o.id = od.order_id " +
                "JOIN products p ON od.product_id = p.id " +
                "JOIN payment_types pt ON o.payment_type_id = pt.id " +
                "WHERE o.id IN (" + placeholders.toString() + ") " +
                "ORDER BY o.id DESC";

        try (Connection conn = DBConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            for (int i = 0; i < orderIds.size(); i++) {
                ps.setInt(i + 1, orderIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("order_id", rs.getInt("order_id"));
                    row.put("username", rs.getString("username"));
                    row.put("product_id", rs.getInt("product_id"));
                    row.put("product_name", rs.getString("product_name"));
                    row.put("quantity", rs.getInt("quantity"));
                    row.put("total_money", rs.getInt("total_money"));
                    row.put("discount_amount", rs.getInt("discount_amount"));
                    row.put("shipping_fee", rs.getInt("shipping_fee"));
                    row.put("status", rs.getByte("status"));
                    row.put("create_at", rs.getTimestamp("create_at"));
                    row.put("updated_at", rs.getTimestamp("updated_at"));
                    row.put("payment_method", rs.getString("payment_method"));
                    row.put("payment_code", rs.getString("payment_code"));
                    result.add(row);
                }
            }
        } catch (SQLException e) {
            log.error("Lỗi lấy chi tiết đơn hàng Unified: " + e.getMessage());
        }
        return result;
    }
}
