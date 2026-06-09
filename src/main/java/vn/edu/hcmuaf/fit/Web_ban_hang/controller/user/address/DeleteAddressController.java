package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.address;

import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.AddressDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Address;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.ReadJsonUtil;

@WebServlet(name = "DeleteAddressController", value = "/delete-address")
public class DeleteAddressController extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("user") == null) {
                out.print("{\"status\": false, \"message\": \"Vui lòng đăng nhập để thực hiện.\"}");
                return;
            }
            User user = (User) session.getAttribute("user");

            String jsonInput = ReadJsonUtil.read(request);
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonInput, JsonObject.class);

            if (jsonObject == null || !jsonObject.has("addressId")) {
                out.print("{\"status\": false, \"message\": \"Dữ liệu không hợp lệ.\"}");
                return;
            }

            int addressId = jsonObject.get("addressId").getAsInt();

            AddressDao addressDao = new AddressDao();

            Address targetAddress = addressDao.getAddressById(addressId);
            if (targetAddress == null) {
                out.print("{\"status\": false, \"message\": \"Không tìm thấy địa chỉ.\"}");
                return;
            }

            if (targetAddress.getUserId() != user.getId()) {
                out.print("{\"status\": false, \"message\": \"Bạn không có quyền xóa địa chỉ này.\"}");
                return;
            }

            if (targetAddress.isDefault()) {
                out.print("{\"status\": false, \"message\": \"Không thể xóa địa chỉ mặc định. Vui lòng chọn địa chỉ khác làm mặc định trước.\"}");
                return;
            }

            boolean success = addressDao.deleteAddress(addressId);

            if (success) {
                out.print("{\"status\": true, \"message\": \"Đã xóa địa chỉ thành công.\"}");
            } else {
                out.print("{\"status\": false, \"message\": \"Có lỗi xảy ra khi xóa từ cơ sở dữ liệu.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\": false, \"message\": \"Lỗi hệ thống: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
            out.close();
        }
    }
}