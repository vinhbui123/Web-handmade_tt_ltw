package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order;


import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.product.ApplyCouponController;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.InventoryDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.OrderDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.OrderDTO;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Coupon;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Order;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.OrderDetail;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.PurchaseService;

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
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            int price = Integer.parseInt(request.getParameter("price"));
            int quantity = Integer.parseInt(request.getParameter("quantity"));

            log.info("productId:{}, quantity:{}, price:{}", productId, quantity, price);
            int total = quantity * price;

            // Tính discount từ coupon trong session
            int couponDiscountAmount = 0;
            Coupon appliedCoupon = (Coupon) session.getAttribute("appliedCoupon");
            if (appliedCoupon != null) {
                couponDiscountAmount = ApplyCouponController.getDiscountAmount(appliedCoupon, (double) total);
            }

            // Tạo đơn hàng
            Order order = new Order();
            order.setStatus(0);
            order.setUserId(user.getId());
            order.setFreeShipping(1);
            order.setPaymentTypeId(1);

            OrderDetail detail = new OrderDetail();
            detail.setProductId(productId);
            detail.setPrice(price);
            detail.setQuantity(quantity);
            detail.setDiscountAmount(couponDiscountAmount);
            detail.setTotalMoney(total);

            List<OrderDetail> details = List.of(detail);

            // 1. Kiểm tra tồn kho trước
            InventoryDao inventoryDao = new InventoryDao();
            int stock = inventoryDao.getStock(productId);
            if (stock < quantity) {
                request.setAttribute("error", " Không đủ hàng trong kho!");
                request.getRequestDispatcher("/purchase.jsp").forward(request, response);
                return;
            }

            // 2. Lưu đơn hàng nếu đủ hàng
            OrderDao orderDAO = new OrderDao();
            orderDAO.addOrder(order, details);
            log.info("Đã thêm đơn hàng!");

            // 3. Xuất kho
            boolean success = inventoryDao.exportProduct(productId, quantity, user.getId(),"Đặt hàng");
            log.error(" export result: {}", success);
            if (!success) {
                request.setAttribute("error", " Xuất kho thất bại sau khi tạo đơn hàng!");
                request.getRequestDispatcher("/purchase.jsp").forward(request, response);
                return;
            }

            // 4. Xóa coupon khỏi session sau khi đặt hàng thành công
            session.removeAttribute("appliedCoupon");

            response.sendRedirect("purchase");

        } catch (Exception e) {
            log.error(e.getMessage());
            request.setAttribute("error", " Lỗi khi đặt hàng");
            request.getRequestDispatcher("/purchase.jsp").forward(request, response);
        }
    }


}
