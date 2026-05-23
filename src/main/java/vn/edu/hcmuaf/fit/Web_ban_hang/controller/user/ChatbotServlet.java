package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/chatbot")
public class ChatbotServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ChatbotServlet.class);
    private static final String GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";
    private static String API_KEY;

    private static final String SYSTEM_PROMPT =
            "Bạn là trợ lý AI của cửa hàng HandMade Craft — chuyên bán các sản phẩm thủ công mỹ nghệ. " +
            "Hãy trả lời ngắn gọn, thân thiện, bằng tiếng Việt. " +
            "Nếu khách hỏi về sản phẩm, giá cả, vận chuyển, đổi trả, bạn hãy tư vấn nhiệt tình. " +
            "Nếu khách hỏi ngoài phạm vi cửa hàng, hãy lịch sự từ chối và hướng họ quay lại chủ đề mua sắm. " +
            "Thông tin cửa hàng: Địa chỉ Stown Thủ Đức, Bình Chiểu, Thủ Đức, TPHCM. " +
            "SĐT: 0343 031 030. Email: handmadedcraft@gmail.com. " +
            "Chính sách: Đổi trả trong 7 ngày, miễn phí vận chuyển đơn trên 500.000đ.";

    @Override
    public void init() throws ServletException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("groq.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                API_KEY = props.getProperty("GROQ_API_KEY", "");
                if (API_KEY.isEmpty()) {
                    log.warn("Groq API key chưa được cấu hình! Hãy cập nhật file groq.properties");
                }
            } else {
                log.error("Không tìm thấy file groq.properties trong classpath");
            }
        } catch (IOException e) {
            log.error("Lỗi khi đọc groq.properties", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Đọc message từ request
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        JsonObject requestJson = JsonParser.parseString(body.toString()).getAsJsonObject();
        String userMessage = requestJson.get("message").getAsString();

        if (userMessage == null || userMessage.trim().isEmpty()) {
            sendError(response, "Tin nhắn không được để trống");
            return;
        }

        if (API_KEY == null || API_KEY.isEmpty()) {
            sendError(response, "AI chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
            return;
        }

        // Quản lý lịch sử hội thoại trong session (OpenAI format)
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        JsonArray chatHistory = (JsonArray) session.getAttribute("chatHistory");
        if (chatHistory == null) {
            chatHistory = new JsonArray();
            session.setAttribute("chatHistory", chatHistory);
        }

        // Thêm tin nhắn người dùng vào lịch sử
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        chatHistory.add(userMsg);

        // Giới hạn lịch sử (giữ 20 tin nhắn gần nhất)
        while (chatHistory.size() > 20) {
            chatHistory.remove(0);
        }

        try {
            String aiReply = callGroqAPI(chatHistory);

            // Thêm phản hồi AI vào lịch sử
            JsonObject assistantMsg = new JsonObject();
            assistantMsg.addProperty("role", "assistant");
            assistantMsg.addProperty("content", aiReply);
            chatHistory.add(assistantMsg);

            // Trả về response
            JsonObject result = new JsonObject();
            result.addProperty("reply", aiReply);
            response.getWriter().write(result.toString());

        } catch (Exception e) {
            log.error("Lỗi khi gọi Groq API", e);
            sendError(response, "Xin lỗi, hiện tại tôi không thể trả lời. Vui lòng thử lại sau.");
            // Xóa tin nhắn user vừa thêm vì chưa được xử lý
            if (chatHistory.size() > 0) chatHistory.remove(chatHistory.size() - 1);
        }
    }

    private String callGroqAPI(JsonArray chatHistory) throws IOException {
        // Build messages array with system prompt
        JsonArray messages = new JsonArray();

        // System message
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", SYSTEM_PROMPT);
        messages.add(systemMsg);

        // Add chat history
        for (int i = 0; i < chatHistory.size(); i++) {
            messages.add(chatHistory.get(i));
        }

        // Build request body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", GROQ_MODEL);
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 1024);

        // Send HTTP request
        HttpURLConnection conn = (HttpURLConnection) new URL(GROQ_ENDPOINT).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        InputStream is = (responseCode >= 200 && responseCode < 300)
                ? conn.getInputStream() : conn.getErrorStream();

        StringBuilder responseText = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseText.append(line);
            }
        }

        if (responseCode < 200 || responseCode >= 300) {
            log.error("Groq API error ({}): {}", responseCode, responseText);
            throw new IOException("Groq API returned status " + responseCode);
        }

        // Parse response (OpenAI format)
        JsonObject responseJson = JsonParser.parseString(responseText.toString()).getAsJsonObject();
        return responseJson
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("reply", message);
        error.addProperty("error", true);
        response.getWriter().write(error.toString());
    }
}
