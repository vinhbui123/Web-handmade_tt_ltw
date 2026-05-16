package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.product.ApplyCouponController;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.InventoryDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.OrderDTO;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Address;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Coupon;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Order;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.OrderDetail;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.AddressService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.OrderService;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.ReadJsonUtil;

@WebServlet(name = "CheckoutController", value = "/checkout")
public class CheckoutController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws  IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 1. Parse JSON
            OrderDTO orderDTO = ReadJsonUtil.parseJson(request, OrderDTO.class);

            OrderService orderService = new OrderService();
            Order order = new Order(orderDTO.getStatus(), orderDTO.getUserId(), orderDTO.getShippingFee(),
                    orderDTO.getPaymentTypeId());
            List<OrderDetail> details = orderService.toDetailOrder(orderDTO.getDetails());

            // 2. Kiểm tra tồn kho
            InventoryDao inventoryDao = new InventoryDao();
            for (OrderDetail detail : details) {
                int stock = inventoryDao.getStock(detail.getProductId());
                if (stock < detail.getQuantity()) {
                    out.print("{\"success\": false, \"message\": \"Không đủ hàng trong kho cho SP ID: "
                            + detail.getProductId() + "\"}");
                    return;
                }
            }

            // 3. Kiểm tra địa chỉ giao hàng
            HttpSession session = request.getSession(false);
            if (session == null) {
                out.print("{\"success\": false, \"message\": \"Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.\"}");
                return;
            }
            Address address = (Address) session.getAttribute("addressDefault");
            if (address == null) {
                out.print("{\"success\": false, \"message\": \"Cập nhật địa chỉ đơn hàng trước khi đặt hàng.\"}");
                return;
            }

            // Áp dụng coupon discount lên chi tiết đơn hàng
            Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
            if (appliedCoupon != null) {
                // Tính tổng tiền đơn hàng
                double orderTotal = 0;
                for (OrderDetail detail : details) {
                    orderTotal += detail.getTotalMoney();
                }

                int discountAmount = ApplyCouponController.getDiscountAmount(appliedCoupon, orderTotal);

                // Phân bổ discount cho từng detail theo tỷ lệ
                int remainingDiscount = discountAmount;
                for (int i = 0; i < details.size(); i++) {
                    OrderDetail detail = details.get(i);

                    if (i == details.size() - 1) {
                        // Dòng cuối nhận phần còn lại để tránh sai lệch do làm tròn
                        detail.setDiscountAmount(remainingDiscount);
                    } else {
                        int detailDiscount = (int) ((double) detail.getTotalMoney() / orderTotal * discountAmount);
                        detail.setDiscountAmount(detailDiscount);
                        remainingDiscount -= detailDiscount;
                    }
                }
            }

            // 5. Lưu đơn hàng
            orderService.addOrder(order, details);

            // 6. Trừ kho
            for (OrderDetail detail : details) {
                System.out.println("Exporting productId=" + detail.getProductId() + ", quantity="
                        + detail.getQuantity() + ", userId=" + orderDTO.getUserId());

                boolean success = inventoryDao.exportProduct(detail.getProductId(), detail.getQuantity(),
                        orderDTO.getUserId(), "export");
                if (!success) {
                    out.print("{\"success\": false, \"message\": \"Trừ kho thất bại sau khi đã lưu đơn.\"}");
                    return;
                }
            }
            CartService cart = (CartService) session.getAttribute("cart");
            if (cart != null) {
                for (OrderDetail detail : details) {
                    cart.remove(detail.getProductId());
                }
            }

            // 7. Xóa coupon khỏi session sau khi đặt hàng thành công
            session.removeAttribute("appliedCoupon");

            out.print("{\"success\": true}");

        } catch (Exception e) {
            log.error("Checkout failed", e);
            java.util.Map<String, Object> errorMap = new java.util.HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống");
            out.print(new Gson().toJson(errorMap));
        } finally {
            out.flush();
            out.close();
        }
    }

    // Nếu bạn dùng GET để hiển thị trang checkout
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Object cart = session.getAttribute("cart");
        User user = (User) session.getAttribute("user");

        if (user == null || user.getUsername() == null) {
            request.setAttribute("message", "Cần đăng nhập để thực hiện thao tác này.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        if (cart == null || ((CartService) cart).getList().isEmpty()) {
            request.setAttribute("isCartEmpty", true);
            request.setAttribute("message", "Giỏ hàng của bạn đang trống.");
            request.getRequestDispatcher("/cart.jsp").forward(request, response);
            return;
        }
        if (session.getAttribute("addressDefault") == null) {
            AddressService addressService = new AddressService();
            Address defaultAddress = addressService.getAddressDefault(user.getId());
            session.setAttribute("addressDefault", defaultAddress);
        }
        // Truyền lại thông tin người nhận
        request.setAttribute("cart", cart);
        request.setAttribute("isCartEmpty", false);

        CartService cartService = (CartService) cart;
        double selectedTotal = cartService.getSelectedTotalWithDiscount();
        int discountAmount = 0;

        Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
        if (appliedCoupon != null) {
            discountAmount = ApplyCouponController.getDiscountAmount(appliedCoupon, selectedTotal);
        }

        request.setAttribute("discountAmount", discountAmount);
        request.setAttribute("finalTotal", selectedTotal - discountAmount);

        request.getRequestDispatcher("/checkout.jsp").forward(request, response);
    }
}
