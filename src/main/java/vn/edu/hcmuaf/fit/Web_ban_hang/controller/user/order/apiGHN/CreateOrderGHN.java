package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/create-order")
public class CreateOrderGHN extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jsonBody = "{"
                + "\"payment_type_id\": 2,"
                + "\"note\": \"Giao nhanh\","
                + "\"required_note\": \"KHONGCHOXEMHANG\","
                + "\"to_name\": \"Nguyễn Văn A\","
                + "\"to_phone\": \"0912345678\","
                + "\"to_address\": \"48 Bùi Thị Xuân\","
                + "\"to_ward_code\": \"20508\","
                + "\"to_district_id\": 1450,"
                + "\"weight\": 500,"
                + "\"length\": 20,"
                + "\"width\": 15,"
                + "\"height\": 10,"
                + "\"service_id\": 53320,"
                + "\"service_type_id\": 2,"
                + "\"items\": [{ \"name\": \"Áo thun\", \"quantity\": 2 }]"
                + "}";

//        String url = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create";
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Token", "YOUR_GHN_TOKEN");
//        headers.put("ShopId", "YOUR_SHOP_ID");
//
////        String result = GHNHelper.callApi(url, "POST", jsonBody, headers);
//        response.setContentType("application/json");
//        response.getWriter().write(result);
    }
}