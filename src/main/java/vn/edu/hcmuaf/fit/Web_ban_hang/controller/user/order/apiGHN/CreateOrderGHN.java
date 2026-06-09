package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    }
}