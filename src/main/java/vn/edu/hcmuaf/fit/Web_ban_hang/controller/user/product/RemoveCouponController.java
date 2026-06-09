package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.product;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService;

@WebServlet("/remove-coupon")
public class RemoveCouponController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(RemoveCouponController.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            HttpSession session = req.getSession();
            session.removeAttribute("appliedCoupon");

            double total = 0.0;
            CartService cart = (CartService) session.getAttribute("cart");
            if (cart != null) {
                total = cart.getSelectedTotalWithDiscount();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đã hủy áp dụng mã giảm giá.");
            result.put("newTotal", total);

            out.print(new Gson().toJson(result));
            out.flush();

        } catch (Exception e) {
            log.error("Lỗi khi hủy mã giảm giá", e);
            out.print("{\"success\": false, \"message\": \"Có lỗi xảy ra trên server, vui lòng thử lại.\"}");
            out.flush();
        }
    }
}
