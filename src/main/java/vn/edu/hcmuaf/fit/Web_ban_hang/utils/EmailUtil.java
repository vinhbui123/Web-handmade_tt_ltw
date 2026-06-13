package vn.edu.hcmuaf.fit.Web_ban_hang.utils;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.ServletContext;


public class EmailUtil {

    private static final Logger log = LoggerFactory.getLogger(EmailUtil.class);
    private static Session buildSession(ServletContext ctx) {
        String fromEmail    = ctx.getInitParameter("MAIL_FROM");
        String fromPassword = ctx.getInitParameter("MAIL_PASSWORD");

        if (fromEmail == null || fromEmail.isBlank() || fromPassword == null || fromPassword.isBlank()) {
            throw new IllegalStateException(
                "Chưa cấu hình MAIL_FROM / MAIL_PASSWORD trong web.xml. " +
                "Vui lòng thêm <context-param> tương ứng.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host",            "smtp.gmail.com");
        props.put("mail.smtp.port",            "587");
        props.put("mail.smtp.auth",            "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, fromPassword);
            }
        });
    }
    public static void sendOtpEmail(ServletContext ctx, String toEmail, String otp)
            throws MessagingException, UnsupportedEncodingException {

        Session session  = buildSession(ctx);
        String fromEmail = ctx.getInitParameter("MAIL_FROM");

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail, "Handmade Shop", "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Mã OTP đặt lại mật khẩu - Handmade Shop");

        String htmlContent =
            "<div style='font-family:Inter,Arial,sans-serif;max-width:520px;margin:0 auto;'>" +
            "  <div style='background:#1a1a2e;padding:28px;text-align:center;border-radius:12px 12px 0 0;'>" +
            "    <h1 style='color:#e2b96f;margin:0;font-size:24px;letter-spacing:1px;'>🔑 Handmade Shop</h1>" +
            "  </div>" +
            "  <div style='background:#ffffff;padding:36px 40px;border:1px solid #e8e8e8;'>" +
            "    <h2 style='color:#1a1a2e;margin-top:0;font-size:20px;'>Xác nhận đặt lại mật khẩu</h2>" +
            "    <p style='color:#555;font-size:15px;line-height:1.6;'>" +
            "      Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn." +
            "    </p>" +
            "    <p style='color:#555;font-size:15px;line-height:1.6;'>Đây là mã OTP của bạn:</p>" +
            "    <div style='text-align:center;margin:28px 0;'>" +
            "      <span style='display:inline-block;background:#f5f5f5;border:2px dashed #e2b96f;" +
            "                   border-radius:10px;padding:18px 40px;font-size:36px;font-weight:700;" +
            "                   letter-spacing:12px;color:#1a1a2e;font-family:monospace;'>" +
            otp +
            "      </span>" +
            "    </div>" +
            "    <p style='color:#555;font-size:14px;line-height:1.6;'>" +
            "      Mã OTP có hiệu lực trong <strong>10 phút</strong>. Không chia sẻ mã này với bất kỳ ai." +
            "    </p>" +
            "    <p style='color:#999;font-size:13px;'>Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.</p>" +
            "    <hr style='border:none;border-top:1px solid #eee;margin:24px 0;'/>" +
            "  </div>" +
            "  <div style='background:#f5f5f5;padding:14px;text-align:center;border-radius:0 0 12px 12px;'>" +
            "    <p style='color:#aaa;font-size:12px;margin:0;'>© 2026 Handmade Shop. Tất cả quyền được bảo lưu.</p>" +
            "  </div>" +
            "</div>";

        message.setContent(htmlContent, "text/html; charset=UTF-8");
        Transport.send(message);
        log.info("OTP email sent to: {}", toEmail);
    }

    @Deprecated
    public static void sendResetPasswordEmail(ServletContext ctx, String toEmail, String resetLink)
            throws MessagingException, UnsupportedEncodingException {

        Session session  = buildSession(ctx);
        String fromEmail = ctx.getInitParameter("MAIL_FROM");

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail, "Handmade Shop", "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Đặt lại mật khẩu - Handmade Shop");

        String htmlContent =
            "<div style='font-family:Arial,sans-serif;max-width:560px;margin:0 auto;'>" +
            "  <div style='background:#1a1a2e;padding:30px;text-align:center;border-radius:8px 8px 0 0;'>" +
            "    <h1 style='color:#e2b96f;margin:0;font-size:26px;letter-spacing:1px;'>🔑 Handmade Shop</h1>" +
            "  </div>" +
            "  <div style='background:#ffffff;padding:36px;border:1px solid #e8e8e8;'>" +
            "    <h2 style='color:#1a1a2e;margin-top:0;'>Đặt lại mật khẩu của bạn</h2>" +
            "    <p style='color:#555;font-size:15px;line-height:1.6;'>" +
            "      Nhấn vào nút bên dưới để tạo mật khẩu mới. Liên kết sẽ hết hạn sau <strong>30 phút</strong>." +
            "    </p>" +
            "    <div style='text-align:center;margin:32px 0;'>" +
            "      <a href='" + resetLink + "' " +
            "         style='background:#e2b96f;color:#1a1a2e;padding:14px 36px;border-radius:6px;" +
            "                text-decoration:none;font-weight:bold;font-size:16px;display:inline-block;'>" +
            "        Đặt lại mật khẩu" +
            "      </a>" +
            "    </div>" +
            "    <p style='color:#999;font-size:13px;'>Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này.</p>" +
            "    <hr style='border:none;border-top:1px solid #eee;margin:24px 0;'/>" +
            "  </div>" +
            "  <div style='background:#f5f5f5;padding:16px;text-align:center;border-radius:0 0 8px 8px;'>" +
            "    <p style='color:#aaa;font-size:12px;margin:0;'>© 2026 Handmade Shop. Tất cả quyền được bảo lưu.</p>" +
            "  </div>" +
            "</div>";

        message.setContent(htmlContent, "text/html; charset=UTF-8");
        Transport.send(message);
        log.info("Reset-password email sent to: {}", toEmail);
    }
}
