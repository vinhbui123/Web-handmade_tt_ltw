package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user;

import java.io.BufferedReader;
import java.io.IOException;

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
import vn.edu.hcmuaf.fit.Web_ban_hang.services.ChatbotService;

@WebServlet("/api/chatbot")
public class ChatbotServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ChatbotServlet.class);
    private ChatbotService chatbotService;

    @Override
    public void init() throws ServletException {
        chatbotService = new ChatbotService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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

        if (!chatbotService.isApiKeyConfigured()) {
            sendError(response, "AI chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
            return;
        }

        HttpSession session = request.getSession();
        JsonArray chatHistory = (JsonArray) session.getAttribute("chatHistory");
        if (chatHistory == null) {
            chatHistory = new JsonArray();
            session.setAttribute("chatHistory", chatHistory);
        }

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        chatHistory.add(userMsg);
        
        while (chatHistory.size() > 20) {
            chatHistory.remove(0);
        }

        try {
            String aiReply = chatbotService.generateReply(userMessage, chatHistory);

            JsonObject assistantMsg = new JsonObject();
            assistantMsg.addProperty("role", "assistant");
            assistantMsg.addProperty("content", aiReply);
            chatHistory.add(assistantMsg);
            JsonObject result = new JsonObject();
            result.addProperty("reply", aiReply);
            response.getWriter().write(result.toString());

        } catch (IOException e) {
            log.error("Lỗi khi gọi Chatbot Service", e);
            sendError(response, "Xin lỗi, hiện tại tôi không thể trả lời. Vui lòng thử lại sau.");
            if (chatHistory.size() > 0) chatHistory.remove(chatHistory.size() - 1);
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("reply", message);
        error.addProperty("error", true);
        response.getWriter().write(error.toString());
    }
}
