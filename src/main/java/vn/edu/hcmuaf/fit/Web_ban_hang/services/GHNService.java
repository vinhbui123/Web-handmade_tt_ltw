package vn.edu.hcmuaf.fit.Web_ban_hang.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import vn.edu.hcmuaf.fit.Web_ban_hang.utils.GHNHelper;

public class GHNService {
    private static final String TOKEN = "53953c8b-4c11-11f1-a973-aee5264794df";
    private static final int SHOP_ID = 200253;
    public static final int FROM_DISTRICT_ID = 1463;

    public String createOrder(String jsonBody) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create";
        try {
            return GHNHelper.postJson(url, TOKEN, SHOP_ID, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to create order\"}";
        }
    }

    public String getProvinces() {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/province";

        try {
            return GHNHelper.getJson(url, TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to get districts\"}";
        }
    }


    public String getDistricts(int provinceId) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district?province_id=" + provinceId;
        try {
            return GHNHelper.getJson(url, TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to get districts\"}";
        }
    }

    public String getWards(int districtId) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/ward?district_id=" + districtId;
        try {
            return GHNHelper.getJson(url, TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to get wards\"}";
        }
    }

    public String calculateFee(String jsonBody) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
        try {
            return GHNHelper.postJson(url, TOKEN, SHOP_ID, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to calculate fee\"}";
        }
    }

    public String cancelOrder(String orderCode) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/switch-status/cancel";
        String json = "{\"order_codes\": [\"" + orderCode + "\"]}";
        try {
            return GHNHelper.postJson(url, TOKEN, SHOP_ID, json);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to cancel order\"}";
        }
    }

    public String getShopAddress() {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shop/all";
        try {
            String response = GHNHelper.postJson(url, TOKEN, SHOP_ID, new JsonObject().toString());

            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            JsonArray shops = jsonResponse.getAsJsonObject("data").getAsJsonArray("shops");

            for (JsonElement element : shops) {
                JsonObject shop = element.getAsJsonObject();
                if (shop.get("_id").getAsLong() == SHOP_ID) {
                    JsonObject result = new JsonObject();
                    result.addProperty("shop_id", shop.get("_id").getAsLong());
                    result.addProperty("name", shop.get("name").getAsString());
                    result.addProperty("address", shop.get("address").getAsString());
                    result.addProperty("district_id", shop.get("district_id").getAsInt());
                    result.addProperty("ward_code", shop.get("ward_code").getAsString());
                    return result.toString();
                }
            }

            return "{\"error\": \"Shop not found\"}";

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to get shop address\"}";
        }
    }

    public String getAvailableServices(String jsonBody) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/available-services";
        try {
            return GHNHelper.postJson(url, TOKEN, SHOP_ID, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to get available services\"}";
        }
    }

