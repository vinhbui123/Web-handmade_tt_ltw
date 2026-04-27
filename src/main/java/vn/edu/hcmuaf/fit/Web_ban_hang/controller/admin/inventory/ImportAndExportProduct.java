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
            //Xác định trang hiện tại và kích thước trang
            int page = 1;
            int pageSize = 10; // Mỗi trang hiện 10 món
            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
            }
            int offset = (page - 1) * pageSize;

            // Lấy bộ lọc Category
            String categoryIdParam = req.getParameter("category");
            Integer categoryId = (categoryIdParam != null && !categoryIdParam.isEmpty())
                    ? Integer.parseInt(categoryIdParam) : null;

            // GỌI DAO ĐỂ LẤY DỮ LIỆU PHÂN TRANG (Thay vì getAll)
            List<Product> products = productDao.getProductsPaged(true, categoryId, offset, pageSize);

            //  TÍNH TỔNG SỐ TRANG
            int totalProducts = productDao.getTotalCount(true, categoryId);
            int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

            // Đẩy các biến này sang JSP để JSTL xử lý
            req.setAttribute("products", products);
            req.setAttribute("totalPages", totalPages);
            req.setAttribute("currentPage", page);
            req.setAttribute("selectedCategory", categoryId);

            // Các phần CategoryService và Message giữ nguyên...
            List<Category> categories = new CategoryService().getAll();
            req.setAttribute("category", categories);

            req.getRequestDispatcher("ad-inventory.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "Lỗi phân trang: " + e.getMessage());
        }
    }
}