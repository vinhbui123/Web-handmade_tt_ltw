package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.auth;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.UserDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService;


@WebServlet("/google/callback")
public class GoogleCallbackServlet extends HttpServlet {
    protected static final String CLIENT_ID = GoogleConfig.getClientId();
    protected static final String CLIENT_SECRET = GoogleConfig.getClientSecret();
    private static final Logger log = LoggerFactory.getLogger(GoogleCallbackServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");

        if (code != null) {
            try {
                HttpTransport httpTransport = new NetHttpTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                String redirectUri;
                String serverName = request.getServerName();
                if (serverName.contains("ttltwtnkiet.id.vn")) {
                    redirectUri = "https://shophandmade.ttltwtnkiet.id.vn/google/callback";
                } else {
                    redirectUri = "http://localhost:8080/Web_ban_hang/google/callback";
                }

                System.out.println("Redirect URI sent to Google: " + redirectUri);

                GoogleAuthorizationCodeFlow authFlow = new GoogleAuthorizationCodeFlow.Builder(httpTransport,
                        jsonFactory, CLIENT_ID, CLIENT_SECRET, Collections.singletonList("email")).build();

                TokenResponse tokenResponse = authFlow.newTokenRequest(code).setRedirectUri(redirectUri).execute();

                GoogleTokenResponse googleResponse = (GoogleTokenResponse) tokenResponse;
                GoogleIdToken idToken = googleResponse.parseIdToken();

                if (idToken != null) {
                    String email = idToken.getPayload().getEmail();
                    authenticateUserWithGoogle(email, request, response);
                } else {
                    response.getWriter().println("Failed to parse ID Token.");
                }

            } catch (Exception e) {
                log.error(e.getMessage());
                response.getWriter().println("Error: " + e.getMessage());
            }
        } else {
            response.getWriter().println("No authorization code received.");
        }
    }

    private void authenticateUserWithGoogle(String email, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        UserDao userDao = new UserDao();
        User user = userDao.getUserByEmail(email);

        if (user == null) {
            String username = email.split("@")[0];

            user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setFirstName(username);
            user.setLastName("");
            user.setAuthProvider("google");
            user.setRole(0);
            user.setStatus(1);

            boolean inserted = userDao.insertGoogleUser(user);
            if (inserted) {
                user = userDao.getUserByEmail(email);
                log.info("{} Đăng ký + Đăng nhập bằng Google: Tài khoản mới được tạo", user.getFirstName() + " " + user.getLastName());
            } else {
                log.info("Không thể tạo tài khoản từ Google: {}", email);
                response.getWriter().println("Không thể tạo tài khoản từ Google.");
                return;
            }
        } else {
            if (user.getStatus() == 0) {
                log.info("Đăng nhập Google thất bại: Tài khoản bị khóa ({})", email);
                request.setAttribute("errorMessage", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }
            log.info("{} Đăng nhập Google thành công", user.getFirstName() + " " + user.getLastName());
        }

        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService cart = new vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService();
        vn.edu.hcmuaf.fit.Web_ban_hang.dao.CartDbDao cartDbDao = new vn.edu.hcmuaf.fit.Web_ban_hang.dao.CartDbDao();
        cart.setData(cartDbDao.getCartByUserId(user.getId()));
        session.setAttribute("cart", cart);

        response.sendRedirect(request.getContextPath() + "/home");
    }
}
