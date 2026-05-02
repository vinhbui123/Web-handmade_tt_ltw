package vn.edu.hcmuaf.fit.Web_ban_hang.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Coupon implements Serializable {
    private int id;
    private String code;
    private int type; // 0: Tiền mặt, 1: Phần trăm
    private int discountValue;
    private Integer maxDiscountValue; // Dùng Integer để có thể nhận giá trị null
    private int minOrderAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;

    // Constructor không tham số
    public Coupon() {
    }

    // Constructor đầy đủ
    public Coupon(int id, String code, int type, int discountValue, int minOrderAmount,
                  Integer maxDiscountValue, LocalDateTime startDate,  LocalDateTime endDate, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.type = type;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.maxDiscountValue = maxDiscountValue;
        this.startDate = startDate;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedDiscountValue() {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(this.discountValue) + "đ";
    }

    public String getFormattedExpiredDate() {
        if (this.endDate == null) return "";
        return this.endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getFormattedMinOrderAmount() {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(this.minOrderAmount) + "đ";
    }
}