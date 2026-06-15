package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.auth;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.UserDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.HttpUtil;

@WebServlet("/facebook/callback")
public class FacebookCallbackServlet extends HttpServlet {
    private static final String CLIENT_ID = "2143905963130973";
    private static final String CLIENT_SECRET = "78b2645f7466b81fcd420c9e0c2d3766";
    private static final Logger log = LoggerFactory.getLogger(FacebookCallbackServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String code = request.getParameter("code");

        if (code == null) {
            response.getWriter().println("Không nhận được mã từ Facebook");
            return;
        }

        try {
            String redirectUri;
            String serverName = request.getServerName();
            if (serverName.contains("ttltwtnkiet.id.vn")) {
                redirectUri = "https://shophandmade.ttltwtnkiet.id.vn/facebook/callback";
            } else {
                redirectUri = "http://localhost:8080/Web_ban_hang/facebook/callback";
            }

            String accessTokenUrl = "https://graph.facebook.com/v18.0/oauth/access_token?" +
                    "client_id=" + CLIENT_ID +
                    "&redirect_uri=" + redirectUri +
                    "&client_secret=" + CLIENT_SECRET +
                    "&code=" + code;

            String accessTokenResponse = HttpUtil.sendGet(accessTokenUrl);
            String accessToken = JsonParser.parseString(accessTokenResponse)
                    .getAsJsonObject().get("access_token").getAsString();

            String userInfoUrl = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
            String userInfoResponse = HttpUtil.sendGet(userInfoUrl);
            JsonObject userInfo = JsonParser.parseString(userInfoResponse).getAsJsonObject();

            String email = userInfo.has("email") ? userInfo.get("email").getAsString() : null;
            String name = userInfo.get("name").getAsString();
            String facebookId = userInfo.get("id").getAsString();
            User user = getUser(email, facebookId, name);

            if (user != null) {
                if (user.getStatus() == 0) {
                    request.setAttribute("errorMessage", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.");
                    request.getRequestDispatcher("login.jsp").forward(request, response);
                    return;
                }
                HttpSession session = request.getSession();
                session.setAttribute("user", user);

                vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService cart = new vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService();
                vn.edu.hcmuaf.fit.Web_ban_hang.dao.CartDbDao cartDbDao = new vn.edu.hcmuaf.fit.Web_ban_hang.dao.CartDbDao();
                cart.setData(cartDbDao.getCartByUserId(user.getId()));
                session.setAttribute("cart", cart);
            }
            response.sendRedirect(request.getContextPath() + "/home");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response.getWriter().println("Lỗi: " + e.getMessage());
        }
    }

    private static User getUser(String email, String facebookId, String name) {
        String username = (email != null) ? email.split("@")[0] : "fb_" + facebookId;

        UserDao userDao = new UserDao();
        User user = (email != null) ? userDao.getUserByEmail(email) : userDao.getUserByUsername(username);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setFirstName(name);
            user.setLastName("");
            user.setRole(0);
            user.setStatus(1);
            user.setAuthProvider("facebook");

            userDao.insertFacebookUser(user);
            user = (email != null) ? userDao.getUserByEmail(email) : userDao.getUserByUsername(username);
        } else {
            if (user.getAuthProvider() == null || user.getAuthProvider().equals("local")) {
                userDao.updateAuthProvider(user.getEmail(), "facebook");
            }
        }
        return user;
    }
}