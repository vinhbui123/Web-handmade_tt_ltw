package vn.edu.hcmuaf.fit.Web_ban_hang.model;

import java.io.Serializable;

public class Order implements Serializable {
    private int id;
    private int status;
    private int userId;
    private int shippingFee;
    private int paymentTypeId;
    private String createdAt;
    private String updatedAt;

    public Order() {
    }

    public Order(int status, int userId, int shippingFee, int paymentTypeId) {
        this.status = status;
        this.userId = userId;
        this.shippingFee = shippingFee;
        this.paymentTypeId = paymentTypeId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getShippingFee() { return shippingFee; }
    public void setShippingFee(int shippingFee) { this.shippingFee = shippingFee; }

    public int getPaymentTypeId() { return paymentTypeId; }
    public void setPaymentTypeId(int paymentTypeId) { this.paymentTypeId = paymentTypeId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", status=" + status +
                ", userId=" + userId +
                ", shippingFee=" + shippingFee +
                ", paymentTypeId=" + paymentTypeId +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

}

