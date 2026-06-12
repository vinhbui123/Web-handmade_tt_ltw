package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.db.DBConnect;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.OrderService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.ProductService;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "ReorderController", urlPatterns = "/reorder")
public class ReorderController extends HttpServlet {
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();

        HttpSession session = request.getSession();
        if (session.getAttribute("user") == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập lại để thực hiện!");
            out.print(gson.toJson(result));
            return;
        }

        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            List<int[]> items = new ArrayList<>();
            String sql = "SELECT product_id, quantity FROM order_details WHERE order_id = ?";
            try (Connection conn = DBConnect.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        items.add(new int[]{rs.getInt("product_id"), rs.getInt("quantity")});
                    }
                }
            }

            if (items.isEmpty()) {
                result.put("success", false);
                result.put("message", "Không tìm thấy sản phẩm nào trong đơn hàng này!");
                out.print(gson.toJson(result));
                return;
            }

            CartService cart = (CartService) session.getAttribute("cart");
            if (cart == null) {
                cart = new CartService();
                session.setAttribute("cart", cart);
            }

            List<String> outOfStockItems = new ArrayList<>();
            int addedCount = 0;

            for (int[] item : items) {
                int productId = item[0];
                int quantity = item[1];

                Product product = productService.getById(productId);
                if (product != null) {
                    int currentQtyInCart = 0;
                    for (var cartItem : cart.getList()) {
                        if (cartItem.getId() == productId) {
                            currentQtyInCart = cartItem.getQuantity();
                            break;
                        }
                    }

                    if (orderService.CheckStock(productId, currentQtyInCart + quantity)) {
                        outOfStockItems.add(product.getName());
                    } else {
                        cart.add(product, quantity);
                        addedCount++;
                    }
                }
            }

            if (addedCount == 0) {
                result.put("success", false);
                result.put("message", "Rất tiếc, tất cả sản phẩm trong đơn hàng này đều đã hết hàng!");
            } else if (!outOfStockItems.isEmpty()) {
                result.put("success", true);
                result.put("message", "Đã thêm vào giỏ. Tuy nhiên có món đã hết hàng: " + String.join(", ", outOfStockItems));
            } else {
                result.put("success", true);
                result.put("message", "Đã thêm toàn bộ sản phẩm vào giỏ hàng thành công!");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi máy chủ: " + e.getMessage());
        }

        out.print(gson.toJson(result));
        out.flush();
    }
}