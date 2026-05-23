package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.product;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.MaterialDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Category;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Material;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CategoryService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.ProductService;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.ProductDao;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "Show", value = "/adminProducts")
public class Show extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CategoryService categoryService = new CategoryService();
        List<Category> categories = categoryService.getAll();
        request.setAttribute("category", categories);

        MaterialDao materialDao = new MaterialDao();
        List<Material> materials = materialDao.getAll();
        request.setAttribute("materials", materials);

        // Khai báo ProductDao để gọi 2 hàm phân trang thần thánh
        ProductDao productDao = new ProductDao();

        String searchKeyword = request.getParameter("search");

        //  Tính toán phân trang
        int page = 1;
        int pageSize = 10;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            page = Integer.parseInt(pageParam);
        }
        int offset = (page - 1) * pageSize;
        List<Product> products;
        int totalProducts = 0;

        // Lấy bộ lọc Danh mục
        String categoryIdStr = request.getParameter("category");
        Integer categoryId = null;
        String categoryName = "Tất cả sản phẩm";
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            // ---> LUỒNG 1: ADMIN ĐANG DÙNG THANH TÌM KIẾM
            String keyword = searchKeyword.trim();
            products = productDao.searchProductsAdminFuzzyPaged(keyword, offset, pageSize);
            totalProducts = productDao.countSearchProductsAdminFuzzy(keyword);

            // Đẩy từ khóa ngược lại JSP để hiển thị lên ô input
            request.setAttribute("searchKeyword", keyword);
            categoryName = "Kết quả tìm kiếm: '" + keyword + "'";

        } else {
            if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
                try {
                    categoryId = Integer.parseInt(categoryIdStr);
                    categoryName = categoryService.getCategoryNameById(categoryId);
                } catch (NumberFormatException e) {
                    categoryId = null;
                }
            }

            // LẤY DỮ LIỆU ĐÃ PHÂN TRANG
            products = productDao.getProductsPaged(true, categoryId, offset, pageSize);
            totalProducts = productDao.getTotalCount(true, categoryId);
            request.setAttribute("selectedCategoryId", categoryId);
        }
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

        request.setAttribute("products", products);
        request.setAttribute("selectedCategory", categoryName);
        request.setAttribute("selectedCategoryId", categoryId);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        request.getRequestDispatcher("ad-product.jsp").forward(request, response);
    }
}
