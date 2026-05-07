package vn.edu.hcmuaf.fit.Web_ban_hang.services;

import vn.edu.hcmuaf.fit.Web_ban_hang.utils.GHNHelper;

public class GHNService {
    private static final String TOKEN = " ";
    private static final int SHOP_ID =  0 ;

    public String createOrder(String jsonBody) {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create";
        try {
            return GHNHelper.postJson(url, TOKEN, SHOP_ID, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to create order\"}";
        }
    }

    public String getProvices() {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/master-data/province";
        try {
            return GHNHelper.getJson(url, TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to get districts\"}";
        }
    }


    public String getDistricts(int provinceId) {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/master-data/district?province_id=" + provinceId;
        try {
            return GHNHelper.getJson(url, TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to get districts\"}";
        }
    }

    public String getWards(int districtId) {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/master-data/ward?district_id=" + districtId;
        try {
            return GHNHelper.getJson(url, TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to get wards\"}";
        }
    }

    public String calculateFee(String jsonBody) {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
        try {
            return GHNHelper.postJson(url, TOKEN, SHOP_ID, jsonBody);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to calculate fee\"}";
        }
    }

    public String cancelOrder(String orderCode) {
        String url = "https://online-gateway.ghn.vn/shiip/public-api/v2/switch-status/cancel";
        String json = "{\"order_codes\": [\"" + orderCode + "\"]}";
        try {
            return GHNHelper.postJson(url, TOKEN, SHOP_ID, json);
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to cancel order\"}";
        }
    }

}