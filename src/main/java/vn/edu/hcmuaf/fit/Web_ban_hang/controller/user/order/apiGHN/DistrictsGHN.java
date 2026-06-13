package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN.District;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN.GHNAddressResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.GHNService;

@WebServlet(urlPatterns = {"/districts"})
public class DistrictsGHN extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String provinceId = request.getParameter("province_id");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        if (provinceId == null || provinceId.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "Thiếu tham số province_id");
            out.print(gson.toJson(error));
            out.flush();
            out.close();
            return;
        }
        GHNService ghnService = new GHNService();
        String jsonDistricts = ghnService.getDistricts(Integer.parseInt(provinceId));

        Type districtType = new TypeToken<GHNAddressResponse<District>>() {
        }.getType();
        GHNAddressResponse<District> districtResponse = gson.fromJson(jsonDistricts, districtType);

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (District d : districtResponse.getData()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", d.getDistrictID());
            entry.put("name", d.getDistrictName());
            filtered.add(entry);
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("code", districtResponse.getCode());
        responseMap.put("message", districtResponse.getMessage());
        responseMap.put("data", filtered);

        out.print(gson.toJson(responseMap));
        out.flush();
        out.close();
    }
}