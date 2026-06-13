package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.cart;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.CartDbDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.InventoryDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Coupon;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.OrderService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.ProductService;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.ReadJsonUtil;
import vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.product.ApplyCouponController;

@WebServlet(name = "CartController", urlPatterns = { "/cart", "/api/cart" })
public class CartController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final CartDbDao cartDbDao = new CartDbDao();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action != null) {
            handleFormRequest(request, response, user.getId());
            return;
        }

        // Đảm bảo load dữ liệu từ DB lên Session nếu Session trống
        CartService cart = (CartService) session.getAttribute("cart");
        if (cart == null) {
            cart = new CartService();
            cart.setData(cartDbDao.getCartByUserId(user.getId()));
            session.setAttribute("cart", cart);
        }

        // ==========================================================
        // LUỒNG UI/UX: TỰ ĐỘNG KIỂM TRA TỒN KHO, GIÁ CẢ VÀ TRẠNG THÁI
        // ==========================================================
        List<String> stockWarnings = new ArrayList<>();
        InventoryDao inventoryDao = new InventoryDao();

        List<vn.edu.hcmuaf.fit.Web_ban_hang.dao.session.CartProduct> itemsToCheck = new ArrayList<>(cart.getList());
        for (var item : itemsToCheck) {
            // Lấy thông tin sản phẩm mới nhất từ Database
            Product currentProduct = productService.getById(item.getId());

            // 1. Kiểm tra sản phẩm có bị Admin xóa hoặc ẩn đi không (Ví dụ: status == 0)
            if (currentProduct == null) {
                cart.remove(item.getId());
                cartDbDao.remove(user.getId(), item.getId());

                // ĐÃ SỬA: Bắt đúng chuỗi mặc định để đổi câu thông báo cho mượt mà
                if ("Sản phẩm đã bị xóa hoặc không tồn tại".equals(item.getName())) {
                    stockWarnings.add("- Một mặt hàng trong giỏ của bạn đã ngừng kinh doanh và được tự động gỡ bỏ.");
                } else {
                    stockWarnings.add("- Sản phẩm [" + item.getName() + "] đã ngừng kinh doanh và được gỡ khỏi giỏ.");
                }
                continue;
            }

            if (item.getPrice() != currentProduct.getPrice() || item.getDiscount() != currentProduct.getDiscount()) {
                item.setPrice(currentProduct.getPrice());
                item.setDiscount(currentProduct.getDiscount());
                stockWarnings.add("- Sản phẩm [" + item.getName() + "] đã có sự thay đổi về giá/khuyến mãi từ hệ thống.");
            }

            int currentStock = inventoryDao.getStock(item.getId());
            if (currentStock <= 0) {
                cart.remove(item.getId());
                cartDbDao.remove(user.getId(), item.getId());
                stockWarnings.add("- Sản phẩm [" + item.getName() + "] đã hết hàng và được gỡ khỏi giỏ.");
            } else if (item.getQuantity() > currentStock) {
                cart.update(item.getId(), currentStock);
                cartDbDao.updateQuantity(user.getId(), item.getId(), currentStock);
                stockWarnings.add("- Sản phẩm [" + item.getName() + "] chỉ còn " + currentStock + " sản phẩm, giỏ hàng đã tự động điều chỉnh.");
            }
        }

        if (!stockWarnings.isEmpty()) {
            String warningStr = String.join("\\n", stockWarnings);
            warningStr = warningStr.replace("'", "\\'");
            request.setAttribute("stockWarning", warningStr);
        }

        cart.refreshStock();
        double subtotal = cart.getSelectedTotalWithDiscount();
        double finalTotal = subtotal;
        int discount = 0;
        Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
        if (appliedCoupon != null) {
            if (subtotal >= appliedCoupon.getMinOrderAmount()) {
                discount = ApplyCouponController.getDiscountAmount(appliedCoupon, subtotal);
                finalTotal = subtotal - discount;
            } else {
                session.removeAttribute("appliedCoupon");
                request.setAttribute("couponWarning", "Mã giảm giá đã bị gỡ do đơn hàng không đủ điều kiện.");
            }
        }
        request.setAttribute("finalTotal", finalTotal);
        request.setAttribute("discountAmount", discount);

        request.getRequestDispatcher("cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String reqPath = request.getServletPath();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            if (reqPath.equals("/api/cart")) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                PrintWriter out = response.getWriter();
                Map<String, Object> result = new HashMap<>();
                result.put("status", false);
                result.put("message", "Bạn cần đăng nhập để thực hiện chức năng này!");
                result.put("redirect", "login.jsp");
                out.print(gson.toJson(result));
                out.flush();
            } else {
                response.sendRedirect("login.jsp");
            }
            return;
        }

        if (reqPath.equals("/api/cart")) {
            handleApiRequest(request, response, user.getId());
        } else {
            handleFormRequest(request, response, user.getId());
        }
    }

    private void handleApiRequest(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String jsonData = ReadJsonUtil.read(request);
            JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);

            String action = "add";
            if (jsonObject.has("action")) {
                action = jsonObject.get("action").getAsString();
            }

            HttpSession session = request.getSession();
            CartService cart = (CartService) session.getAttribute("cart");
            if (cart == null) {
                cart = new CartService();
                session.setAttribute("cart", cart);
            }

            Map<String, Object> result = new HashMap<>();

            if ("add".equals(action) || !jsonObject.has("action")) {
                int productId = jsonObject.get("productId").getAsInt();
                int quantity = jsonObject.has("quantity") ? jsonObject.get("quantity").getAsInt() : 1;

                Product product = productService.getById(productId);
                if (product != null) {
                    int totalQuantity = quantity;
                    for (var item : cart.getList()) {
                        if (item.getId() == productId) {
                            totalQuantity += item.getQuantity();
                            break;
                        }
                    }
                    if (orderService.CheckStock(productId, totalQuantity)) {
                        result.put("status", false);
                        result.put("message", "Sản phẩm đã hết hàng hoặc không đủ số lượng!");
                    } else {
                        cart.add(product, quantity);
                        cartDbDao.saveOrUpdate(userId, productId, quantity); // Đồng bộ DB
                        result.put("status", true);
                        result.put("message", "Đã thêm vào giỏ hàng!");
                    }
                } else {
                    result.put("status", false);
                    result.put("message", "Sản phẩm không tồn tại!");
                }
            } else if ("update".equals(action)) {
                int productId = jsonObject.get("id").getAsInt();
                int quantity = jsonObject.get("quantity").getAsInt();

                if (quantity <= 0) {
                    cart.remove(productId);
                    cartDbDao.remove(userId, productId); // Xóa khỏi DB
                    result.put("status", true);
                    result.put("message", "Cập nhật thành công!");
                } else if (orderService.CheckStock(productId, quantity)) {
                    result.put("status", false);
                    result.put("message", "Không đủ hàng trong kho!");
                } else {
                    cart.update(productId, quantity);
                    cartDbDao.updateQuantity(userId, productId, quantity); // Đồng bộ DB
                    result.put("status", true);
                    result.put("message", "Cập nhật thành công!");
                }

            } else if ("updateSelection".equals(action)) {
                int productId = jsonObject.get("id").getAsInt();
                boolean selected = jsonObject.get("selected").getAsBoolean();
                boolean success = cart.updateSelection(productId, selected);
                if (success) {
                    cartDbDao.updateSelection(userId, productId, selected); // Đồng bộ DB
                    result.put("status", true);
                    result.put("message", "Cập nhật thành công!");
                } else {
                    result.put("status", false);
                    result.put("message", "Sản phẩm không tồn tại!");
                }
            } else if ("selectAll".equals(action)) {
                boolean selected = jsonObject.get("selected").getAsBoolean();
                cart.toggleAllSelection(selected);
                cartDbDao.toggleAllSelection(userId, selected); // Đồng bộ DB
                result.put("status", true);
                result.put("message", "Đã cập nhật tất cả!");
            }

            result.put("cartSize", cart.getTotalQuantityAll());
            double subtotal = cart.getSelectedTotalWithDiscount();
            double finalTotal = subtotal;
            Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
            if (appliedCoupon != null) {
                if (subtotal >= appliedCoupon.getMinOrderAmount()) {
                    int discount = ApplyCouponController.getDiscountAmount(appliedCoupon, subtotal);
                    finalTotal = subtotal - discount;
                } else {
                    // Xử lý Lỗi "Kẹt" Mã Giảm Giá qua AJAX: Gỡ mã nếu tổng tiền không đủ điều kiện
                    session.removeAttribute("appliedCoupon");
                    result.put("couponRemoved", true); // Báo cho frontend biết mã đã bị gỡ
                    result.put("couponWarning", "Mã giảm giá đã bị gỡ do đơn hàng không đủ điều kiện.");
                }
            }
            result.put("total", finalTotal);
            out.print(gson.toJson(result));

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", false);
            error.put("message", "Lỗi xử lý: " + e.getMessage());
            out.print(gson.toJson(error));
        }
        out.flush();
    }

    private void handleFormRequest(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession();
        CartService cart = (CartService) session.getAttribute("cart");

        if (cart != null) {
            if ("remove".equals(action)) {
                try {
                    int id = Integer.parseInt(request.getParameter("id"));
                    cart.remove(id);
                    cartDbDao.remove(userId, id); // Đồng bộ DB
                } catch (NumberFormatException e) {
                    log.error(e.getMessage());
                }
            } else if ("update".equals(action)) {
                try {
                    int id = Integer.parseInt(request.getParameter("id"));
                    int qty = Integer.parseInt(request.getParameter("quantity"));
                    if (qty <= 0) {
                        cart.remove(id);
                        cartDbDao.remove(userId, id);
                    } else if (!orderService.CheckStock(id, qty)) {
                        cart.update(id, qty);
                        cartDbDao.updateQuantity(userId, id, qty);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
        response.sendRedirect("cart");
    }
}