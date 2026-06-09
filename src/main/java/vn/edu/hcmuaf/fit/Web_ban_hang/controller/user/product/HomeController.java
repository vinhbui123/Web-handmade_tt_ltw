package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.product;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        productViewest = productService.getProductViewest(itemsPerPage);
        req.setAttribute("productViewest", productViewest);

        req.setAttribute("itemsPerPageConfig", itemsPerPage);

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
