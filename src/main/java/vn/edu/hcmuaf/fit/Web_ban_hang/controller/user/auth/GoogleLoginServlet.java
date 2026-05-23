package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.auth;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/google-login")
public class GoogleLoginServlet extends HttpServlet {
    private static final String CLIENT_ID = GoogleConfig.getClientId();
    private static final String SCOPE = "email profile";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Tự động chọn redirect URI dựa trên domain hiện tại
        String redirectUri;
        String serverName = request.getServerName();
        if (serverName.contains("ttltwtnkiet.id.vn")) {
            redirectUri = "https://shophandmade.ttltwtnkiet.id.vn/google/callback";
        } else {
            redirectUri = "http://localhost:8080/Web_ban_hang/google/callback";
        }

        String authorizationUrl = "https://accounts.google.com/o/oauth2/auth?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + redirectUri +
                "&scope=" + SCOPE +
                "&response_type=code";

        // Chuyển hướng người dùng đến trang đăng nhập Google
        response.sendRedirect(authorizationUrl);
    }
}
