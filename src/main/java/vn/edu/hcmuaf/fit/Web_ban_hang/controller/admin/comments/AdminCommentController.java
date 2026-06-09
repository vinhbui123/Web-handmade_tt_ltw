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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if (action == null || action.equals("list")) {
            CommentDao dao = new CommentDao();
            List<Comment> comments = dao.getAllComments();

            req.setAttribute("comments", comments);
            req.getRequestDispatcher("/ad-comment.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("delete".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                CommentDao dao = new CommentDao();
                dao.deleteCommentById(id);
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
            }
            resp.sendRedirect(req.getContextPath() + "/adminComments");
        } else {
            resp.sendRedirect(req.getContextPath() + "/adminComments");
        }
    }
}