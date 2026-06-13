package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.GHNService;

@WebServlet("/available-services")
public class AvailableServicesGHN extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        String toDistrictId = request.getParameter("to_district_id");
        String toWardCode = request.getParameter("to_ward_code");

        if (toDistrictId == null || toDistrictId.isEmpty() || toWardCode == null || toWardCode.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", 400);
            error.put("message", "Thiếu tham số bắt buộc: to_district_id hoặc to_ward_code");
            out.print(gson.toJson(error));
            out.flush();
            out.close();
            return;
        }

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("from_district_id", 1463);
        jsonBody.addProperty("to_district_id", Integer.parseInt(toDistrictId));
        jsonBody.addProperty("to_ward_code", toWardCode);
        GHNService ghnService = new GHNService();
        String result = ghnService.getAvailableServices(jsonBody.toString());

        out.print(result);
        out.flush();
        out.close();
    }
}