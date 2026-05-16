package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.order.apiGHN;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.CategoryShippingDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.GHNService;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Address;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.ProductDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.utils.ReadJsonUtil;
import jakarta.servlet.http.HttpSession;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

            int totalWeight = 0;
            int orderLength = 0;
            int orderWidth = 0;
            int orderHeight = 0;
            JsonArray itemsArray = new JsonArray();

            // KHAI BÁO THÊM DAO ĐỂ LẤY KÍCH THƯỚC TỪ BẢNG DEFAULT
            ProductDao productDao = new ProductDao();
            CategoryShippingDao catShippingDao = new CategoryShippingDao();

            for (Map.Entry<Integer, Integer> entry : productQuantityMap.entrySet()) {
                Product p = productDao.getById(entry.getKey());
                if (p != null) {
                    int quantity = entry.getValue();

                    // 1. Lấy toàn bộ [cân nặng, dài, rộng, cao] mặc định của Danh mục
                    int[] catDefaults = catShippingDao.getShippingDefaults(p.getCatalog_id());
                    int categoryWeight = catDefaults[0];
                    int pLength = catDefaults[1];
                    int pWidth  = catDefaults[2];
                    int pHeight = catDefaults[3];

                    // 2. FALLBACK:
                    // Nếu Product có khối lượng > 0 thì xài của Product, nếu không thì xài của Category tránh trường hợp người dùng quên nhập khối lượng
                    int w = (p.getWeight() > 0) ? p.getWeight() : categoryWeight;
                    totalWeight += (w * quantity);

                    // 3. Phân loại lại: Dài nhất = Length, Ngắn nhất = Height
                    int[] dims = {pLength, pWidth, pHeight};
                    Arrays.sort(dims);
                    int itemHeight = dims[0]; // Min
                    int itemWidth = dims[1];  // Mid
                    int itemLength = dims[2]; // Max

                    // 4. Tính kích thước Gộp (Xếp các món hàng chồng lên nhau)
                    if (itemLength > orderLength) orderLength = itemLength;
                    if (itemWidth > orderWidth) orderWidth = itemWidth;
                    orderHeight += (itemHeight * quantity); // Xếp chồng lên -> Sum Height

                    // 5. Đẩy vào mảng items để GHN tính toán chi tiết
                    JsonObject itemObj = new JsonObject();
                    itemObj.addProperty("name", p.getName());
                    itemObj.addProperty("quantity", quantity);
                    itemObj.addProperty("weight", w);
                    itemObj.addProperty("length", itemLength);
                    itemObj.addProperty("width", itemWidth);
                    itemObj.addProperty("height", itemHeight);
                    itemsArray.add(itemObj);
                }
            }

            if (totalWeight < 10) totalWeight = 10;
            if (orderLength < 1) orderLength = 1;
            if (orderWidth < 1) orderWidth = 1;
            if (orderHeight < 1) orderHeight = 1;

            int dimensionalWeight = (orderLength * orderWidth * orderHeight) / 5;

            int finalChargeWeight = Math.max(totalWeight, dimensionalWeight);

            // Quyết định Gói dịch vụ: Lớn hơn 20kg (20.000g) là Hàng Nặng (5), ngược lại là Hàng Nhẹ (2)
            int serviceTypeId = (finalChargeWeight > 20000) ? 5 : 2;

            int totalOrderValue = jsonObject.has("totalOrderValue")
                    ? jsonObject.get("totalOrderValue").getAsInt()
                    : 0;

            JsonObject ghnRequest = new JsonObject();
            ghnRequest.addProperty("from_district_id", fromDistrictId);
            ghnRequest.addProperty("to_district_id", toDistrictId);
            ghnRequest.addProperty("to_ward_code", toWardCode);
            ghnRequest.addProperty("service_type_id", serviceTypeId);
            ghnRequest.addProperty("weight", totalWeight);
            ghnRequest.addProperty("length", orderLength);
            ghnRequest.addProperty("width", orderWidth);
            ghnRequest.addProperty("height", orderHeight);
            ghnRequest.addProperty("insurance_value", totalOrderValue);
            ghnRequest.addProperty("cod_failed_amount", 0);
            ghnRequest.add("items", itemsArray);

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