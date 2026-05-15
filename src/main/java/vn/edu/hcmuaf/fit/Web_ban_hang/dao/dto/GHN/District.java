package vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN;

public class District {
    private int DistrictID;
    private String DistrictName;
    private String Code;

    public int getDistrictID() {
        return DistrictID;
    }

    public void setDistrictID(int districtID) {
        DistrictID = districtID;
    }

    public String getDistrictName() {
        return DistrictName;
    }

    public void setDistrictName(String districtName) {
        DistrictName = districtName;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }

    public District() {
    }
}