<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/review-modal.css">

<!-- Review Modal Popup -->
<div id="reviewModal" class="review-modal">
    <div class="review-modal-content">
        <span class="review-close" onclick="closeReviewModal()">&times;</span>
        <h3 class="review-modal-title">
            <i class="fas fa-star"></i> Đánh giá sản phẩm
        </h3>

        <div id="review-items-container">
            <!-- Sản phẩm sẽ được render bằng JS -->
        </div>

        <div class="review-form-group">
            <label>Chọn số sao:</label>
            <div class="star-rating" id="star-rating">
                <i class="far fa-star" data-value="1"></i>
                <i class="far fa-star" data-value="2"></i>
                <i class="far fa-star" data-value="3"></i>
                <i class="far fa-star" data-value="4"></i>
                <i class="far fa-star" data-value="5"></i>
            </div>
            <input type="hidden" id="review-rating-value" value="0">
        </div>

        <div class="review-form-group">
            <label for="review-content">Nhận xét của bạn:</label>
            <textarea id="review-content" placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..." rows="4"></textarea>
        </div>

        <input type="hidden" id="review-product-id" value="">
        <input type="hidden" id="review-order-id" value="">

        <div class="review-actions">
            <button class="review-cancel-btn" onclick="closeReviewModal()">Hủy</button>
            <button class="review-submit-btn" onclick="submitReview()">
                <i class="fas fa-paper-plane"></i> Gửi đánh giá
            </button>
        </div>
    </div>
</div>
