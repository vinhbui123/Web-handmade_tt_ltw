package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.coupons;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.CouponDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Coupon;

@WebServlet(name = "AdminCouponController", urlPatterns = {"/adminCoupons"})
public class AdminCouponController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminCouponController.class);
    private CouponDao couponDao = new CouponDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Coupon> coupons = couponDao.getAllCoupons();
        request.setAttribute("coupons", coupons);
        request.getRequestDispatcher("/ad-coupon.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect("adminCoupons");
            return;
        }

        try {
            if ("add".equals(action) || "update".equals(action)) {
                String code = request.getParameter("code");
                int type = Integer.parseInt(request.getParameter("type"));
                int minOrderAmount = Integer.parseInt(request.getParameter("minOrderAmount"));
                Coupon c = new Coupon();
                c.setCode(code.toUpperCase().trim());
                c.setType(type);
                c.setMinOrderAmount(minOrderAmount);

                if (type == 0) {
                    c.setDiscountValue(Integer.parseInt(request.getParameter("discountValue")));
                } else {
                    c.setDiscountPercent(Integer.parseInt(request.getParameter("discountPercent")));
                }

                String maxDiscountStr = request.getParameter("maxDiscountValue");
                if (maxDiscountStr != null && !maxDiscountStr.isEmpty()) {
                    c.setMaxDiscountValue(Integer.parseInt(maxDiscountStr));
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                c.setStartDate(LocalDateTime.parse(request.getParameter("startDate"), formatter));
                c.setEndDate(LocalDateTime.parse(request.getParameter("endDate"), formatter));
                if ("add".equals(action)) {
                    couponDao.addCoupon(c);
                    log.info("Admin đã thêm mã giảm giá mới: {}", c.getCode());
                } else if ("update".equals(action)) {
                    c.setId(Integer.parseInt(request.getParameter("id")));
                    couponDao.updateCoupon(c);
                    log.info("Admin đã cập nhật mã giảm giá ID: {}", c.getId());
                }

            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                couponDao.deleteCoupon(id);
                log.info("Admin đã xóa mã giảm giá ID: {}", id);
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý POST Admin Coupon: {}", e.getMessage());
            request.getSession().setAttribute("errorMsg", "Dữ liệu nhập vào không hợp lệ!");
        }

        response.sendRedirect("adminCoupons");
    }
}
