package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/wards")
public class WardGHN extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String districtId = request.getParameter("district_id");

//        String url = "https://online-gateway.ghn.vn/shiip/public-api/master-data/ward";
//        String jsonBody = "{\"district_id\": " + districtId + "}";
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Token", "YOUR_GHN_TOKEN");
//
//        String result = GHNHelper.callApi(url, "POST", jsonBody, headers);
//        response.setContentType("application/json");
//        response.getWriter().write(result);
    }
}