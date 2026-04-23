package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/google-login")
public class GoogleLoginServlet extends HttpServlet {
    private static final String CLIENT_ID = GoogleConfig.getClientId();
    private static final String SCOPE = "email profile";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Tạo redirect_uri động theo domain đang chạy (hỗ trợ cả localhost và domain thực)
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);
        if (serverPort != 80 && serverPort != 443) {
            url.append(":").append(serverPort);
        }
        url.append(contextPath).append("/google/callback");
        String redirectUri = url.toString();

        String authorizationUrl = "https://accounts.google.com/o/oauth2/auth?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + redirectUri +
                "&scope=" + SCOPE +
                "&response_type=code";

        // Chuyển hướng người dùng đến trang đăng nhập Google
        response.sendRedirect(authorizationUrl);
    }
}
