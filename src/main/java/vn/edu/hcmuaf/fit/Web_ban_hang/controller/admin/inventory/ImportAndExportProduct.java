package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.inventory;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.InventoryDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.ProductDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Category;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CategoryService;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/adminInventory")
public class ImportAndExportProduct extends HttpServlet {
    private InventoryDao inventoryDao;
    private ProductDao productDao;

    @Override
    public void init() {
        inventoryDao = new InventoryDao();
        productDao = new ProductDao();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) { resp.sendRedirect("login.jsp"); return; }

        try {
            int productId = Integer.parseInt(req.getParameter("productId"));
            int quantity = Integer.parseInt(req.getParameter("quantity"));
            String type = req.getParameter("type");

            boolean success = false;
            // Xử lý linh hoạt cả 4 loại giao dịch
            if ("import".equalsIgnoreCase(type) || "returned".equalsIgnoreCase(type)) {
                // Nhập hàng hoặc Khách trả hàng (đều làm tăng kho)
                success = inventoryDao.importProduct(productId, quantity, user.getId());
            } else if ("export".equalsIgnoreCase(type) || "damaged".equalsIgnoreCase(type)) {
                // Xuất hàng hoặc Hàng lỗi (đều làm giảm kho)
                success = inventoryDao.exportProduct(productId, quantity, user.getId(), type);
            }

            req.getSession().setAttribute("message", success ? " Thao tác thành công!" : " Thao tác thất bại (Kiểm tra tồn kho)!");
            req.getSession().setAttribute("messageType", success ? "success" : "error");

        } catch (NumberFormatException e) {
            req.getSession().setAttribute("message", " Dữ liệu không hợp lệ.");
            req.getSession().setAttribute("messageType", "error");
        }
        resp.sendRedirect(req.getContextPath() + "/adminInventory");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 1. Nhận toàn bộ tham số từ AJAX
            String searchKeyword = req.getParameter("search");
            String sortBy = req.getParameter("sortBy");
            String order = req.getParameter("order");

            String categoryIdParam = req.getParameter("category");
            Integer categoryId = (categoryIdParam != null && !categoryIdParam.isEmpty())
                    ? Integer.parseInt(categoryIdParam) : null;

            int page = 1;
            int pageSize = 10;
            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
            }
            int offset = (page - 1) * pageSize;

            // 2. GỌI HÀM HỢP NHẤT (Sử dụng chung ProductDao)
            // Lưu ý: Trang Nhập xuất không lọc theo Chất liệu nên ta truyền 'null' vào vị trí materialId
            List<Product> products = productDao.getAdminProductsUnified(searchKeyword, categoryId, null, sortBy, order, offset, pageSize);
            int totalProducts = productDao.getTotalCountUnified(searchKeyword, categoryId, null);
            int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

            // 3. Đẩy biến sang JSP
            req.setAttribute("products", products);
            req.setAttribute("searchKeyword", searchKeyword != null ? searchKeyword.trim() : "");
            req.setAttribute("selectedCategoryId", categoryId); // Đổi tên biến cho khớp với JSP
            req.setAttribute("sortBy", sortBy);
            req.setAttribute("order", order);
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages);

            List<Category> categories = new CategoryService().getAll();
            req.setAttribute("category", categories);

            req.getRequestDispatcher("ad-inventory.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "Lỗi phân trang: " + e.getMessage());
        }
    }
}