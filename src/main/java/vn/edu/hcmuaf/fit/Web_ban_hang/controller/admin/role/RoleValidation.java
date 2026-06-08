package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.role;

import vn.edu.hcmuaf.fit.Web_ban_hang.dao.CategoryDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Category;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.UserService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.*;

/**
 * Filter phân quyền truy cập trang Admin theo Role.
 *
 * Role 0: User thường       → Không truy cập admin
 * Role 1: Admin             → Toàn quyền
 * Role 2: Seller            → Tất cả trừ quản lý tài khoản & comment
 * Role 3: Mod Nhập Hàng     → Chỉ nhập hàng, kiểm tra tồn kho, xem sản phẩm
 * Role 4: Kiểm Duyệt Viên  → Chỉ quản lý tài khoản & comment
 */
@WebFilter("/*")
public class RoleValidation implements Filter {

    private final CategoryDao categoryDao = new CategoryDao();

    // Danh sách URL được phép truy cập cho từng role (trừ Admin - toàn quyền)
    private static final Map<Integer, Set<String>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        // Role 2: Seller — tất cả trừ quản lý tài khoản & comment
        Set<String> sellerUrls = new HashSet<>(Arrays.asList(
                "/admin",              // Dashboard
                "/adminProducts",      // Quản lý sản phẩm
                "/adminAdd",           // Thêm sản phẩm
                "/adminEdit",          // Sửa sản phẩm
                "/adminRemove",        // Xóa sản phẩm
                "/adminGetProduct",    // Lấy chi tiết sản phẩm
                "/uploadProductImage", // Upload ảnh sản phẩm
                "/adminCategorys",     // Quản lý danh mục
                "/adminMaterials",     // Quản lý chất liệu
                "/adminOrders",        // Quản lý đơn hàng
                "/confirmOrder",       // Xác nhận đơn hàng
                "/cancelOrder",        // Hủy đơn hàng
                "/adminInventory",     // Quản lý xuất nhập kho
                "/adminCoupons"        // Quản lý mã giảm giá
        ));
        ROLE_PERMISSIONS.put(2, sellerUrls);

        // Role 3: Mod Nhập Hàng — chỉ nhập hàng, kiểm tra tồn kho, xem sản phẩm
        Set<String> modImportUrls = new HashSet<>(Arrays.asList(
                "/admin",              // Dashboard
                "/adminProducts",      // Xem danh sách sản phẩm (chỉ xem)
                "/adminGetProduct",    // Xem chi tiết sản phẩm
                "/adminInventory"      // Quản lý xuất nhập kho
        ));
        ROLE_PERMISSIONS.put(3, modImportUrls);

        // Role 4: Kiểm Duyệt Viên — chỉ quản lý tài khoản & comment
        Set<String> moderatorUrls = new HashSet<>(Arrays.asList(
                "/admin",              // Dashboard
                "/adminUsers",         // Quản lý tài khoản
                "/adminComments"       // Quản lý đánh giá/comment
        ));
        ROLE_PERMISSIONS.put(4, moderatorUrls);
    }

    /**
     * Kiểm tra quyền truy cập dựa trên role và URI.
     *
     * @param role Vai trò của user (0-4)
     * @param uri  URI đang truy cập (đã bỏ contextPath)
     * @return true nếu được phép, false nếu bị từ chối
     */
    private boolean hasPermission(int role, String uri) {
        // Role 0: User thường — không được truy cập admin
        if (role == 0) return false;

        // Role 1: Admin — toàn quyền
        if (role == 1) return true;

        // Role 2, 3, 4: Kiểm tra theo danh sách URL được phép
        Set<String> allowedUrls = ROLE_PERMISSIONS.get(role);
        if (allowedUrls == null) return false; // Role không xác định

        // Kiểm tra URI có nằm trong danh sách cho phép không
        for (String allowedUrl : allowedUrls) {
            if (uri.endsWith(allowedUrl) || uri.contains(allowedUrl)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();

        // 1. Bỏ qua tài nguyên tĩnh và các trang công khai (login, register, access-denied)
        if (uri.endsWith("login.jsp") || uri.endsWith("register.jsp")
                || uri.endsWith("access-denied.jsp")
                || uri.contains("/login") || uri.contains("/register")
                || uri.contains("/api/") || uri.contains("/css/")
                || uri.contains("/js/") || uri.contains("/images/")
                || uri.contains("/fonts/") || uri.endsWith(".png")
                || uri.endsWith(".jpg") || uri.endsWith(".gif")
                || uri.endsWith(".svg") || uri.endsWith(".ico")
        ) {
            chain.doFilter(request, response);
            return;
        }

        // Tạo session nếu chưa có để lưu Category hoặc lấy User
        HttpSession session = req.getSession(true);
        User sessionUser = (User) session.getAttribute("user");

        // 2. Load Global Data (Danh mục sản phẩm cho Menu)
        if (session.getAttribute("category") == null) {
            List<Category> categories = categoryDao.getAll();
            session.setAttribute("category", categories);
        }

        // 3. Kiểm tra bảo mật cho trang Admin
        if (uri.contains("/admin") || uri.contains("/confirmOrder") || uri.contains("/cancelOrder")
                || uri.contains("/uploadProductImage")) {

            // 3.1. Kiểm tra đăng nhập
            if (sessionUser == null) {
                resp.sendRedirect(contextPath + "/login.jsp");
                return;
            }

            // 3.2. Kiểm tra trạng thái thực tế từ Database
            UserService userService = new UserService();
            User freshUser = userService.getById(sessionUser.getId());

            if (freshUser == null || freshUser.getStatus() == 0) {
                session.invalidate();
                resp.sendRedirect(contextPath + "/login.jsp");
                return;
            }

            // Cập nhật lại thông tin user mới nhất vào session
            session.setAttribute("user", freshUser);

            // 3.3. Kiểm tra quyền truy cập theo Role
            int role = freshUser.getRole();

            if (!hasPermission(role, uri)) {
                // Không có quyền → chuyển đến trang access-denied
                resp.sendRedirect(contextPath + "/access-denied.jsp");
                return;
            }
        }

        // 4. Cho phép request đi tiếp
        chain.doFilter(request, response);
    }
}