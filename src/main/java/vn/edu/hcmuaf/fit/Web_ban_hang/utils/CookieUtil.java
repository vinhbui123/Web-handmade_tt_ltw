package vn.edu.hcmuaf.fit.Web_ban_hang.utils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {

    // Key AES 16 bytes (128-bit) — thay đổi trong production
    private static final String SECRET_KEY = "hCmUaF@2025#WebS";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 ngày

    public static String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.substring(0, 16).getBytes(StandardCharsets.UTF_8), "AES");
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Ghép IV + ciphertext rồi encode Base64
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encrypt failed", e);
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            byte[] combined = Base64.getUrlDecoder().decode(encryptedText);

            byte[] iv = new byte[16];
            byte[] cipherText = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, cipherText, 0, cipherText.length);

            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.substring(0, 16).getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    // Tạo cookie remember me với userId đã mã hóa
    public static void setRememberCookie(HttpServletResponse response, int userId, String contextPath) {
        String encrypted = encrypt(String.valueOf(userId));
        Cookie cookie = new Cookie("REMEMBER_TOKEN", encrypted);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    // Xoá cookie remember me
    public static void clearRememberCookie(HttpServletResponse response, String contextPath) {
        Cookie cookie = new Cookie("REMEMBER_TOKEN", "");
        cookie.setMaxAge(0);
        cookie.setPath(contextPath.isEmpty() ? "/" : contextPath);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    // Đọc userId từ cookie, trả -1 nếu không có hoặc lỗi
    public static int getUserIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return -1;

        for (Cookie cookie : cookies) {
            if ("REMEMBER_TOKEN".equals(cookie.getName())) {
                String decrypted = decrypt(cookie.getValue());
                if (decrypted != null) {
                    try {
                        return Integer.parseInt(decrypted);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return -1;
    }
}
