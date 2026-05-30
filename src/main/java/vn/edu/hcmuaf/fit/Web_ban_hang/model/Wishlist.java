package vn.edu.hcmuaf.fit.Web_ban_hang.model;

import java.io.Serializable;
import java.util.Date;

public class Wishlist implements Serializable {
    private int id;
    private int userId;
    private int productId;
    private Date createdAt;

    public Wishlist() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
