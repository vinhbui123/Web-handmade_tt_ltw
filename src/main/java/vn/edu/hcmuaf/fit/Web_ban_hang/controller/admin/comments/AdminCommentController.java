package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.comments;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.CommentDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Comment;

@WebServlet(urlPatterns = "/adminComments")
public class AdminCommentController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminCommentController.class);
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private static final int PAGE_SIZE = 10;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        // AJAX: trả JSON phân trang
        if ("list_ajax".equals(action)) {
            int page = 1;
            try {
                page = Integer.parseInt(req.getParameter("page"));
            } catch (Exception ignored) {}

            CommentDao dao = new CommentDao();
            int totalComments = dao.getTotalCommentsCount();
            int totalPages = (int) Math.ceil((double) totalComments / PAGE_SIZE);
            int offset = (page - 1) * PAGE_SIZE;

            List<Comment> comments = dao.getCommentsByPage(offset, PAGE_SIZE);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("comments", comments);
            responseMap.put("totalPages", totalPages);
            responseMap.put("totalComments", totalComments);
            responseMap.put("currentPage", page);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(responseMap));
            out.flush();
            return;
        }

        // Mặc định: forward sang JSP
        req.getRequestDispatcher("/ad-comment.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        boolean isAjax = "true".equals(req.getParameter("ajax"));

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if ("delete".equals(action)) {
            boolean success = false;
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                CommentDao dao = new CommentDao();
                dao.deleteCommentById(id);
                success = true;
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
            }

            if (isAjax) {
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(Map.of("success", success)));
                out.flush();
            } else {
                resp.sendRedirect(req.getContextPath() + "/adminComments");
            }
            return;
        }

        if (isAjax) {
            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(Map.of("success", false, "message", "Unknown action")));
            out.flush();
        } else {
            resp.sendRedirect(req.getContextPath() + "/adminComments");
        }
    }
}