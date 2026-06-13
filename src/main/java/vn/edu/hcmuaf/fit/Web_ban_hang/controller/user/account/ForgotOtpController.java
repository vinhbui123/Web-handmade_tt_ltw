package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.account;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "ForgotOtpController", urlPatterns = "/forgot-otp")
public class ForgotOtpController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ForgotOtpController.class);
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("resetOtp") == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }
        request.getRequestDispatcher("/forgot-otp.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("resetOtp") == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String correctOtp = (String) session.getAttribute("resetOtp");
        Long   expiryAt   = (Long)   session.getAttribute("resetOtpExpiry");
        String enteredOtp = request.getParameter("otp");

        if (expiryAt == null || System.currentTimeMillis() > expiryAt) {
            clearOtpSession(session);
            request.setAttribute("error", "Mã OTP đã hết hạn. Vui lòng gửi lại yêu cầu.");
            request.getRequestDispatcher("/forgot-otp.jsp").forward(request, response);
            return;
        }

        if (enteredOtp == null || !enteredOtp.trim().equals(correctOtp)) {
            request.setAttribute("error", "Mã OTP không đúng. Vui lòng kiểm tra lại.");
            request.getRequestDispatcher("/forgot-otp.jsp").forward(request, response);
            return;
        }

        String email = (String) session.getAttribute("resetEmail");
        clearOtpSession(session);
        session.setAttribute("otpVerifiedEmail", email);

        log.info("OTP verified for email: {}", email);
        response.sendRedirect(request.getContextPath() + "/reset-password");
    }

    private void clearOtpSession(HttpSession session) {
        session.removeAttribute("resetOtp");
        session.removeAttribute("resetOtpExpiry");
        session.removeAttribute("resetEmail");
    }
}
