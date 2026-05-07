package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/shipfee")
public class ShippingFeeGHN {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jsonBody = "{"
                + "\"service_id\":53320,"
                + "\"insurance_value\":100000,"
                + "\"from_district_id\":3440,"
                + "\"to_district_id\":1450,"
                + "\"to_ward_code\":\"20508\","
                + "\"height\":10,"
                + "\"length\":20,"
                + "\"weight\":500,"
                + "\"width\":15"
                + "}";

        String url = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";

//        Map<String, String> headers = new HashMap<>();
//        headers.put("Token", "YOUR_GHN_TOKEN");
//        headers.put("ShopId", "YOUR_SHOP_ID");
//
//        String result = GHNHelper.callApi(url, "POST", jsonBody, headers);
//        response.setContentType("application/json");
//        response.getWriter().write(result);
    }
}