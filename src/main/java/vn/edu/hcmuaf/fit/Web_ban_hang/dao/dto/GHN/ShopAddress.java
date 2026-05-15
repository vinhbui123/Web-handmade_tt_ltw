package vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN;

import com.google.gson.annotations.SerializedName;

public class ShopAddress {

    @SerializedName("shop_id")
    private long shopId;
    private String name;
    private String address;

    @SerializedName("district_id")
    private int districtId;

    @SerializedName("ward_code")
    private String wardCode;

    // Constructors
    public ShopAddress() {}

    // Getters and Setters
    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDistrictId() {
        return districtId;
    }

    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }

    public String getWardCode() {
        return wardCode;
    }

    public void setWardCode(String wardCode) {
        this.wardCode = wardCode;
    }

    @Override
    public String toString() {
        return "ShopAddress{" +
                "shopId=" + shopId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", districtId=" + districtId +
                ", wardCode='" + wardCode + '\'' +
                '}';
    }
}