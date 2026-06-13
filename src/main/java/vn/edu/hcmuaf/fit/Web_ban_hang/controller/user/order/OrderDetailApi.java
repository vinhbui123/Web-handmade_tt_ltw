package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.OrderDao;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(urlPatterns = "/api/order-detail")
public class OrderDetailApi extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));

            OrderDao orderDao = new OrderDao();
            Map<String, Object> orderData = orderDao.getOrderDetailForPopup(orderId);

            if (orderData != null) {
                out.print("{\"success\": true, \"order\": " + gson.toJson(orderData) + "}");
            } else {
                out.print("{\"success\": false, \"message\": \"Không tìm thấy chi tiết đơn hàng!\"}");
            }
        } catch (NumberFormatException e) {
            out.print("{\"success\": false, \"message\": \"Mã đơn hàng không hợp lệ!\"}");
        } catch (Exception e) {
            out.print("{\"success\": false, \"message\": \"Lỗi máy chủ nội bộ!\"}");
        } finally {
            out.flush();
        }
    }
}