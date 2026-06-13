package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.address;

import java.io.BufferedReader;
import java.io.IOException;

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

@WebServlet("/default-address")
public class DefaultAddressController extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        Gson gson = new Gson();
        JsonObject resp = new JsonObject();

        try {
            HttpSession session = request.getSession(false);
            User user = (session != null) ? (User) session.getAttribute("user") : null;

            if (user == null) {
                resp.addProperty("status", false);
                resp.addProperty("message", "Vui lòng đăng nhập!");
            } else {
                BufferedReader reader = request.getReader();
                JsonObject data = gson.fromJson(reader, JsonObject.class);
                int addressId = data.get("addressId").getAsInt();

                AddressService service = new AddressService();
                Address address = new Address();
                address.setId(addressId);
                address.setUserId(user.getId());

                boolean success = service.setDefault(address);

                if (success) {
                    resp.addProperty("status", true);
                    Address defaultAddr = service.getAddressDefault(user.getId());
                    session.setAttribute("addressDefault", defaultAddr);
                    resp.add("addressDefault", gson.toJsonTree(defaultAddr));
                } else {
                    resp.addProperty("status", false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.addProperty("status", false);
        }
        response.getWriter().write(gson.toJson(resp));
    }
}