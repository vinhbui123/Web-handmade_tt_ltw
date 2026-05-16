<%-- Created by IntelliJ IDEA. User: Admin Date: 1/6/2025 Time: 1:29 PM To change this template use File | Settings |
    File Templates. --%>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <%@ page contentType="text/html;charset=UTF-8" language="java" %>
                <html>

                <head>
                    <title>Thanh Toán</title>
                    <link rel="stylesheet"
                        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
                    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/checkout.css">
                </head>

                <body>
                    <%@include file="header.jsp" %>
                        <header>
                            <div class="header-container">
                                <div class="logo">HAND MADE STUDIO</div>
                                <div class="title">| Thanh Toán</div>
                            </div>
                        </header>

                        <main>
                            <section class="shipping-address">
                                <div class="address-header">
                                    <span> Địa Chỉ Nhận Hàng</span>
                                    <%@include file="address-form.jsp" %>
                                </div>

                                <div class="address-details" id="current-address-display">
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.addressDefault}">
                                            <c:set var="address" value="${sessionScope.addressDefault}" />
                                            <span class="address-info-text" style="font-weight: bold">
                                                ${address.fullName}, SĐT: ${address.phone}
                                            </span><br>
                                            <span class="address-string">
                                                ${address.addressDetail}, ${address.ward}, ${address.district},
                                                ${address.province}
                                            </span>
                                            <input type="hidden" id="address-id-check" value="${address.id}" />
                                        </c:when>
                                        <c:otherwise>
                                            <em class="no-address">Vui lòng thêm địa chỉ nhận hàng để tiếp tục!</em>
                                            <input type="hidden" id="address-id-check" value="" />
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </section>

                            <section class="product-list">
                                <div class="menu-info">
                                    <div class="product-info-header">Sản phẩm</div>
                                    <div class="price-info-header">Đơn giá</div>
                                    <div class="quantity-info-header">Số lượng</div>
                                    <div class="total-info-header">Thành tiền</div>
                                </div>

                                <c:forEach var="product" items="${cart.list}">
                                    <c:if test="${product.selected}">
                                        <div class="product-item" data-product-id="${product.id}"
                                            data-price="${product.discountedPrice}" data-quantity="${product.quantity}">
                                            <div class="product-info">
                                                <div class="product-id" style="display: none;">${product.id}</div>
                                                <img src="${product.img}" alt="${product.name}" class="product-image">
                                                <div class="product-details">
                                                    <p class="product-name">${product.name}</p>
                                                </div>
                                            </div>
                                            <div class="price-info">
                                                <c:choose>
                                                    <c:when test="${product.discount > 0}">
                                                        <span class="current-price"
                                                            data-price="${product.discountedPrice}">
                                                            <f:formatNumber value="${product.discountedPrice}"
                                                                pattern="#,##0" /> đ
                                                        </span>
                                                        <br>
                                                        <span class="original-price" data-price="${product.price}"
                                                            style="text-decoration: line-through; color: #999; font-size: 0.8em;">
                                                            <f:formatNumber value="${product.price}" pattern="#,##0" />
                                                            đ
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="current-price" data-price="${product.price}">
                                                            <f:formatNumber value="${product.price}" pattern="#,##0" />
                                                            đ
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="quantity-info">${product.quantity}</div>
                                            <div class="total-info">
                                                <f:formatNumber value="${product.discountedPrice * product.quantity}"
                                                    pattern="#,##0" /> đ
                                            </div>
                                        </div>
                                    </c:if>
                                </c:forEach>

                                <div class="shipping-method-section">
                                    <div class="note-and-shipping">
                                        <div class="message-box">
                                            <label for="message"><strong>Lời nhắn:</strong></label><br>
                                            <textarea id="message" placeholder="Lưu ý cho Người bán..." rows="5"
                                                required style="width:100%; min-height: 70px;"></textarea>
                                        </div>
                                    </div>
                                </div>
                            </section>

                            <section class="payment-methods">
                                <h2>Phương thức thanh toán</h2>
                                <div class="payment-tabs" style="padding-top: 15px;">
                                    <button class="tab active" onclick="showPayment('cod')">Thanh toán khi nhận
                                        hàng</button>
                                    <button class="tab" onclick="showPayment('qr')">QR chuyển khoản</button>
                                </div>

                                <div class="payment-content" id="cod">
                                    <p>Bạn đã chọn phương thức <strong>Thanh toán khi nhận hàng</strong>. Vui lòng kiểm
                                        tra lại đơn hàng trước khi giao.</p>
                                    <p>Phí thu hộ: ₫0 VNĐ. Ưu đãi về phí vận chuyển (nếu có) áp dụng cả với phí thu hộ.
                                    </p>
                                </div>

                                <div class="payment-content hidden" id="qr">
                                    <p>Bạn đã chọn phương thức <strong>QR chuyển khoản</strong>.</p>
                                    <div class="qr-container">
                                        <img src="${pageContext.request.contextPath}/images/qrcode.png" alt="QR Code"
                                            class="qr-code">
                                        <p>Quét mã QR để thanh toán.</p>
                                    </div>
                                </div>

                                <div class="coupon-section"
                                    style="margin-top: 20px; padding: 15px; background: #fff; border-radius: 5px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
                                    <div style="display: flex; align-items: center; justify-content: space-between;">
                                        <div style="display: flex; align-items: center; gap: 10px;">
                                            <i class="fas fa-ticket-alt" style="color: #e74c3c; font-size: 1.2rem;"></i>
                                            <span style="font-weight: bold; font-size: 1.1rem;">Khuyến mãi</span>
                                        </div>
                                        <div style="display: flex; gap: 10px;">
                                            <input type="text" id="coupon-code-checkout"
                                                placeholder="Nhập mã giảm giá..."
                                                style="padding: 10px 15px; border: 1px solid #ccc; border-radius: 4px; outline: none; width: 250px;">
                                            <button type="button"
                                                onclick="applyCoupon(document.getElementById('coupon-code-checkout').value.trim())"
                                                style="padding: 10px 20px; background-color: #5a9153; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; transition: background 0.3s;">
                                                Áp Dụng
                                            </button>
                                        </div>
                                    </div>
                                </div>

                                <div class="order-summary" style="margin-top: 15px;">
                                    <div class="total-info total-order-value" id="total-order-value"
                                        style="display: flex;justify-content: flex-end;margin: 8px 0;font-size: 1em;">
                                        <span>Tổng cộng:</span>
                                        <f:formatNumber value="${sessionScope.cart.selectedTotalWithDiscount}"
                                            pattern="#,##0" />₫
                                    </div>
                                    <p>Phí vận chuyển: <span class="shipping-fee">0đ</span></p>
                                    <div id="applied-coupon-info" style="text-align: right; margin: 8px 0;">
                                        <c:if test="${not empty sessionScope.appliedCoupon}">
                                            <div
                                                style="display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
                                                <div style="text-align: right;">
                                                    <p style="margin: 0; color: #e74c3c;">Mã đã áp dụng:
                                                        <strong>${sessionScope.appliedCoupon.code}</strong>
                                                    </p>
                                                    <p style="margin: 0; color: #e74c3c;">Đã giảm: <strong>-
                                                            <f:formatNumber value="${requestScope.discountAmount}"
                                                                pattern="#,##0" /> đ
                                                        </strong></p>
                                                </div>
                                                <a href="javascript:void(0)" onclick="removeCoupon()"
                                                    style="color: #e74c3c; font-size: 1.2rem; margin-left: 5px;"
                                                    title="Hủy mã giảm giá"><i class="fas fa-times"></i></a>
                                            </div>
                                        </c:if>
                                    </div>
                                    <p><strong>Tổng thanh toán: <span>
                                                <f:formatNumber
                                                    value="${requestScope.finalTotal != null ? requestScope.finalTotal : sessionScope.cart.selectedTotalWithDiscount}"
                                                    pattern="#,##0" /> đ
                                            </span></strong></p>
                                    <input type="hidden" id="selectedCouponCode"
                                        value="${sessionScope.appliedCoupon != null ? sessionScope.appliedCoupon.code : ''}" />
                                </div>
                                <button class="submit-order" onclick="placeOrder()">Đặt hàng</button>
                            </section>
                        </main>

                        <%@include file="footer.jsp" %>
                            <script>
                                const contextPath = "${pageContext.request.contextPath}";
                                const userId = "${sessionScope.user.id}";
                            </script>
                            <script src="${pageContext.request.contextPath}/js/checkout.js"></script>
                </body>

                </html>