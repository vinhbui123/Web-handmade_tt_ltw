<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trạng thái đơn hàng</title>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/purchase.css">

</head>

<body>
<%@include file="header.jsp" %>

<div class="container">
    <h2 class="purchase-title">Đơn Mua</h2>
    <c:if test="${not empty sessionScope.message}">
        <div style="background-color: #d4edda; color: #155724; padding: 10px; margin-bottom: 15px; border-radius: 4px; border: 1px solid #c3e6cb;">
                ${sessionScope.message}
        </div>
        <c:remove var="message" scope="session"/>
    </c:if>

    <c:if test="${not empty sessionScope.error}">
        <div style="background-color: #f8d7da; color: #721c24; padding: 10px; margin-bottom: 15px; border-radius: 4px; border: 1px solid #f5c6cb;">
                ${sessionScope.error}
        </div>
        <c:remove var="error" scope="session"/>
    </c:if>
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
            <div class="order-block" data-index="${order.status}" data-order-id="${order.id}">
                <div class="order-header">
                    <div class="header-left">
                        <h3 class="order-id">Đơn hàng #${order.id}</h3>
                        <p class="order-date">Ngày đặt: ${order.createdAt}</p>
                    </div>
                    <div class="header-right">
                        <span class="status-label">Trạng thái:</span>
                        <span class="status">${order.statusString}</span>
                    </div>
                    <i class="fa-solid fa-list-ul status-list-icon" onclick="openPurchaseDetailPopup(${order.id})" style="cursor: pointer; margin-left: 5px;" title="Xem chi tiết đơn hàng"></i>
                </div>

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

                <div class="order-summary">
                    <hr class="summary-divider" />
                    <div class="order-actions">

                        <c:choose>
                            <c:when test="${order.status == 0}">
                                <button class="cancel" onclick="cancelOrder(${order.id})">Hủy đơn</button>
                            </c:when>

                            <c:when test="${order.status == 3}">
                                <button class="return-order-btn hidden" onclick="openReturnModal('${order.id}')">Hoàn đơn hàng</button>
                            </c:when>
                        </c:choose>

                        <button class="pay hidden" onclick="payOrder(${order.id})">Thanh toán</button>

                        <button class="ratting hidden" onclick="rateOrder(${order.id})">Đánh giá</button>

                        <button class="reorder hidden" onclick="reorder(${order.id})">Mua lại</button>
                        <button class="connect hidden" onclick="contactSupport(${order.id})">Liên hệ</button>

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
<div id="returnOrderModal" style="display: none; position: fixed; z-index: 9999; left: 0; top: 0; width: 100%; height: 100%; overflow: auto; background-color: rgba(0,0,0,0.5);">
    <div style="background-color: #fff; margin: 10vh auto; padding: 25px; border-radius: 8px; width: 90%; max-width: 500px; box-shadow: 0 4px 15px rgba(0,0,0,0.2); position: relative;">

        <span onclick="closeReturnModal()" style="position: absolute; right: 20px; top: 15px; font-size: 24px; cursor: pointer; color: #888;">&times;</span>
        <h2 style="margin-top: 0; margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px; font-size: 20px;">
            Yêu cầu Hoàn trả đơn hàng <span id="returnOrderIdDisplay" style="color: #ee4d2d;"></span>
        </h2>

        <form id="returnOrderForm" action="${pageContext.request.contextPath}/returnOrder" method="post" enctype="multipart/form-data">
            <input type="hidden" id="returnOrderIdInput" name="orderId">

            <div style="margin-bottom: 15px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px;">Lý do hoàn trả (Bắt buộc):</label>
                <select id="returnReason" name="reason" required style="width: 100%; padding: 10px; border-radius: 4px; border: 1px solid #ccc; outline: none;">
                    <option value="">-- Chọn lý do --</option>
                    <option value="Giao sai sản phẩm/sai màu sắc">Giao sai sản phẩm/sai màu sắc</option>
                    <option value="Sản phẩm bị lỗi, hỏng hóc do vận chuyển">Sản phẩm bị lỗi, hỏng hóc do vận chuyển</option>
                    <option value="Không đúng với mô tả">Không đúng với mô tả</option>
                    <option value="Lý do khác">Lý do khác...</option>
                </select>
            </div>

            <div style="margin-bottom: 15px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px;">Mô tả chi tiết (Tùy chọn):</label>
                <textarea id="returnDescription" name="description" rows="4" placeholder="Ví dụ: Gấu bông bị rách tai..."
                          style="width: 100%; padding: 10px; border-radius: 4px; border: 1px solid #ccc; resize: vertical; outline: none;"></textarea>
            </div>

            <div style="margin-bottom: 25px;">
                <label style="display: block; font-weight: bold; margin-bottom: 5px;">Hình ảnh minh chứng (Bắt buộc):</label>
                <input type="file" id="returnImage" name="proofImage" accept="image/*" required style="width: 100%; padding: 5px;">
                <div id="imagePreviewContainer" style="margin-top: 10px; display: none;">
                    <img id="imagePreview" src="" alt="Xem trước" style="max-width: 100px; border-radius: 4px; border: 1px solid #ddd; padding: 2px;">
                </div>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px; border-top: 1px solid #eee; padding-top: 15px;">
                <button type="button" onclick="closeReturnModal()" style="padding: 10px 20px; background: #f1f1f1; border: 1px solid #ddd; border-radius: 4px; cursor: pointer; font-weight: 500;">Hủy bỏ</button>
                <button type="submit" onclick="return validateReturnForm()" style="padding: 10px 20px; background: #ee4d2d; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold;">Gửi yêu cầu hoàn đơn</button>
            </div>
        </form>
    </div>
</div>
<script>
    function openReturnModal(orderId) {
        document.getElementById("returnOrderModal").style.display = "block";
        document.getElementById("returnOrderIdDisplay").innerText = "#" + orderId;
        document.getElementById("returnOrderIdInput").value = orderId;
    }

    function closeReturnModal() {
        document.getElementById("returnOrderModal").style.display = "none";
        document.getElementById("returnOrderForm").reset();
        document.getElementById("imagePreviewContainer").style.display = "none";
        document.getElementById("imagePreview").src = "";
    }

    document.getElementById("returnImage").addEventListener("change", function(event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                document.getElementById("imagePreview").src = e.target.result;
                document.getElementById("imagePreviewContainer").style.display = "block";
            }
            reader.readAsDataURL(file);
        } else {
            document.getElementById("imagePreviewContainer").style.display = "none";
        }
    });

    function validateReturnForm() {
        const reason = document.getElementById("returnReason").value;
        const file = document.getElementById("returnImage").files.length;

        if (reason === "") {
            alert("Vui lòng chọn Lý do hoàn trả!");
            return false;
        }
        if (file === 0) {
            alert("Vui lòng đính kèm Hình ảnh minh chứng!");
            return false;
        }
        return true;
    }
</script>
<script src="${pageContext.request.contextPath}/js/purchase.js"></script>
<script src="${pageContext.request.contextPath}/js/review.js"></script>

</body>

</html>
