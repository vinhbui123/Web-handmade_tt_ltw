package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.order;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.OrderDao;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = "/adminOrders")
public class OrderManagement extends HttpServlet {
    private final OrderDao orderDao = new OrderDao();
    private static final Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy HH:mm:ss").create();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("list_ajax".equals(action)) {
            int page = 1;
            int pageSize = 10;
            if (request.getParameter("page") != null) {
                page = Integer.parseInt(request.getParameter("page"));
            }

            int totalOrders = orderDao.getTotalOrdersCount();
            int totalPages = (int) Math.ceil((double) totalOrders / pageSize);
            int offset = (page - 1) * pageSize;

            List<Map<String, Object>> orders = orderDao.getOrdersByPageForAdmin(offset, pageSize);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("orderDetails", orders);
            responseMap.put("totalPages", totalPages);
            responseMap.put("totalOrders", totalOrders);
            responseMap.put("currentPage", page);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(gson.toJson(responseMap));
            out.flush();
        } else {
            request.getRequestDispatcher("ad-orders.jsp").forward(request, response);
        }
    }
}