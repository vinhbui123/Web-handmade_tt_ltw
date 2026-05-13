package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.account;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Xác thực OTP quên mật khẩu:
 *   GET  /forgot-otp  → hiển thị form nhập OTP
 *   POST /forgot-otp  → kiểm tra OTP → nếu đúng, đánh dấu session và chuyển sang /reset-password
 */
@WebServlet(name = "ForgotOtpController", urlPatterns = "/forgot-otp")
public class ForgotOtpController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ForgotOtpController.class);

    // ------------------------------------------------------------------ GET
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Nếu không có OTP trong session → quay về trang nhập email
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("resetOtp") == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }
        request.getRequestDispatcher("/forgot-otp.jsp").forward(request, response);
    }

    // ----------------------------------------------------------------- POST
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // Không có session hoặc OTP → redirect về bước 1
        if (session == null || session.getAttribute("resetOtp") == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String correctOtp = (String) session.getAttribute("resetOtp");
        Long   expiryAt   = (Long)   session.getAttribute("resetOtpExpiry");
        String enteredOtp = request.getParameter("otp");

        // Kiểm tra hết hạn
        if (expiryAt == null || System.currentTimeMillis() > expiryAt) {
            clearOtpSession(session);
            request.setAttribute("error", "Mã OTP đã hết hạn. Vui lòng gửi lại yêu cầu.");
            request.getRequestDispatcher("/forgot-otp.jsp").forward(request, response);
            return;
        }

        // Kiểm tra mã OTP
        if (enteredOtp == null || !enteredOtp.trim().equals(correctOtp)) {
            request.setAttribute("error", "Mã OTP không đúng. Vui lòng kiểm tra lại.");
            request.getRequestDispatcher("/forgot-otp.jsp").forward(request, response);
            return;
        }

        // OTP đúng → đánh dấu session cho phép đổi mật khẩu
        String email = (String) session.getAttribute("resetEmail");
        clearOtpSession(session);                          // xoá OTP khỏi session
        session.setAttribute("otpVerifiedEmail", email);  // cho phép /reset-password dùng

        log.info("OTP verified for email: {}", email);
        response.sendRedirect(request.getContextPath() + "/reset-password");
    }

    // ------------------------------------------------------------- helpers
    private void clearOtpSession(HttpSession session) {
        session.removeAttribute("resetOtp");
        session.removeAttribute("resetOtpExpiry");
        session.removeAttribute("resetEmail");
    }
}
