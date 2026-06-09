package vn.edu.hcmuaf.fit.Web_ban_hang.controller.admin.comments;

import java.io.IOException;
import java.util.List;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if (action == null || action.equals("list")) {
            CommentDao dao = new CommentDao();
            int totalComments = dao.getTotalCommentsCount();
            int totalPages = (int) Math.ceil((double) totalComments / pageSize);
            int offset = (page - 1) * pageSize;

            List<Comment> comments = dao.getCommentsByPage(offset, pageSize);

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
            resp.sendRedirect(req.getContextPath() + "/adminComments");
        } else {
            resp.sendRedirect(req.getContextPath() + "/adminComments");
        }
        resp.sendRedirect(req.getContextPath() + "/adminComments");
    }
}