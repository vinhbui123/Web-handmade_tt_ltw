<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/purchase-detail.css">
<script src="${pageContext.request.contextPath}/js/purchase-detail.js"></script>

<div id="purchaseModal" class="modal">
    <div class="modal-content">
        <div class="modal-header">
            <h2 style="margin: 0; font-size: 18px;">Chi Tiết Đơn Hàng #<span id="modal-order-id"></span></h2>
            <span class="close" onclick="closePurchaseDetailPopup()">&times;</span>
        </div>

        <div id="modal-loading" class="modal-loading-block">
            <i class="fas fa-spinner fa-spin fa-2x" style="color: #17a2b8;"></i>
            <p style="margin-top: 10px; color: #666; font-size: 14px;">Đang tải dữ liệu...</p>
        </div>

        <div id="modal-body" class="modal-body" style="display: none;">

            <div id="modal-timeline-container"></div>

            <div class="info-block">
                <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                    <span style="color: #555; font-size: 14px;"><i class="fa-solid fa-wallet" style="margin-right: 5px;"></i> Phương thức thanh toán:</span>
                    <strong id="modal-payment-method" style="color: #333; font-size: 14px;">Đang tải...</strong>
                </div>
                <div style="display: flex; justify-content: space-between;">
                    <span style="color: #555; font-size: 14px;"><i class="fa-solid fa-truck" style="margin-right: 5px;"></i> Tình trạng đơn:</span>
                    <strong id="modal-order-status" style="color: #17a2b8; font-size: 14px;">Đang tải...</strong>
                </div>
            </div>

            <div id="modal-product-list">
            </div>
        </div>

        <div id="modal-footer" class="modal-footer" style="display: none;">
            <div style="display: flex; justify-content: space-between;">
                <span>Tổng tiền hàng:</span>
                <strong id="modal-subtotal">0đ</strong>
            </div>
            <div style="display: flex; justify-content: space-between;">
                <span>Phí vận chuyển:</span>
                <strong id="modal-shipping">0đ</strong>
            </div>
            <div style="display: flex; justify-content: space-between; color: #e74c3c;">
                <span>Giảm giá coupon:</span>
                <strong id="modal-discount">-0đ</strong>
            </div>
            <div class="total-row">
                <strong>Tổng thanh toán:</strong>
                <strong id="modal-total" style="color: #ee4d2d;">0đ</strong>
            </div>
        </div>
    </div>
</div>