package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.role;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.CategoryDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Category;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CartService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.UserService;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.CookieUtil;

/**
 * Role 0: User thường       → Không truy cập admin
 * Role 1: Admin             → Toàn quyền
 * Role 2: Seller            → Tất cả trừ quản lý tài khoản & comment
 * Role 3: Mod Nhập Hàng     → Chỉ nhập hàng, kiểm tra tồn kho, xem sản phẩm
 * Role 4: Kiểm Duyệt Viên  → Chỉ quản lý tài khoản & comment
 */
@WebFilter("/*")
public class RoleValidation implements Filter {

    private final CategoryDao categoryDao = new CategoryDao();

    private static final Map<Integer, Set<String>> ROLE_PERMISSIONS = new HashMap<>();

    static {
        Set<String> sellerUrls = new HashSet<>(Arrays.asList(
                "/admin",              
                "/adminProducts",     
                "/adminAdd",          
                "/adminEdit",          
                "/adminRemove",       
                "/adminGetProduct",   
                "/uploadProductImage", 
                "/adminCategorys",     
                "/adminMaterials",     
                "/adminOrders",        
                "/confirmOrder",       
                "/cancelOrder",        
                "/adminInventory",     
                "/adminCoupons"        
        ));
        ROLE_PERMISSIONS.put(2, sellerUrls);

        Set<String> modImportUrls = new HashSet<>(Arrays.asList(
                "/admin",              
                "/adminProducts",    
                "/adminGetProduct",   
                "/adminInventory"      
        ));
        ROLE_PERMISSIONS.put(3, modImportUrls);
        Set<String> moderatorUrls = new HashSet<>(Arrays.asList(
                "/admin",              
                "/adminUsers",         
                "/adminComments"       
        ));
        ROLE_PERMISSIONS.put(4, moderatorUrls);
    }

    private boolean hasPermission(int role, String uri) {
        if (role == 0) return false;
        if (role == 1) return true;
        Set<String> allowedUrls = ROLE_PERMISSIONS.get(role);
        if (allowedUrls == null) return false;

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

        HttpSession session = req.getSession(true);
        User sessionUser = (User) session.getAttribute("user");

        // Auto-login từ cookie Remember Me
        if (sessionUser == null) {
            int cookieUserId = CookieUtil.getUserIdFromCookie(req);
            if (cookieUserId > 0) {
                UserService us = new UserService();
                User cookieUser = us.getById(cookieUserId);
                if (cookieUser != null && cookieUser.getStatus() != 0) {
                    session.setAttribute("user", cookieUser);
                    if (session.getAttribute("cart") == null) {
                        session.setAttribute("cart", new CartService());
                    }
                    sessionUser = cookieUser;
                } else {
                    CookieUtil.clearRememberCookie(resp, contextPath);
                }
            }
        }

        if (session.getAttribute("category") == null) {
            List<Category> categories = categoryDao.getAll();
            session.setAttribute("category", categories);
        }

        if (uri.contains("/admin") || uri.contains("/confirmOrder") || uri.contains("/cancelOrder")
                || uri.contains("/uploadProductImage")) {

            if (sessionUser == null) {
                resp.sendRedirect(contextPath + "/login.jsp");
                return;
            }

            UserService userService = new UserService();
            User freshUser = userService.getById(sessionUser.getId());

            if (freshUser == null || freshUser.getStatus() == 0) {
                session.invalidate();
                resp.sendRedirect(contextPath + "/login.jsp");
                return;
            }

            session.setAttribute("user", freshUser);

            int role = freshUser.getRole();

            if (!hasPermission(role, uri)) {
                resp.sendRedirect(contextPath + "/access-denied.jsp");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}