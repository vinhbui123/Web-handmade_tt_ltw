package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.address;

import java.io.IOException;
import java.util.List;

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

@WebServlet("/get-address-list")
public class GetAddressListController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        Gson gson = new Gson();
        JsonObject responseJson = new JsonObject();

        try {
            HttpSession session = request.getSession(false);
            User user = (session != null) ? (User) session.getAttribute("user") : null;

            if (user == null) {
                responseJson.addProperty("status", false);
                responseJson.addProperty("message", "Chưa đăng nhập");
            } else {
                AddressService addressService = new AddressService();
                List<Address> list = addressService.getAddressByIdUser(user.getId());

                responseJson.addProperty("status", true);
                responseJson.add("addressList", gson.toJsonTree(list));
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.addProperty("status", false);
            responseJson.addProperty("message", "Lỗi server: " + e.getMessage());
        }

        response.getWriter().write(gson.toJson(responseJson));
    }
}