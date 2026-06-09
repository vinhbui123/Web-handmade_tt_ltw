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

        ProductDao productDao = new ProductDao();
        String searchKeyword = request.getParameter("search");
        String sortBy = request.getParameter("sortBy");
        String order = request.getParameter("order");

        String categoryIdStr = request.getParameter("category");
        Integer categoryId = null;
        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            try { categoryId = Integer.parseInt(categoryIdStr); } catch (Exception e) {}
        }

        String materialIdStr = request.getParameter("material");
        Integer materialId = null;
        if (materialIdStr != null && !materialIdStr.isEmpty()) {
            try { materialId = Integer.parseInt(materialIdStr); } catch (Exception e) {}
        }

        int page = 1;
        int pageSize = 10;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            page = Integer.parseInt(pageParam);
        }
        int offset = (page - 1) * pageSize;
        List<Product> products = productDao.getAdminProductsUnified(searchKeyword, categoryId, materialId, sortBy, order, offset, pageSize);
        int totalProducts = productDao.getTotalCountUnified(searchKeyword, categoryId, materialId);
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

        String categoryName = "Tất cả sản phẩm";
        if (categoryId != null) {
            categoryName = categoryService.getCategoryNameById(categoryId);
        } else if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            categoryName = "Kết quả tìm kiếm: '" + searchKeyword.trim() + "'";
        }

        request.setAttribute("products", products);
        request.setAttribute("searchKeyword", searchKeyword != null ? searchKeyword.trim() : "");
        request.setAttribute("selectedCategory", categoryName);
        request.setAttribute("selectedCategoryId", categoryId);
        request.setAttribute("selectedMaterialId", materialId);
        request.setAttribute("sortBy", sortBy);
        request.setAttribute("order", order);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalProducts", totalProducts);
        request.getRequestDispatcher("ad-product.jsp").forward(request, response);
    }
}
