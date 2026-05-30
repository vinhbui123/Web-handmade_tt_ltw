package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.wishlist;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.WishlistService;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.ReadJsonUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "WishlistController", urlPatterns = {"/wishlist", "/api/wishlist"})
public class WishlistController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(WishlistController.class);
    private final WishlistService wishlistService = new WishlistService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // Kiểm tra đăng nhập
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        List<Product> wishlistProducts = wishlistService.getProductsByUserId(user.getId());
        request.setAttribute("wishlistProducts", wishlistProducts);
        request.getRequestDispatcher("wishlist.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        Map<String, Object> result = new HashMap<>();

        // Kiểm tra đăng nhập
        if (user == null) {
            result.put("status", false);
            result.put("message", "Bạn cần đăng nhập để sử dụng chức năng này!");
            result.put("redirect", "login.jsp");
            out.print(gson.toJson(result));
            out.flush();
            return;
        }

        try {
            String jsonData = ReadJsonUtil.read(request);
            JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
            int productId = jsonObject.get("productId").getAsInt();

            String action = jsonObject.has("action") ? jsonObject.get("action").getAsString() : "toggle";

            if ("remove".equals(action)) {
                wishlistService.remove(user.getId(), productId);
                result.put("status", true);
                result.put("action", "removed");
                result.put("message", "Đã xóa khỏi danh sách yêu thích!");
            } else {
                // Toggle: thêm nếu chưa có, xóa nếu đã có
                boolean added = wishlistService.toggle(user.getId(), productId);
                result.put("status", true);
                result.put("action", added ? "added" : "removed");
                result.put("message", added
                        ? "Đã thêm vào danh sách yêu thích!"
                        : "Đã xóa khỏi danh sách yêu thích!");
            }

            int count = wishlistService.countByUserId(user.getId());
            result.put("count", count);

        } catch (Exception e) {
            log.error("Lỗi xử lý wishlist: {}", e.getMessage(), e);
            result.put("status", false);
            result.put("message", "Lỗi xử lý: " + e.getMessage());
        }

        out.print(gson.toJson(result));
        out.flush();
    }
}
