package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Category;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CategoryService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.ProductService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.WishlistService;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet(urlPatterns = {"/home"})
public class HomeController extends HttpServlet {
    private final WishlistService wishlistService = new WishlistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        CategoryService categoryService = new CategoryService();
        List<Category> categories = categoryService.getAll();
        req.getSession().setAttribute("category", categories);

        // biến số lượng sản phẩm truyền vào js
        int itemsPerPage = 40;

        ProductService productService = new ProductService();
        List<Product> productViewest;

        // 2. Sử dụng biến này để lấy data
        productViewest = productService.getProductViewest(itemsPerPage);
        req.setAttribute("productViewest", productViewest);

        // 3. Gửi biến này sang index.jsp
        req.setAttribute("itemsPerPageConfig", itemsPerPage);

        // 4. Inject wishlist data
        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");
        if (user != null) {
            Set<Integer> wishlistIds = wishlistService.getProductIdsByUserId(user.getId());
            req.setAttribute("wishlistIds", wishlistIds);
            session.setAttribute("wishlistCount", wishlistIds.size());
        } else {
            req.setAttribute("wishlistIds", new HashSet<>());
        }

        req.getRequestDispatcher("index.jsp").forward(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }
}
