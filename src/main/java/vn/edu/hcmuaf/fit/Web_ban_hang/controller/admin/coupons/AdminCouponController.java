package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.coupons;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.CouponDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Coupon;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet(name = "AdminCouponController", urlPatterns = {"/adminCoupons"})
public class AdminCouponController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminCouponController.class);
    private CouponDao couponDao = new CouponDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // (Tùy chọn) Kiểm tra quyền Admin ở đây nếu đã có logic phân quyền
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        // if (user == null || user.getRole() != 1) { response.sendRedirect("login.jsp"); return; }

        // Lấy danh sách mã giảm giá và đẩy sang file JSP
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
                // 1. Lấy dữ liệu cơ bản từ form JSP
                String code = request.getParameter("code");
                int type = Integer.parseInt(request.getParameter("type"));
                int minOrderAmount = Integer.parseInt(request.getParameter("minOrderAmount"));

                // 2. Xử lý logic Giảm theo Tiền (0) hay Phần trăm (1)
                Coupon c = new Coupon();
                c.setCode(code.toUpperCase().trim()); // Chuẩn hóa mã Code luôn in hoa và không khoảng trắng
                c.setType(type);
                c.setMinOrderAmount(minOrderAmount);

                if (type == 0) {
                    c.setDiscountValue(Integer.parseInt(request.getParameter("discountValue")));
                } else {
                    c.setDiscountPercent(Integer.parseInt(request.getParameter("discountPercent")));
                }

                // 3. Xử lý Giá trị giảm tối đa (Có thể bỏ trống/null)
                String maxDiscountStr = request.getParameter("maxDiscountValue");
                if (maxDiscountStr != null && !maxDiscountStr.isEmpty()) {
                    c.setMaxDiscountValue(Integer.parseInt(maxDiscountStr));
                }

                // 4. Xử lý parse Ngày Tháng (Từ input type="datetime-local" của HTML5)
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                c.setStartDate(LocalDateTime.parse(request.getParameter("startDate"), formatter));
                c.setEndDate(LocalDateTime.parse(request.getParameter("endDate"), formatter));

                // 5. Thực thi Insert hoặc Update
                if ("add".equals(action)) {
                    couponDao.addCoupon(c);
                    log.info("Admin đã thêm mã giảm giá mới: {}", c.getCode());
                } else if ("update".equals(action)) {
                    c.setId(Integer.parseInt(request.getParameter("id")));
                    couponDao.updateCoupon(c);
                    log.info("Admin đã cập nhật mã giảm giá ID: {}", c.getId());
                }

            } else if ("delete".equals(action)) {
                // 6. Xử lý Xóa mã
                int id = Integer.parseInt(request.getParameter("id"));
                couponDao.deleteCoupon(id);
                log.info("Admin đã xóa mã giảm giá ID: {}", id);
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý POST Admin Coupon: {}", e.getMessage());
            // (Tùy chọn) Có thể set Attribute lỗi để hiện alert trên JSP
            request.getSession().setAttribute("errorMsg", "Dữ liệu nhập vào không hợp lệ!");
        }

        // Xong việc thì Load (Refresh) lại trang danh sách
        response.sendRedirect("adminCoupons");
    }
}
