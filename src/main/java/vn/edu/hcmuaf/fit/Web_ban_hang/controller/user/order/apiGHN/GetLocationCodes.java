package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.GHNService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/get-location-codes")
public class GetLocationCodes extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Lấy thông tin địa chỉ từ request
        String province = request.getParameter("province");
        String district = request.getParameter("district");
        String ward = request.getParameter("ward");

        // Validate parameters
        if (province == null || district == null || ward == null) {
            out.print("{\"error\": \"Thiếu thông tin địa chỉ\"}");
            return;
        }

        // Gọi service để lấy mã địa chỉ
        GHNService ghnService = new GHNService();
        String result = ghnService.getLocationCodes(province, district, ward);

        // Trả về kết quả
        out.print(result);
    }
}