package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.OrderDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.VnPayConfig;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet(name = "VnPayReturnController", value = "/vnpay-return")
public class VnPayReturnController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        try {
            for (String fieldName : fieldNames) {
                String fieldValue = fields.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                        hashData.append('&');
                    }
                }
            }
        } catch (Exception e) {}

        String signValue = VnPayConfig.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                String orderIdStr = request.getParameter("vnp_TxnRef");
                try {
                    int orderId = Integer.parseInt(orderIdStr);
                    OrderDao orderDao = new OrderDao();
                    orderDao.confirmOrder(orderId);
                } catch (Exception e) {}

                response.sendRedirect(request.getContextPath() + "/purchase?msg=payment_success");
            } else {
                String orderIdStr = request.getParameter("vnp_TxnRef");
                try {
                    int orderId = Integer.parseInt(orderIdStr);
                    OrderDao orderDao = new OrderDao();
                    vn.edu.hcmuaf.fit.Web_ban_hang.model.User user = (vn.edu.hcmuaf.fit.Web_ban_hang.model.User) request.getSession().getAttribute("user");

                    if (user != null) {
                        orderDao.cancelOrder(orderId, user.getId());
                    } else {
                        orderDao.cancelOrder(orderId, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                response.sendRedirect(request.getContextPath() + "/purchase?msg=payment_failed");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/purchase?msg=invalid_signature");
        }
    }
}