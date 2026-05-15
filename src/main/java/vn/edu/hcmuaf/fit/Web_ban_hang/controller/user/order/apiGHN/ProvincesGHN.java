package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN.GHNAddressResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN.Province;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.GHNService;


import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Gọi ra các tỉnh thành trong api
@WebServlet(urlPatterns = {"/provinces"})
public class ProvincesGHN extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        GHNService ghnService = new GHNService();

        PrintWriter out = response.getWriter();

        String jsonProvinces = ghnService.getProvinces();
        Gson gson = new Gson();
        Type provinceType = new TypeToken<GHNAddressResponse<Province>>() {
        }.getType();
        GHNAddressResponse<Province> provinceResponse = gson.fromJson(jsonProvinces, provinceType);

        // Chỉ lấy ProvinceID và ProvinceName
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Province p : provinceResponse.getData()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", p.getProvinceID());
            entry.put("name", p.getProvinceName());
            filtered.add(entry);
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("code", provinceResponse.getCode());
        responseMap.put("message", provinceResponse.getMessage());
        responseMap.put("data", filtered);

        out.print(gson.toJson(responseMap));
        out.flush();
        out.close();
    }
}