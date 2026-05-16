package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
import vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.product.ApplyCouponController;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.OrderDTO;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Address;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Coupon;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Order;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.OrderDetail;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.AddressService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.OrderService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.ProductService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.PurchaseService;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.ReadJsonUtil;


@WebServlet(urlPatterns = "/purchase")
public class PurchaseController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PurchaseController.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            PurchaseService purchaseService = new PurchaseService();
            List<OrderDTO> orders = purchaseService.getAllPurchaseByUserID(user.getId());
            request.setAttribute("orders", orders);
            request.getRequestDispatcher("purchase.jsp").forward(request, response);
        } catch (ServletException | IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        OrderService orderService = new OrderService();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 1. Kiểm tra đăng nhập
            HttpSession session = request.getSession(false);
            if (session == null) {
                out.print("{\"success\": false, \"redirect\": \"login.jsp\"}");
                return;
            }
            User user = (User) session.getAttribute("user");
            if (user == null) {
                out.print("{\"success\": false, \"redirect\": \"login.jsp\"}");
                return;
            }

            // 2. Parse JSON từ request body
            JsonObject jsonObject = ReadJsonUtil.parseJson(request, JsonObject.class);

            int productId = jsonObject.get("productId").getAsInt();
            int quantity = jsonObject.has("quantity") ? jsonObject.get("quantity").getAsInt() : 1;

            if (productId <= 0 || quantity <= 0) {
                out.print("{\"success\": false, \"message\": \"Dữ liệu không hợp lệ!\"}");
                return;
            }

            // Lấy thông tin sản phẩm từ DB (bảo mật - không tin giá từ client)
            ProductService productService = new ProductService();
            Product product = productService.getById(productId);
            if (product == null) {
                out.print("{\"success\": false, \"message\": \"Không tìm thấy sản phẩm!\"}");
                return;
            }

            // Tính giá sau discount của sản phẩm
            int price = product.getPrice();
            if (product.getDiscount() > 0) {
                price = price - (price * product.getDiscount() / 100);
            }

            // 4. Kiểm tra tồn kho
            if (orderService.CheckStock(productId, quantity)) {
                out.print("{\"success\": false, \"message\": \"Không đủ hàng trong kho!\"}");
                return;
            }

            // 5. Kiểm tra địa chỉ giao hàng
            Address address = (Address) session.getAttribute("addressDefault");
            if (address == null) {
                AddressService addressService = new AddressService();
                address = addressService.getAddressDefault(user.getId());
                if (address != null) {
                    session.setAttribute("addressDefault", address);
                } else {
                    out.print("{\"success\": false, \"message\": \"Vui lòng cập nhật địa chỉ nhận hàng trước khi mua hàng!\"}");
                    return;
                }
            }

            // 6. Tính toán đơn hàng
            int total = quantity * price;
            int shippingFee = 0;

            // Tính discount từ coupon trong session (nếu có)
            int couponDiscountAmount = 0;
            Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
            if (appliedCoupon != null) {
                couponDiscountAmount = ApplyCouponController.getDiscountAmount(appliedCoupon, (double) total);
            }

            // 7. Tạo đơn hàng
            Order order = new Order();
            order.setStatus(0); // Trạng thái chờ xác nhận
            order.setUserId(user.getId());
            order.setPaymentTypeId(1); // COD mặc định
            order.setShippingFee(shippingFee);

            OrderDetail detail = new OrderDetail();
            detail.setProductId(productId);
            detail.setPrice(price);
            detail.setQuantity(quantity);
            detail.setDiscountAmount(couponDiscountAmount);
            detail.setTotalMoney(total);

            List<OrderDetail> details = List.of(detail);

            // 8. Lưu đơn hàng
            orderService.addOrder(order, details);
            log.info("Mua ngay - Đã tạo đơn hàng cho user:{}, product:{}, quantity:{}", user.getId(), productId, quantity);

            // 10. Xóa coupon khỏi session sau khi đặt hàng thành công
            session.removeAttribute("appliedCoupon");
            out.print("{\"success\": true, \"message\": \"Đặt hàng thành công!\"}");

        } catch (Exception e) {
            log.error("Mua ngay thất bại", e);
            java.util.Map<String, Object> errorMap = new java.util.HashMap<>();
            errorMap.put("success", false);
            errorMap.put("message", e.getMessage() != null ? e.getMessage() : "Lỗi hệ thống");
            out.print(new Gson().toJson(errorMap));
        } finally {
            out.flush();
            out.close();
        }
    }
}
