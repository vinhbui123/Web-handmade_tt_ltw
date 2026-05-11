package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.account;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.UserDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.EmailUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

/**
 * Luồng quên mật khẩu bằng OTP:
 *   GET  /forgot-password  → hiển thị trang nhập email
 *   POST /forgot-password  → tạo OTP 6 số, lưu session, gửi email → redirect /forgot-otp
 */
@WebServlet(name = "ForgotPasswordController", urlPatterns = "/forgot-password")
public class ForgotPasswordController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordController.class);

    /** OTP hết hạn sau bao nhiêu giây (10 phút) */
    private static final int OTP_EXPIRY_SECONDS = 10 * 60;

    private final UserDao userDao = UserDao.getInstance();

    // ------------------------------------------------------------------ GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/forget-password.jsp").forward(request, response);
    }

    // ----------------------------------------------------------------- POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        // Validate
        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập địa chỉ email.");
            request.getRequestDispatcher("/forget-password.jsp").forward(request, response);
            return;
        }
        email = email.trim().toLowerCase();

        // Kiểm tra email tồn tại (trả thông báo chung để tránh lộ thông tin)
        User user = userDao.getUserByEmail(email);
        if (user == null) {
            request.setAttribute("message",
                "Nếu email của bạn tồn tại trong hệ thống, chúng tôi đã gửi mã OTP. Vui lòng kiểm tra hộp thư.");
            request.getRequestDispatcher("/forget-password.jsp").forward(request, response);
            return;
        }

        // Tạo OTP 6 chữ số
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        long   expiryAt = System.currentTimeMillis() + OTP_EXPIRY_SECONDS * 1000L;

        // Lưu OTP vào session (kèm email và thời gian hết hạn)
        HttpSession session = request.getSession();
        session.setAttribute("resetOtp",      otp);
        session.setAttribute("resetEmail",    email);
        session.setAttribute("resetOtpExpiry", expiryAt);

        // Gửi email OTP
        try {
            EmailUtil.sendOtpEmail(getServletContext(), email, otp);
            log.info("OTP sent to {}", email);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Gửi OTP thất bại cho: {}", email, e);
            request.setAttribute("error", "Không thể gửi OTP lúc này. Vui lòng thử lại sau.");
            request.getRequestDispatcher("/forget-password.jsp").forward(request, response);
            return;
        }

        // Chuyển sang trang nhập OTP
        response.sendRedirect(request.getContextPath() + "/forgot-otp");
    }
}
