package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.address;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Address;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.AddressService;

@WebServlet("/address-form")
public class AddressController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();

        try {
            HttpSession session = request.getSession(false);
            User user = (session != null) ? (User) session.getAttribute("user") : null;

            if (user == null) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Bạn cần đăng nhập để thực hiện thao tác này!");
                out.print(gson.toJson(responseJson));
                return;
            }

            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);

            Address address = new Address();

            if (jsonRequest.has("id") && !jsonRequest.get("id").isJsonNull() && !jsonRequest.get("id").getAsString().isEmpty()) {
                address.setId(jsonRequest.get("id").getAsInt());
            }

            address.setUserId(user.getId());
            address.setFullName(jsonRequest.get("fullName").getAsString());
            address.setPhone(jsonRequest.get("phone").getAsString());
            address.setProvince(jsonRequest.get("province").getAsString());
            address.setDistrict(jsonRequest.get("district").getAsString());
            address.setWard(jsonRequest.get("ward").getAsString());
            address.setAddressDetail(jsonRequest.get("addressDetail").getAsString());
            address.setAddressType(jsonRequest.get("addressType").getAsString());

            boolean isDefault = jsonRequest.get("isDefault").getAsBoolean();
            address.setDefault(isDefault);

            AddressService addressService = new AddressService();
            boolean success = false;

            if (address.getId() == null || address.getId() == 0) {
                success = addressService.insertAddressAndSetDefault(address);
            } else {
                success = addressService.updateAddress(address);
                if (success && isDefault) {
                    addressService.setDefault(address);
                }
            }

            if (success) {
                responseJson.addProperty("status", true);
                responseJson.addProperty("message", "Lưu địa chỉ thành công!");
                Address defaultAddr = addressService.getAddressDefault(user.getId());
                if (defaultAddr != null) {
                    responseJson.add("addressDefault", gson.toJsonTree(defaultAddr));
                    session.setAttribute("addressDefault", defaultAddr);
                }
            } else {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Lưu địa chỉ thất bại tại Database!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            responseJson.addProperty("status", false);
            responseJson.addProperty("message", "Lỗi hệ thống: " + e.getMessage());
        }

        out.print(gson.toJson(responseJson));
        out.flush();
    }
}