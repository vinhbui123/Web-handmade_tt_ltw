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
import vn.edu.hcmuaf.fit.Web_ban_hang.services.ProductService;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
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
            // Chỉ xóa sản phẩm khỏi giỏ hàng thật khi không phải Mua Ngay
            if (!orderDTO.isBuyNow()) {
                CartService cart = (CartService) session.getAttribute("cart");
                if (cart != null) {
                    for (OrderDetail detail : details) {
                        cart.remove(detail.getProductId());
                    }
                }
            }

            // 7. Xóa coupon khỏi session sau khi đặt hàng thành công
            session.removeAttribute("appliedCoupon");

            if (order.getPaymentTypeId() == 2) { // 2 = QR VNPAY
                System.out.println("========== ĐÃ VÀO ĐƯỢC LUỒNG VNPAY ==========");
                // Tính tổng tiền cần thanh toán
                long totalAmount = order.getShippingFee();
                for (OrderDetail detail : details) {
                    totalAmount += (detail.getTotalMoney() - detail.getDiscountAmount());
                }

                // Gọi Config để sinh link VNPAY
                String vnpayUrl = vn.edu.hcmuaf.fit.Web_ban_hang.utils.VnPayConfig.generatePaymentUrl(String.valueOf(order.getId()), totalAmount, request);

                // Trả URL về cho Javascript chuyển hướng
                out.print("{\"success\": true, \"redirectUrl\": \"" + vnpayUrl + "\"}");
            } else {
                // Thanh toán tiền mặt (COD) - Trả về thành công bình thường
                out.print("{\"success\": true}");
            }
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

    // Hiển thị trang checkout
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // Kiểm tra đăng nhập
        if (user == null || user.getUsername() == null) {
            request.setAttribute("message", "Cần đăng nhập để thực hiện thao tác này.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        // Lấy địa chỉ mặc định nếu chưa có
        if (session.getAttribute("addressDefault") == null) {
            AddressService addressService = new AddressService();
            Address defaultAddress = addressService.getAddressDefault(user.getId());
            session.setAttribute("addressDefault", defaultAddress);
        }

        // ===== Phần mua ngay (buyNow) =====
        String buyNow = request.getParameter("buyNow");
        if ("true".equals(buyNow)) {
            try {
                int productId = Integer.parseInt(request.getParameter("productId"));
                int quantity = Integer.parseInt(request.getParameter("quantity"));

                // Lấy sản phẩm từ DB
                ProductService productService = new ProductService();
                Product product = productService.getById(productId);
                if (product == null) {
                    request.setAttribute("message", "Không tìm thấy sản phẩm!");
                    request.getRequestDispatcher("/cart.jsp").forward(request, response);
                    return;
                }

                // Tạo giỏ hàng tạm chỉ chứa sản phẩm "Mua Ngay"
                CartService buyNowCart = new CartService();
                buyNowCart.add(product, quantity);

                // Truyền dữ liệu cho checkout.jsp
                request.setAttribute("cart", buyNowCart);
                request.setAttribute("isCartEmpty", false);
                request.setAttribute("isBuyNow", true); // Đánh dấu mode Mua Ngay

                addTocart(request, response, session, buyNowCart);
                return;

            } catch (NumberFormatException e) {
                log.error("Mua ngay - Dữ liệu không hợp lệ", e);
                request.setAttribute("message", "Dữ liệu sản phẩm không hợp lệ!");
                request.getRequestDispatcher("/cart.jsp").forward(request, response);
                return;
            }
        }

        // ===== Phần cart (giỏ hàng) =====
        Object cart = session.getAttribute("cart");
        if (cart == null || ((CartService) cart).getList().isEmpty()) {
            request.setAttribute("isCartEmpty", true);
            request.setAttribute("message", "Giỏ hàng của bạn đang trống.");
            request.getRequestDispatcher("/cart.jsp").forward(request, response);
            return;
        }

        request.setAttribute("cart", cart);
        request.setAttribute("isCartEmpty", false);

        CartService cartService = (CartService) cart;
        addTocart(request, response, session, cartService);
    }

    private void addTocart(HttpServletRequest request, HttpServletResponse response, HttpSession session, CartService cartService) throws ServletException, IOException {
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
