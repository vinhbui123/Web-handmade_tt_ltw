package vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN;

public class Province {
    private int ProvinceID;
    private String ProvinceName;
    private String Code;

    public int getProvinceID() {
        return ProvinceID;
    }

    public void setProvinceID(int provinceID) {
        ProvinceID = provinceID;
    }

    public String getProvinceName() {
        return ProvinceName;
    }

    public void setProvinceName(String provinceName) {
        ProvinceName = provinceName;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public Province() {
    }
}