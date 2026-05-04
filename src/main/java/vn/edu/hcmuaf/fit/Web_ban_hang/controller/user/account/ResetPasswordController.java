package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.account;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.UserDao;

import java.io.IOException;

/**
 * Đặt lại mật khẩu sau khi OTP đã được xác thực.
 *   GET  /reset-password  → hiển thị form đặt mật khẩu mới (yêu cầu otpVerifiedEmail trong session)
 *   POST /reset-password  → cập nhật mật khẩu vào DB
 */
@WebServlet(name = "ResetPasswordController", urlPatterns = "/reset-password")
public class ResetPasswordController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ResetPasswordController.class);
    private final UserDao userDao = UserDao.getInstance();

    // ------------------------------------------------------------------ GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Phải có otpVerifiedEmail trong session mới được vào trang này
        HttpSession session      = request.getSession(false);
        String      verifiedEmail = (session != null)
                ? (String) session.getAttribute("otpVerifiedEmail") : null;

        if (verifiedEmail == null || verifiedEmail.isBlank()) {
            // Không hợp lệ → về bước 1
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        request.getRequestDispatcher("/reset-password.jsp").forward(request, response);
    }

    // ----------------------------------------------------------------- POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session      = request.getSession(false);
        String      verifiedEmail = (session != null)
                ? (String) session.getAttribute("otpVerifiedEmail") : null;

        // Guard: không có quyền
        if (verifiedEmail == null || verifiedEmail.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String newPassword = request.getParameter("newPassword");
        String confirmPwd  = request.getParameter("confirmPassword");

        // Validate
        if (newPassword == null || newPassword.isEmpty() || confirmPwd == null || confirmPwd.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ mật khẩu mới.");
            request.getRequestDispatcher("/reset-password.jsp").forward(request, response);
            return;
        }
        if (!newPassword.equals(confirmPwd)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp.");
            request.getRequestDispatcher("/reset-password.jsp").forward(request, response);
            return;
        }
        if (newPassword.length() < 8) {
            request.setAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự.");
            request.getRequestDispatcher("/reset-password.jsp").forward(request, response);
            return;
        }

        // Cập nhật mật khẩu
        boolean updated = userDao.updatePasswordByEmail(verifiedEmail, newPassword);
        if (!updated) {
            request.setAttribute("error", "Có lỗi khi cập nhật mật khẩu. Vui lòng thử lại.");
            request.getRequestDispatcher("/reset-password.jsp").forward(request, response);
            return;
        }

        // Xoá quyền khỏi session
        session.removeAttribute("otpVerifiedEmail");
        log.info("Đặt lại mật khẩu thành công cho: {}", verifiedEmail);

        // Thành công → về trang đăng nhập
        request.getSession().setAttribute("success",
                "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
