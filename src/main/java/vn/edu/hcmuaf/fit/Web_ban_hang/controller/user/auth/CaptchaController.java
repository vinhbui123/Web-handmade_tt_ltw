package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.auth;



import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.CaptchaUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@WebServlet("/captcha")
public class CaptchaController extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Kiểm tra Referer để tránh request từ bên ngoài (hotlinking/spam từ domain khác)
        String referer = request.getHeader("Referer");
        if (referer == null || !referer.contains(request.getServerName())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        // 2. Giới hạn tần suất gọi API (Rate limiting): 1 giây / 1 lần / 1 session
        HttpSession session = request.getSession();
        Long lastCaptchaTime = (Long) session.getAttribute("lastCaptchaTime");
        long currentTime = System.currentTimeMillis();
        
        if (lastCaptchaTime != null && (currentTime - lastCaptchaTime) < 1000) {
            response.sendError(429, "Too Many Requests");
            return;
        }
        session.setAttribute("lastCaptchaTime", currentTime);

        // Sinh mã CAPTCHA
        String captchaText = CaptchaUtil.generateCaptcha();
        session.setAttribute("captcha", captchaText);

        // Tạo ảnh CAPTCHA
        BufferedImage captchaImage = CaptchaUtil.generateCaptchaImage(captchaText);
        if (captchaImage == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tạo CAPTCHA");
            return;
        }

        // Thiết lập header để tránh lưu cache
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Gửi ảnh CAPTCHA về client
        response.setContentType("image/png");
        ImageIO.write(captchaImage, "png", response.getOutputStream());
    }
}