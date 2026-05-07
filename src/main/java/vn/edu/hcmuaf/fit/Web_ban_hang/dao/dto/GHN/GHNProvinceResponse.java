package vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN;

import java.util.List;

public class GHNProvinceResponse {
    private int code;
    private String message;
    private List<Province> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Province> getData() {
        return data;
    }

    public void setData(List<Province> data) {
        this.data = data;
    }

    public GHNProvinceResponse() {
    }
}