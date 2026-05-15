package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN.District;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN.GHNAddressResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN.Ward;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.GHNService;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/wards"})
public class WardGHN extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String districtId = request.getParameter("district_id");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        // Validate
        if (districtId == null || districtId.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "Thiếu tham số district_id");
            out.print(gson.toJson(error));
            out.flush();
            out.close();
            return;
        }
        GHNService ghnService = new GHNService();
        String jsonWards = ghnService.getWards(Integer.parseInt(districtId));

        Type wardType = new TypeToken<GHNAddressResponse<Ward>>() {}.getType();
        GHNAddressResponse<Ward> wardResponse = gson.fromJson(jsonWards, wardType);

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Ward w : wardResponse.getData()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", w.getWardCode());
            entry.put("name", w.getWardName());
            filtered.add(entry);
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("code", wardResponse.getCode());
        responseMap.put("message", wardResponse.getMessage());
        responseMap.put("data", filtered);

        out.print(gson.toJson(responseMap));
        out.flush();
        out.close();
    }
}