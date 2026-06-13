package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.product;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.ProductService;

@WebServlet("/api/search-suggestions")
public class SearchSuggestionController extends HttpServlet {

    private static final int MAX_SUGGESTIONS = 8;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String keyword = request.getParameter("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            out.write("[]");
            return;
        }

        keyword = keyword.trim();

        ProductService productService = new ProductService();
        List<Product> products = productService.searchProducts(keyword);

        if (products.size() > MAX_SUGGESTIONS) {
            products = products.subList(0, MAX_SUGGESTIONS);
        }

        JsonArray jsonArray = new JsonArray();
        for (Product p : products) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", p.getId());
            obj.addProperty("name", p.getName());
            obj.addProperty("img", p.getImg());
            obj.addProperty("price", p.getPrice());
            obj.addProperty("discount", p.getDiscount());

            int finalPrice = p.getDiscount() > 0
                    ? p.getPrice() - (p.getPrice() * p.getDiscount() / 100)
                    : p.getPrice();
            obj.addProperty("finalPrice", finalPrice);

            jsonArray.add(obj);
        }

        out.write(new Gson().toJson(jsonArray));
    }
}
