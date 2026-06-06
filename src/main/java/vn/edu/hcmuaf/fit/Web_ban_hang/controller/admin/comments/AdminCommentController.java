package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.comments;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.CommentDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Comment;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = "/adminComments")
public class AdminCommentController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminCommentController.class);
    // Format thời gian cho Gson để hiển thị đẹp trên JSP
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        // Gọi AJAX để lấy dữ liệu trang
        if ("list_ajax".equals(action)) {
            int page = 1;
            int pageSize = 10;
            if (req.getParameter("page") != null) {
                page = Integer.parseInt(req.getParameter("page"));
            }

            CommentDao dao = new CommentDao();
            int totalComments = dao.getTotalCommentsCount();
            int totalPages = (int) Math.ceil((double) totalComments / pageSize);
            int offset = (page - 1) * pageSize;

            List<Comment> comments = dao.getCommentsByPage(offset, pageSize);

            // Đóng gói dữ liệu trả về Frontend
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
        } else {
            // Lần đầu tiên vào trang thì trả về khung JSP
            req.getRequestDispatcher("/ad-comment.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        boolean isAjax = "true".equals(req.getParameter("ajax"));

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
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("success", success);
                resp.getWriter().print(jsonResponse.toString());
                return;
            }
        }
        resp.sendRedirect(req.getContextPath() + "/adminComments");
    }
}