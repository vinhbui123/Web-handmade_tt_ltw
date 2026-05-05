package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.account;

import java.io.IOException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.UserService;

// 1. Update urlPatterns to include both login and logout
@WebServlet(name = "AuthController", urlPatterns = {"/login", "/logout"})
public class AuthController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME = 5 * 60 * 1000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        // 2. Check which URL triggered this method
        if ("/logout".equals(path)) {
            handleLogout(request, response);
        } else {
            // Default to showing the login page
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Post is usually only for the Login form submission
        String usernameOrEmail = request.getParameter("username");
        String password = request.getParameter("password");
        String captchaInput = request.getParameter("captcha");

        HttpSession session = request.getSession(true);

        // Kiểm tra tài khoản có bị khóa không
        Long lockTime = (Long) session.getAttribute("lockTime");
        Integer failedAttempts = (Integer) session.getAttribute("failedAttempts");

        if (lockTime != null && Instant.now().toEpochMilli() - lockTime < LOCK_TIME) {
            request.setAttribute("errorMessage", "Bạn đã nhập sai quá 5 lần. Vui lòng thử lại sau 5 phút.");
            request.setAttribute("username", usernameOrEmail);
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        // Validate CAPTCHA
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if (sessionCaptcha == null || captchaInput == null || !sessionCaptcha.equalsIgnoreCase(captchaInput)) {
            failedAttempts = (failedAttempts == null) ? 1 : failedAttempts + 1;
            session.setAttribute("failedAttempts", failedAttempts);

            if (failedAttempts >= MAX_ATTEMPTS) {
                session.setAttribute("lockTime", Instant.now().toEpochMilli());
                request.setAttribute("errorMessage", "Bạn đã nhập CAPTCHA sai quá 5 lần. Tài khoản bị khóa trong 5 phút.");
            } else {
                request.setAttribute("errorMessage", "Mã CAPTCHA không chính xác. Bạn còn " + (MAX_ATTEMPTS - failedAttempts) + " lần thử.");
            }

            request.setAttribute("username", usernameOrEmail);
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        // --- Authenticate User ---
        UserService userService = new UserService();
        session.removeAttribute("user"); // Clear previous user if any

        User user = userService.authenticateUser(usernameOrEmail, password);

        if (user != null) {
            // Reset attempts on successful login
            session.removeAttribute("failedAttempts");
            session.removeAttribute("lockTime");

            // Set session attributes
            if (session.getAttribute("cart") == null) {
                session.setAttribute("cart", new CartService());
            }
            session.setAttribute("user", user);
            log.info("Đăng nhập thành công: {}", user.getUsername());
            response.sendRedirect(request.getContextPath() + ("/home"));
            return;
        }
        
        failedAttempts = (failedAttempts == null) ? 1 : failedAttempts + 1;
        session.setAttribute("failedAttempts", failedAttempts);

        if (failedAttempts >= MAX_ATTEMPTS) {
            session.setAttribute("lockTime", Instant.now().toEpochMilli());
            request.setAttribute("errorMessage", "Bạn đã nhập sai quá 5 lần. Vui lòng thử lại sau 5 phút.");
        } else {
            request.setAttribute("errorMessage", "Tài khoản, email hoặc mật khẩu không đúng. Bạn còn " + (MAX_ATTEMPTS - failedAttempts) + " lần thử.");
        }

        request.setAttribute("username", usernameOrEmail);
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    // Helper method to handle logout logic
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate(); // Remove session form client
        }
    // Redirect to home (using context path for safety)
        response.sendRedirect(request.getContextPath() + "/home");
    }
}