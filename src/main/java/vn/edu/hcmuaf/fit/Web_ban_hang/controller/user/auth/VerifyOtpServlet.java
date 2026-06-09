package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.auth;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.session.Cart;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;

@WebServlet(name = "VerifyOtpServlet", urlPatterns = "/verify-otp")
public class VerifyOtpServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String enteredOtp = request.getParameter("otp");
        String sessionOtp = (String) session.getAttribute("otp");

        if (enteredOtp != null && enteredOtp.equals(sessionOtp)) {
            User user = (User) session.getAttribute("pendingUser");
            session.setAttribute("user", user);
            session.setAttribute("cart", new Cart());
            session.removeAttribute("otp");
            session.removeAttribute("pendingUser");

            response.sendRedirect(request.getContextPath() + "/home");
        } else {
            request.setAttribute("errorMessage", "Mã OTP không chính xác hoặc đã hết hạn.");
            request.getRequestDispatcher("verify-otp.jsp").forward(request, response);
        }
    }
}