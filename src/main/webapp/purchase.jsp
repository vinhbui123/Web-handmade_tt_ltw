<%-- Created by IntelliJ IDEA. User: NguyenDucHuy Date: 24/03/2025 Time: 11:10 PM To change this template use File |
    Settings | File Templates. --%>
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
            <%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
                <html>

                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Trạng thái đơn hàng</title>
                    <!-- Font Awesome -->
                    <link rel="stylesheet"
                        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
                    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/purchase.css">

                </head>

                <body>
                    <%@include file="header.jsp" %>

                        <div class="container">
                            <h2 class="purchase-title">Đơn Mua</h2>
                            <div class="tabs">
                                <button class="tab active" data-status="all" data-index="all">Tất cả</button>
                                <button class="tab" data-status="pending-payment-purchase" data-index="0">Chờ xác
                                    nhận</button>
                                <button class="tab" data-status="shipping" data-index="1">Đã xác nhận</button>
                                <button class="tab" data-status="pending-delivery" data-index="2">Đang giao
                                    hàng</button>
                                <button class="tab" data-status="completed" data-index="3">Đã hoàn thành</button>
                                <button class="tab" data-status="cancelled" data-index="4">Đã hủy</button>
                                <button class="tab" data-status="review" data-index="review">Đánh giá</button>
                            </div>

                            <div class="order-list">
                                <c:forEach var="order" items="${orders}">
                                    <!-- Gán data-index cho toàn bộ order-block -->
                                    <div class="order-block" data-index="${order.status}" data-order-id="${order.id}">
                                        <!-- Header -->
                                        <div class="order-header">
                                            <div class="header-left">
                                                <h3 class="order-id">Đơn hàng #${order.id}</h3>
                                                <p class="order-date">Ngày đặt: ${order.createdAt}</p>
                                            </div>
                                            <div class="header-right">
                                                <span class="status-label">Trạng thái:</span>
                                                <span class="status">${order.statusString}</span>
                                            </div>
                                            <i class="fa-solid fa-list-ul status-list-icon" onclick="openPurchaseDetailPopup()" style="cursor: pointer; margin-left: 5px;"></i>
                                        </div>

                                        <!-- Danh sách sản phẩm -->
                                        <div class="order-items">
                                            <c:forEach var="item" items="${order.purchaseItems}">
                                                <div class="purchase-item" style="display: none">${item.idProduct}</div>
                                                <div class="order-item">
                                                    <img src="${item.img}" alt="${item.name}" class="product-image" />
                                                    <div class="order-details">
                                                        <h4>${item.name}</h4>
                                                        <c:if test="${item.discount > 0}">
                                                            <p>Giảm giá: ${item.discount}%</p>
                                                        </c:if>
                                                        <p>Số lượng: ${item.quantity}</p>
                                                        <p>Thành tiền:
                                                            <f:formatNumber value="${item.total}" pattern="#,##0" /> đ
                                                        </p>
                                                    </div>
                                                </div>
                                            </c:forEach>
                                        </div>


                                        <!-- Tổng đơn -->
                                        <div class="order-summary">
                                            <hr class="summary-divider" />
                                            <!-- Nút thao tác (hiện/ẩn theo JS) -->
                                            <div class="order-actions">
                                                <button class="cancel hidden" onclick="cancelOrder(${order.id})">Hủy
                                                    đơn</button>
                                                <button class="pay hidden" onclick="payOrder(${order.id})">Thanh
                                                    toán</button>
                                                <button class="ratting hidden" onclick="rateOrder(${order.id})">Đánh
                                                    giá</button>
                                                <button class="reorder hidden" onclick="reorder(${order.id})">Mua
                                                    lại</button>
                                                <button class="connect hidden"
                                                    onclick="contactSupport(${order.id})">Liên hệ</button>
                                            </div>
                                            <c:set var="subtotal" value="0" />
                                            <c:set var="totalDiscount" value="0" />
                                            <c:forEach var="item" items="${order.purchaseItems}">
                                                <c:set var="subtotal" value="${subtotal + item.total}" />
                                                <c:set var="totalDiscount"
                                                    value="${totalDiscount + item.discountAmount}" />
                                            </c:forEach>

                                            <p>Phí vận chuyển:
                                                <f:formatNumber value="${order.shippingFee}" pattern="#,##0" /> đ
                                            </p>
                                            <c:if test="${totalDiscount > 0}">
                                                <p style="color: #e74c3c;">Giảm giá coupon: <strong>-
                                                        <f:formatNumber value="${totalDiscount}" type="number" /> đ
                                                    </strong></p>
                                            </c:if>
                                            <p><strong>Tổng cộng: <span class="price">
                                                        <f:formatNumber
                                                            value="${subtotal + order.shippingFee - totalDiscount}"
                                                            type="number" /> đ
                                                    </span></strong>
                                            </p>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>

                        <%@include file="purchase-detail.jsp" %>
                        <%@include file="review-modal.jsp" %>
                        <%@include file="footer.jsp" %>
                            <script>
                                const contextPath = "${pageContext.request.contextPath}";
                            </script>
                            <script src="${pageContext.request.contextPath}/js/purchase.js"></script>
                            <script src="${pageContext.request.contextPath}/js/review.js"></script>

                </body>

                </html>
