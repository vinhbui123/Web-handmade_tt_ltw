package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.GHNService;

import java.io.IOException;

@WebServlet("/districts")
public class DistrictGHN extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        GHNService ghnService = new GHNService();

//        PrintWriter out = response.getWriter();
//
////        String jsonProvinces = ghnService.getDistricts(request.getAttribute("id"));
//        Gson gson = new Gson();
//        GHNProvinceResponse provinceResponse = gson.fromJson(json, GHNProvinceResponse.class);
//
//        // Chỉ lấy ProvinceID và ProvinceName
//        List<Map<String, Object>> filtered = new ArrayList<>();
//        for (Province p : provinceResponse.getData()) {
//            Map<String, Object> entry = new HashMap<>();
//            entry.put("id", p.getProvinceID());
//            entry.put("name", p.getProvinceName());
//            filtered.add(entry);
//        }
//
//        Map<String, Object> responseMap = new HashMap<>();
//        responseMap.put("code", provinceResponse.getCode());
//        responseMap.put("message", provinceResponse.getMessage());
//        responseMap.put("data", filtered);
//
//        out.print(gson.toJson(responseMap));
//        out.flush();
//        out.close();
    }
}