    public String getLocationCodes(String province, String district, String ward) {
        try {
            String provincesJson = getProvinces();
            System.out.println("[GHN] Provinces response: " + provincesJson);
            JsonObject provincesResponse = JsonParser.parseString(provincesJson).getAsJsonObject();
            JsonArray provinces = provincesResponse.getAsJsonArray("data");
            int provinceId = -1;
            for (JsonElement element : provinces) {
                JsonObject p = element.getAsJsonObject();
                if (p.get("ProvinceName").getAsString().equalsIgnoreCase(province)) {
                    provinceId = p.get("ProvinceID").getAsInt();
                    break;
                }
            }

            if (provinceId == -1) {
                System.out.println("[GHN] Không tìm thấy tỉnh/thành phố: " + province);
                return "{\"error\": \"Không tìm thấy tỉnh/thành phố\"}";
            }
            System.out.println("[GHN] Tìm thấy provinceId: " + provinceId);

            String districtsJson = getDistricts(provinceId);
            System.out.println("[GHN] Districts response: " + districtsJson);
            JsonObject districtsResponse = JsonParser.parseString(districtsJson).getAsJsonObject();
            JsonArray districts = districtsResponse.getAsJsonArray("data");
            int districtId = -1;
            for (JsonElement element : districts) {
                JsonObject d = element.getAsJsonObject();
                if (d.get("DistrictName").getAsString().equalsIgnoreCase(district)) {
                    districtId = d.get("DistrictID").getAsInt();
                    break;
                }
            }

            if (districtId == -1) {
                System.out.println("[GHN] Không tìm thấy quận/huyện: " + district);
                return "{\"error\": \"Không tìm thấy quận/huyện\"}";
            }
            System.out.println("[GHN] Tìm thấy districtId: " + districtId);

            String wardsJson = getWards(districtId);
            System.out.println("[GHN] Wards response: " + wardsJson);
            JsonObject wardsResponse = JsonParser.parseString(wardsJson).getAsJsonObject();
            JsonArray wards = wardsResponse.getAsJsonArray("data");

            String wardCode = null;
            for (JsonElement element : wards) {
                JsonObject w = element.getAsJsonObject();
                if (w.get("WardName").getAsString().equalsIgnoreCase(ward)) {
                    wardCode = w.get("WardCode").getAsString();
                    break;
                }
            }

            if (wardCode == null) {
                System.out.println("[GHN] Không tìm thấy phường/xã: " + ward);
                return "{\"error\": \"Không tìm thấy phường/xã\"}";
            }
            System.out.println("[GHN] Tìm thấy wardCode: " + wardCode);

            // Trả về kết quả
            JsonObject result = new JsonObject();
            result.addProperty("province_id", provinceId);
            result.addProperty("district_id", districtId);
            result.addProperty("ward_code", wardCode);

            String finalResult = result.toString();
            System.out.println("[GHN] Final result: " + finalResult);
            return finalResult;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[GHN] Error: " + e.getMessage());
            return "{\"error\": \"Lỗi khi lấy mã địa chỉ: " + e.getMessage() + "\"}";
        }
    }

    public int getDynamicServiceId(int fromDistrictId, int toDistrictId, String toWardCode) {
        try {
            com.google.gson.JsonObject req = new com.google.gson.JsonObject();
            req.addProperty("shop_id", SHOP_ID);
            req.addProperty("from_district", fromDistrictId);
            req.addProperty("to_district", toDistrictId);
            req.addProperty("to_ward", toWardCode);
            String res = getAvailableServices(req.toString());
            com.google.gson.JsonObject resObj = com.google.gson.JsonParser.parseString(res).getAsJsonObject();
            if (resObj.has("data")) {
                com.google.gson.JsonArray data = resObj.getAsJsonArray("data");
                if (data.size() > 0) {
                    return data.get(0).getAsJsonObject().get("service_id").getAsInt();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    public String createShippingOrder(String toName, String toPhone, String addressDetail, String wardCode, int districtId, int serviceId, int codAmount, com.google.gson.JsonArray items) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create";

        com.google.gson.JsonObject jsonBody = new com.google.gson.JsonObject();

        jsonBody.addProperty("payment_type_id", 2);
        jsonBody.addProperty("note", "Hàng dễ vỡ, xin nhẹ tay");

        jsonBody.addProperty("required_note", "CHOXEMHANGKHONGTHU");

        jsonBody.addProperty("to_name", toName);
        jsonBody.addProperty("to_phone", toPhone);
        jsonBody.addProperty("to_address", addressDetail);
        jsonBody.addProperty("to_ward_code", wardCode);
        jsonBody.addProperty("to_district_id", districtId);

        jsonBody.addProperty("cod_amount", codAmount);

        jsonBody.addProperty("weight", 500);
        jsonBody.addProperty("length", 20);
        jsonBody.addProperty("width", 15);
        jsonBody.addProperty("height", 10);

        jsonBody.addProperty("service_id", serviceId);
        jsonBody.addProperty("service_type_id", 2);

        jsonBody.add("items", items);

        try {
            String result = GHNHelper.postJson(url, TOKEN, SHOP_ID, jsonBody.toString());
            System.out.println("[GHN_CREATE_ORDER] " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo đơn GHN: " + e.getMessage());
            return null;
        }
    }

}