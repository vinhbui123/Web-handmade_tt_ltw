package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.order;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.OrderDao;

import java.io.IOException;

@WebServlet(name = "AdminProcessReturn", urlPatterns = "/adminProcessReturn")
public class AdminProcessReturn extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String orderIdRaw = request.getParameter("orderId");
        String action = request.getParameter("action"); // Sẽ nhận giá trị "accept" hoặc "reject"

        try {
            int orderId = Integer.parseInt(orderIdRaw);
            boolean success = orderDao.processReturnRequest(orderId, action);

            if (success) {
                if ("accept".equals(action)) {
                    System.out.println("Admin đã CHẤP NHẬN hoàn trả đơn hàng #" + orderId);
                } else {
                    System.out.println("Admin đã TỪ CHỐI hoàn trả đơn hàng #" + orderId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Xử lý xong thì quay lại trang Quản lý Đơn hàng
        response.sendRedirect(request.getContextPath() + "/adminOrders");
    }
}