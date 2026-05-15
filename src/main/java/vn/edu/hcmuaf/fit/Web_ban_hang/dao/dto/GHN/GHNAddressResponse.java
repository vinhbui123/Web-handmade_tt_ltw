package vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.GHN;

import java.util.List;

public class GHNAddressResponse<T> {
    private int code;
    private String message;
    private List<T> data;

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

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public GHNAddressResponse() {
    }
}