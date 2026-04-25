package vn.edu.hcmuaf.fit.Web_ban_hang.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Coupon implements Serializable {
    private int id;
    private String code;
    private String description;
    private int discountValue;
    private int discountPercent;
    private int minOrderAmount;
    private Integer maxDiscountValue;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    // Constructor không tham số
    public Coupon() {
    }

    // Constructor đầy đủ
    public Coupon(int id, String code, int discountValue, int discountPercent, int minOrderAmount,
                  Integer maxDiscountValue, LocalDateTime expiredAt, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.discountValue = discountValue;
        this.discountPercent = discountPercent;
        this.minOrderAmount = minOrderAmount;
        this.maxDiscountValue = maxDiscountValue;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(int discountValue) {
        this.discountValue = discountValue;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public int getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(int minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public Integer getMaxDiscountValue() {
        return maxDiscountValue;
    }

    public void setMaxDiscountValue(Integer maxDiscountValue) {
        this.maxDiscountValue = maxDiscountValue;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormattedDiscountValue() {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(this.discountValue) + "đ";
    }

    public String getFormattedExpiredDate() {
        if (this.expiredAt == null) return "";
        return this.expiredAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFormattedMinOrderAmount() {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(this.minOrderAmount) + "đ";
    }
}