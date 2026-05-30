package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.order;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.OrderDao;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(name = "GetReturnDetails", urlPatterns = "/getReturnDetails")
public class GetReturnDetails extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            Map<String, String> details = orderDao.getReturnDetails(orderId);

            if (!details.isEmpty()) {
                // Trả về JSON thủ công để tránh lỗi thư viện
                String json = String.format(
                        "{\"success\": true, \"reason\": \"%s\", \"description\": \"%s\", \"proofImg\": \"%s\"}",
                        details.get("reason").replace("\"", "\\\""),
                        details.get("description") != null ? details.get("description").replace("\"", "\\\"") : "",
                        details.get("proofImg").replace("\\", "/")
                );
                out.print(json);
            } else {
                out.print("{\"success\": false}");
            }
        } catch (Exception e) {
            out.print("{\"success\": false}");
        } finally {
            out.flush();
        }
    }
}