package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
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
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Category;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.CategoryService;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.ProductService;

@WebServlet("/api/chatbot")
public class ChatbotServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ChatbotServlet.class);
    private static final String GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";
    private static String API_KEY;

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();

    private static final String SYSTEM_PROMPT =
            "Bạn là trợ lý AI của cửa hàng HandMade Craft — chuyên bán các sản phẩm thủ công mỹ nghệ. " +
            "Hãy trả lời ngắn gọn, thân thiện, bằng tiếng Việt. " +
            "Nếu khách hỏi về sản phẩm, giá cả, vận chuyển, đổi trả, bạn hãy tư vấn nhiệt tình. " +
            "Nếu khách hỏi ngoài phạm vi cửa hàng, hãy lịch sự từ chối và hướng họ quay lại chủ đề mua sắm. " +
            "Thông tin cửa hàng: Địa chỉ Stown Thủ Đức, Bình Chiểu, Thủ Đức, TPHCM. " +
            "SĐT: 0343 031 030. Email: handmadedcraft@gmail.com. " +
            "Chính sách: Đổi trả trong 7 ngày, miễn phí vận chuyển đơn trên 500.000đ. " +
            "Khi trả lời về sản phẩm, hãy sử dụng DỮ LIỆU THỰC TẾ từ hệ thống được cung cấp bên dưới. " +
            "Nếu khách hỏi giá, hãy hiển thị giá gốc và giá sau giảm (nếu có). " +
            "Định dạng tiền VNĐ có dấu chấm phân cách hàng nghìn (ví dụ: 150.000đ).";

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

        // Quản lý lịch sử hội thoại trong session
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
            // Truy vấn dữ liệu thực từ database dựa trên câu hỏi
            String dbContext = buildDatabaseContext(userMessage);
            String aiReply = callGroqAPI(chatHistory, dbContext);

            // Thêm phản hồi AI vào lịch sử
            JsonObject assistantMsg = new JsonObject();
            assistantMsg.addProperty("role", "assistant");
            assistantMsg.addProperty("content", aiReply);
            chatHistory.add(assistantMsg);

            // Trả về response
            JsonObject result = new JsonObject();
            result.addProperty("reply", aiReply);
            response.getWriter().write(result.toString());

        } catch (IOException e) {
            log.error("Lỗi khi gọi Groq API", e);
            sendError(response, "Xin lỗi, hiện tại tôi không thể trả lời. Vui lòng thử lại sau.");
            if (chatHistory.size() > 0) chatHistory.remove(chatHistory.size() - 1);
        }
    }

    /**
     * Xây dựng context từ database dựa trên câu hỏi của người dùng.
     * Phân tích intent và truy vấn dữ liệu thực tế.
     */
    private String buildDatabaseContext(String userMessage) {
        StringBuilder context = new StringBuilder();
        String lowerMsg = userMessage.toLowerCase();

        try {
            // --- Top sản phẩm xem nhiều nhất ---
            if (containsAny(lowerMsg, "xem nhiều", "phổ biến", "hot", "bán chạy", "top", "nổi bật", "trending", "được xem")) {
                List<Product> topViewed = productService.getProductViewest(10);
                if (!topViewed.isEmpty()) {
                    context.append("\n TOP 10 SẢN PHẨM XEM NHIỀU NHẤT:\n");
                    for (int i = 0; i < topViewed.size(); i++) {
                        Product p = topViewed.get(i);
                        context.append(formatProductInfo(i + 1, p));
                    }
                }
            }

            // --- Sản phẩm đánh giá cao ---
            if (containsAny(lowerMsg, "đánh giá", "rating", "tốt nhất", "chất lượng", "recommend", "gợi ý", "nên mua")) {
                List<Product> topRated = productService.getTopRatedProducts();
                if (!topRated.isEmpty()) {
                    context.append("\n SẢN PHẨM ĐÁNH GIÁ CAO (>4 sao):\n");
                    int limit = Math.min(topRated.size(), 10);
                    for (int i = 0; i < limit; i++) {
                        Product p = topRated.get(i);
                        context.append(formatProductInfo(i + 1, p));
                    }
                }
            }

            // --- Tìm kiếm sản phẩm theo từ khóa ---
            if (containsAny(lowerMsg, "tìm", "search", "có bán", "có sản phẩm", "muốn mua", "cần mua")) {
                // Trích xuất từ khóa tìm kiếm (bỏ các từ phổ biến)
                String keyword = extractSearchKeyword(lowerMsg);
                if (keyword != null && !keyword.isEmpty()) {
                    List<Product> searchResults = productService.searchProducts(keyword);
                    if (!searchResults.isEmpty()) {
                        context.append("\n KẾT QUẢ TÌM KIẾM '").append(keyword).append("':\n");
                        int limit = Math.min(searchResults.size(), 5);
                        for (int i = 0; i < limit; i++) {
                            Product p = searchResults.get(i);
                            context.append(formatProductInfo(i + 1, p));
                        }
                        if (searchResults.size() > 5) {
                            context.append("... và ").append(searchResults.size() - 5).append(" sản phẩm khác.\n");
                        }
                    } else {
                        context.append("\n Không tìm thấy sản phẩm nào với từ khóa '").append(keyword).append("'.\n");
                    }
                }
            }

            // --- Danh mục sản phẩm ---
            if (containsAny(lowerMsg, "danh mục", "loại", "category", "phân loại", "nhóm sản phẩm", "có những gì")) {
                List<Category> categories = categoryService.getAll();
                if (!categories.isEmpty()) {
                    context.append("\n DANH MỤC SẢN PHẨM:\n");
                    for (Category c : categories) {
                        context.append("  • ").append(c.getName()).append("\n");
                    }
                }
            }

            // --- Giá / Khuyến mãi ---
            if (containsAny(lowerMsg, "giá", "giảm giá", "khuyến mãi", "sale", "rẻ", "đắt", "bao nhiêu", "promotion")) {
                List<Product> allProducts = productService.getAll();
                long discountedCount = allProducts.stream().filter(p -> p.getDiscount() > 0).count();
                if (discountedCount > 0) {
                    context.append("\n SẢN PHẨM ĐANG GIẢM GIÁ (").append(discountedCount).append(" sản phẩm):\n");
                    int count = 0;
                    for (Product p : allProducts) {
                        if (p.getDiscount() > 0 && count < 10) {
                            context.append(formatProductInfo(count + 1, p));
                            count++;
                        }
                    }
                    if (discountedCount > 10) {
                        context.append("... và ").append(discountedCount - 10).append(" sản phẩm giảm giá khác.\n");
                    }
                }
            }

            // --- Thống kê tổng quan (khi hỏi chung chung) ---
            if (containsAny(lowerMsg, "có bao nhiêu", "tổng", "thống kê", "tất cả", "toàn bộ")) {
                List<Product> all = productService.getAll();
                List<Category> cats = categoryService.getAll();
                context.append("\n THỐNG KÊ CỬA HÀNG:\n");
                context.append("  • Tổng sản phẩm đang bán: ").append(all.size()).append("\n");
                context.append("  • Số danh mục: ").append(cats.size()).append("\n");
                long discounted = all.stream().filter(p -> p.getDiscount() > 0).count();
                context.append("  • Sản phẩm đang giảm giá: ").append(discounted).append("\n");
            }

        } catch (Exception e) {
            log.warn("Lỗi khi truy vấn database cho chatbot: {}", e.getMessage());
        }

        return context.toString();
    }

    /**
     * Format thông tin sản phẩm cho context
     */
    private String formatProductInfo(int rank, Product p) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(rank).append(". ").append(p.getName());
        sb.append(" | Giá: ").append(nf.format(p.getPrice())).append("đ");
        if (p.getDiscount() > 0) {
            int salePrice = p.getPrice() - (p.getPrice() * p.getDiscount() / 100);
            sb.append(" → Giảm ").append(p.getDiscount()).append("% còn ").append(nf.format(salePrice)).append("đ");
        }
        sb.append(" | Lượt xem: ").append(p.getView());
        if (p.getStock() > 0) {
            sb.append(" | Còn: ").append(p.getStock());
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Kiểm tra xem tin nhắn có chứa bất kỳ từ khóa nào không
     */
    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }

    /**
     * Trích xuất từ khóa tìm kiếm từ tin nhắn
     */
    private String extractSearchKeyword(String message) {
        // Loại bỏ các từ phổ biến để lấy từ khóa chính
        String[] removeWords = {"tìm", "search", "có bán", "có sản phẩm", "muốn mua", "cần mua",
                "cho", "tôi", "mình", "em", "anh", "chị", "shop", "cửa hàng",
                "không", "nào", "gì", "sản phẩm", "hàng", "đồ", "cái", "chiếc",
                "giúp", "xem", "thử", "được", "nhé", "nha", "ạ", "à", "ơi"};
        String result = message;
        for (String word : removeWords) {
            result = result.replace(word, "");
        }
        return result.trim().replaceAll("\\s+", " ");
    }

    private String callGroqAPI(JsonArray chatHistory, String dbContext) throws IOException {
        // Build messages array
        JsonArray messages = new JsonArray();

        // System message with database context
        String fullSystemPrompt = SYSTEM_PROMPT;
        if (dbContext != null && !dbContext.isEmpty()) {
            fullSystemPrompt += "\n\n=== DỮ LIỆU THỰC TẾ TỪ HỆ THỐNG ===\n" + dbContext +
                    "\n=== HẾT DỮ LIỆU ===\n" +
                    "Hãy sử dụng dữ liệu trên để trả lời chính xác. Không bịa thông tin sản phẩm.";
        }

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", fullSystemPrompt);
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

        // Parse response
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
