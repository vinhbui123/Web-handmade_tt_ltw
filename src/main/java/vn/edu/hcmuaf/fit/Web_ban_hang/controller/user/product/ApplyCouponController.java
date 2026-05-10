package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.product;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.CouponDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Coupon;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService;

@WebServlet("/apply-coupon")
public class ApplyCouponController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ApplyCouponController.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            HttpSession session = req.getSession();
            String code = req.getParameter("code");

            double total = 0.0;
            CartService cart = (CartService) session.getAttribute("cart");
            if (cart != null) {
                total = cart.getSelectedTotalWithDiscount();
            }

            CouponDao couponDao = new CouponDao();
            Coupon matchedCoupon = couponDao.getByCode(code);

            if (matchedCoupon == null) {
                out.print("{\"success\": false, \"message\": \"Không tìm thấy mã hoặc mã đã hết hạn.\"}");
                out.flush();
                return;
            }

            if (total < matchedCoupon.getMinOrderAmount()) {
                out.print("{\"success\": false, \"message\": \"Đơn hàng chưa đủ tối thiểu để áp dụng mã.\"}");
                out.flush();
                return;
            }

            // Logic tính toán số tiền giảm giá
            int discountAmount = getDiscountAmount(matchedCoupon, total);

            // Lưu mã vào session để có thể sử dụng khi checkout nếu cần
            session.setAttribute("appliedCoupon", matchedCoupon);

            // Trả về JSON kết quả bằng HashMap (tương thích tốt với Gson)
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Đã áp dụng mã thành công.");
            result.put("discountAmount", discountAmount);
            result.put("newTotal", total - discountAmount);

            out.print(new Gson().toJson(result));
            out.flush();

        } catch (Exception e) {
            log.error("Lỗi khi áp dụng mã giảm giá", e);
            out.print("{\"success\": false, \"message\": \"Có lỗi xảy ra trên server, vui lòng thử lại.\"}");
            out.flush();
        }
    }

    public static int getDiscountAmount(Coupon matchedCoupon, Double total) {
        int discountAmount = 0;
        if (matchedCoupon.getType() == 0) { // Giảm tiền mặt
            discountAmount = matchedCoupon.getDiscountValue();
        } else if (matchedCoupon.getType() == 1) { // Giảm theo phần trăm
            int discountPercent = matchedCoupon.getDiscountPercent();
            discountAmount = (int) (total * discountPercent / 100.0);

            // Áp dụng giới hạn số tiền giảm tối đa nếu có
            if (matchedCoupon.getMaxDiscountValue() != null && discountAmount > matchedCoupon.getMaxDiscountValue()) {
                discountAmount = matchedCoupon.getMaxDiscountValue();
            }
        }

        // Safety check: Số tiền giảm không được lớn hơn tổng giá trị đơn hàng
        if (discountAmount > total) {
            discountAmount = total.intValue();
        }
        return discountAmount;
    }

}
