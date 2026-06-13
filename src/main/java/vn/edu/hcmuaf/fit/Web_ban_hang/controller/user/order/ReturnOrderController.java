package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.OrderDao;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@WebServlet(name = "ReturnOrderServlet", value = "/returnOrder")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class ReturnOrderController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            int orderId = Integer.parseInt(request.getParameter("orderId"));
            String reason = request.getParameter("reason");
            String description = request.getParameter("description");
            Part filePart = request.getPart("proofImage"); // Lấy file ảnh minh chứng

            String imgPath = "";
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                // Lưu ảnh vào thư mục images/returns
                String uploadPath = getServletContext().getRealPath("/images/returns");
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                String filePath = uploadPath + File.separator + fileName;
                filePart.write(filePath);

                imgPath = "images/returns/" + fileName;
            }

            // Gọi DAO để lưu vào Database
            OrderDao orderDao = new OrderDao();
            boolean success = orderDao.createReturnRequest(orderId, reason, description, imgPath);

            if (success) {
                // Trả về thông báo thành công và load lại trang Đơn Mua
                request.getSession().setAttribute("message", "Đã gửi yêu cầu hoàn trả thành công! Chờ Admin phê duyệt.");
            } else {
                request.getSession().setAttribute("error", "Có lỗi xảy ra khi gửi yêu cầu hoàn trả.");
            }

            // Redirect về lại trang mua hàng
            response.sendRedirect(request.getContextPath() + "/purchase");

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Lỗi xử lý hoàn trả: " + e.getMessage());
        }
    }
}