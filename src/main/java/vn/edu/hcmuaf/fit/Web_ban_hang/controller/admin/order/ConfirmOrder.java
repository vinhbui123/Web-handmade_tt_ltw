package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.order;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.OrderDao;

import java.io.IOException;

@WebServlet(name = "ConfirmOrder", urlPatterns = "/confirmOrder")
public class ConfirmOrder extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        boolean isAjax = "true".equals(request.getParameter("ajax"));

        OrderDao dao = new OrderDao();
        boolean success = dao.confirmOrder(orderId);

        if (success) {
            System.out.println("Admin đã xác nhận đơn hàng #" + orderId);
        }

        // Xử lý trả về cho AJAX
        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print("{\"success\": " + success + "}");
            return;
        }

        // Fallback nếu người dùng tắt JS hoặc lỗi (sẽ reload trang)
        response.sendRedirect(request.getContextPath() + "/adminOrders");
    }
}