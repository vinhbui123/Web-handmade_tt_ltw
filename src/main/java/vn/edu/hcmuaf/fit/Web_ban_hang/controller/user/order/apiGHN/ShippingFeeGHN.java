package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.AddressDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN.ShopAddress;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.GHNService;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Address;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Dimension;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.ProductDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.ReadJsonUtil;
import jakarta.servlet.http.HttpSession;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static vn.edu.hcmuaf.fit.Web_ban_hang.services.GHNService.FROM_DISTRICT_ID;

@WebServlet(urlPatterns = {"/shipfee"})
public class ShippingFeeGHN extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            Gson gson = new Gson();
            GHNService ghnService = new GHNService();

            //  Lấy thông tin shop (id, district_id, ward_code)
            int fromDistrictId = ghnService.FROM_DISTRICT_ID;
//        String shopAddressJson = ghnService.getShopAddress();
//        ShopAddress shopAddress = gson.fromJson(shopAddressJson, ShopAddress.class);
//        int fromDistrictId = shopAddress.getDistrictId();

            //  Lấy địa chỉ nhận hàng của khách từ session
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            if (user == null) {
                out.print("{\"error\":\"Phiên đăng nhập hết hạn\"}");
                out.flush();
                out.close();
                return;
            }

            Address addressDefault = (Address) session.getAttribute("addressDefault");
            if (addressDefault == null) {
                out.print("{\"error\":\"Yêu cầu cập nhật địa chỉ nhận hàng\"}");
                out.flush();
                out.close();
                return;
            }
            String toProvince = addressDefault.getProvince();
            String toDistrict = addressDefault.getDistrict();
            String toWard = addressDefault.getWard();

            //  Lấy mã địa chỉ từ GHN
            String locationJson = ghnService.getLocationCodes(toProvince, toDistrict, toWard);
            JsonObject location = JsonParser.parseString(locationJson).getAsJsonObject();
            int toDistrictId = location.get("district_id").getAsInt();
            String toWardCode = location.get("ward_code").getAsString();

            // Lấy thông tin sản phẩm
            String jsonBody = ReadJsonUtil.read(request);
            JsonObject jsonObject = gson.fromJson(jsonBody, JsonObject.class);
            JsonArray products = jsonObject.getAsJsonArray("products");
            Map<Integer, Integer> productQuantityMap = new HashMap<>();
            for (JsonElement element : products) {
                JsonObject product = element.getAsJsonObject();
                int productId = product.get("id").getAsInt();
                int quantity = product.get("quantity").getAsInt();
                productQuantityMap.put(productId, quantity);
            }


            // Lấy thông tin kích thước và giá trị đơn hàng
            Dimension dimension = AddressDao.calculateTotalDimension(productQuantityMap);

            int totalOrderValue = jsonObject.has("totalOrderValue")
                    ? jsonObject.get("totalOrderValue").getAsInt()
                    : 0;


            // Lấy service_id động phù hợp
            int serviceId = ghnService.getDynamicServiceId(fromDistrictId, toDistrictId, toWardCode);
            if (serviceId == -1) {
                out.print("{\"error\":\"Không tìm thấy dịch vụ vận chuyển phù hợp\"}");
                out.flush();
                out.close();
                return;
            }

            //  Chuẩn bị JSON gửi lên API tính phí GHN
            JsonObject ghnRequest = new JsonObject();
            ghnRequest.addProperty("from_district_id", fromDistrictId);
            ghnRequest.addProperty("service_id", serviceId);
            ghnRequest.addProperty("to_district_id", toDistrictId);
            ghnRequest.addProperty("to_ward_code", toWardCode);
            ghnRequest.addProperty("weight", dimension.getTotalWeight());
            ghnRequest.addProperty("length", dimension.getTotalLength());
            ghnRequest.addProperty("width", dimension.getTotalWidth());
            ghnRequest.addProperty("height", dimension.getTotalHeight());
            ghnRequest.addProperty("insurance_value", totalOrderValue);
            ghnRequest.addProperty("cod_failed_amount", 0);

            String feeResponse = ghnService.calculateFee(ghnRequest.toString());
            JsonObject feeJson = gson.fromJson(feeResponse, JsonObject.class);
            int totalFee = 0;
            try {
                totalFee = feeJson.getAsJsonObject("data").get("total").getAsInt();
            } catch (Exception e) {
                out.print("{\"error\":\"Không lấy được phí giao hàng\"}");
                out.flush();
                out.close();
                return;
            }

            // Trả về phí giao hàng cho client
            out.print("{\"total\":" + totalFee + "}");
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("[SHIPFEE] Lỗi không xác định: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"error\":\"Có lỗi xảy ra khi tính phí vận chuyển\"}");
        }
    }
}