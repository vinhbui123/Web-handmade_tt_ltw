package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.auth;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/facebook-login")
public class FacebookLoginServlet extends HttpServlet {
    private static final String CLIENT_ID = "2143905963130973";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Tự động chọn redirect URI dựa trên domain hiện tại
        String redirectUri;
        String serverName = request.getServerName();
        if (serverName.contains("ttltwtnkiet.id.vn")) {
            redirectUri = "https://shophandmade.ttltwtnkiet.id.vn/facebook/callback";
        } else {
            redirectUri = "http://localhost:8080/Web_ban_hang/facebook/callback";
        }

        String loginUrl = "https://www.facebook.com/v18.0/dialog/oauth?" +
                "client_id=" + CLIENT_ID +
                "&redirect_uri=" + redirectUri +
                "&scope=email";

        response.sendRedirect(loginUrl);
    }
}