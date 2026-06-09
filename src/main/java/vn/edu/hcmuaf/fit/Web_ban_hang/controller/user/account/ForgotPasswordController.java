package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.account;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.UserDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.EmailUtil;

@WebServlet(name = "ForgotPasswordController", urlPatterns = "/forgot-password")
public class ForgotPasswordController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordController.class);

    private static final int OTP_EXPIRY_SECONDS = 10 * 60;

    private final UserDao userDao = UserDao.getInstance();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/forget-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập địa chỉ email.");
            request.getRequestDispatcher("/forget-password.jsp").forward(request, response);
            return;
        }
        email = email.trim().toLowerCase();

        User user = userDao.getUserByEmail(email);
        if (user == null) {
            request.setAttribute("message",
                "Nếu email của bạn tồn tại trong hệ thống, chúng tôi đã gửi mã OTP. Vui lòng kiểm tra hộp thư.");
            request.getRequestDispatcher("/forget-password.jsp").forward(request, response);
            return;
        }

        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        long   expiryAt = System.currentTimeMillis() + OTP_EXPIRY_SECONDS * 1000L;

        HttpSession session = request.getSession();
        session.setAttribute("resetOtp",      otp);
        session.setAttribute("resetEmail",    email);
        session.setAttribute("resetOtpExpiry", expiryAt);

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
