package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.account;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.UserService;

@WebServlet("/change-password")
public class ChangePasswordController extends HttpServlet {
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("change-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String currentPassword = request.getParameter("currentPassword") != null ? request.getParameter("currentPassword") : "";
        String newPassword = request.getParameter("newPassword") != null ? request.getParameter("newPassword") : "";
        String confirmPassword = request.getParameter("confirmPassword") != null ? request.getParameter("confirmPassword") : "";

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            request.setAttribute("error", "Vui lòng nhập đầy đủ thông tin!");
            request.getRequestDispatcher("change-password.jsp").forward(request, response);
            return;
        }

        if (userService.authenticateUser(user.getUsername(), currentPassword) == null) {
            request.setAttribute("error", "Mật khẩu cũ không chính xác!");
            request.getRequestDispatcher("change-password.jsp").forward(request, response);
            return;
        }

        if (!confirmPassword.equals(newPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp!");
            request.getRequestDispatcher("change-password.jsp").forward(request, response);
            return;
        }

        if (currentPassword.equals(newPassword)) {
            request.setAttribute("error","Mật khẩu phải khác mật khẩu cũ");
            request.getRequestDispatcher("change-password.jsp").forward(request, response);
            return;
        }

        boolean isUpdated = userService.updatePassword(user.getUsername(), newPassword);
        if (isUpdated) {
            user.setPassword(newPassword);
            session.setAttribute("user", user);
            request.setAttribute("success", " Đổi mật khẩu thành công!");
        } else {
            request.setAttribute("error", " Có lỗi xảy ra, vui lòng thử lại!");
        }

        request.getRequestDispatcher("change-password.jsp").forward(request, response);
    }
}