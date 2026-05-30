<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="java.util.Map, java.util.List, java.util.LinkedHashMap, java.util.ArrayList" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head>
    <title>Quản Lý Đơn Hàng - Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <script src="${pageContext.request.contextPath}/js/admin.js"></script>
    <style>
        .modal-overlay {
            display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.6);
        }
        .modal-box {
            background-color: #fff; margin: 5% auto; padding: 20px 30px; border-radius: 8px; width: 90%; max-width: 500px; box-shadow: 0 5px 15px rgba(0,0,0,0.3); position: relative;
        }
        .close-btn { position: absolute; right: 20px; top: 15px; font-size: 24px; cursor: pointer; color: #555; }
        .close-btn:hover { color: red; }
    </style>
</head>
<body>
<%@include file="ad-menu.jsp" %>

<div class="main-content">
    <header>
        <h1>Quản Lý Đơn Hàng</h1>
    </header>

    <section class="order-management">
        <table class="transaction-table">
            <thead>
            <tr>
                <th>Mã Đơn</th>
                <th>Người Đặt</th>
                <th>Sản Phẩm (ID - Tên - SL)</th>
                <th>Chi Tiết Tiền</th>
                <th>Thanh Toán</th>
                <th>Trạng Thái</th>
                <th>Thời gian</th>
                <th>Hành Động</th>
            </tr>
            </thead>

            <%
                Map<Integer, List<Map<String, Object>>> groupedOrders = new LinkedHashMap<>();
                List<Map<String, Object>> rawOrders = (List<Map<String, Object>>) request.getAttribute("orderDetails");

                if(rawOrders != null) {
                    for (Map<String, Object> row : rawOrders) {
                        Integer orderId = (Integer) row.get("order_id");
                        groupedOrders.putIfAbsent(orderId, new ArrayList<>());
                        groupedOrders.get(orderId).add(row);
                    }
                }
                request.setAttribute("groupedOrders", groupedOrders);
            %>

            <tbody>
            <c:forEach var="entry" items="${groupedOrders}">
                <c:set var="products" value="${entry.value}"/>
                <c:set var="firstRow" value="${products[0]}"/>
                <tr>
                    <td>${firstRow.order_id}</td>
                    <td>${firstRow.username}</td>
                    <td>
                        <div style="line-height: 1.6;">
                            <c:forEach var="p" items="${products}">
                                ID: ${p.product_id} - ${p.product_name} - SL: ${p.quantity}<br/>
                            </c:forEach>
                        </div>
                    </td>

                    <td>
                        <c:set var="subTotal" value="0"/>
                        <c:set var="totalDiscount" value="0"/>
                        <c:set var="shippingFee" value="${firstRow.shipping_fee != null ? firstRow.shipping_fee : 0}"/>
                        <c:forEach var="p" items="${products}">
                            <c:set var="subTotal" value="${subTotal + p.total_money}"/>
                            <c:set var="discountForProduct" value="${p.discount_amount + (p.total_money * p.discount_percentage / 100)}"/>
                            <c:set var="totalDiscount" value="${totalDiscount + discountForProduct}"/>
                        </c:forEach>

                        <c:set var="finalTotal" value="${subTotal + shippingFee - totalDiscount}"/>

                        <div style="line-height: 1.6; font-size: 0.95em;">
                            <div style="color: #555;">Tổng gốc: <fmt:formatNumber value="${subTotal}" type="number"/> đ</div>
                            <div style="color: #3498db;">Phí ship: <fmt:formatNumber value="${shippingFee}" type="number"/> đ</div>
                            <c:if test="${totalDiscount > 0}">
                                <div style="color: #e74c3c;"> Giảm giá: -<fmt:formatNumber value="${totalDiscount}" type="number"/> đ</div>
                            </c:if>
                            <div style="font-weight: bold; color: #2ecc71; font-size: 1.15em; margin-top: 5px; border-top: 1px dashed #ccc; padding-top: 5px;">
                                Thực thu: <fmt:formatNumber value="${finalTotal}" type="number"/> đ
                            </div>
                        </div>
                    </td>

                    <td>${firstRow.payment_code}</td>

                    <td>
                        <c:choose>
                            <c:when test="${firstRow.status == 0}"><span class="status-pending">Đang chờ xác nhận</span></c:when>
                            <c:when test="${firstRow.status == 1}"><span class="status-confirmed">Đã xác nhận</span></c:when>
                            <c:when test="${firstRow.status == 2}"><span class="status-shipping">Đang giao hàng</span></c:when>
                            <c:when test="${firstRow.status == 3}"><span class="status-done">Đã hoàn thành</span></c:when>
                            <c:when test="${firstRow.status == 4}"><span class="status-cancelled" style="color: red; font-weight: bold;">Đã huỷ</span></c:when>
                            <c:when test="${firstRow.status == 7}"><span style="color: #7f8c8d; font-weight: bold; text-decoration: line-through;">Từ chối hoàn trả</span></c:when>
                            <%--  BỔ SUNG TRẠNG THÁI HOÀN TRẢ --%>
                            <c:when test="${firstRow.status == 5}"><span style="color: #ee4d2d; font-weight: bold;">Yêu cầu hoàn trả</span></c:when>
                            <c:when test="${firstRow.status == 6}"><span style="color: #8e44ad; font-weight: bold;">Đã hoàn tiền</span></c:when>
                            <c:otherwise><span class="status-unknown">Không rõ</span></c:otherwise>
                        </c:choose>
                    </td>

                    <td style="line-height: 1.5;">
                        <small> Ngày đặt: <fmt:formatDate value="${firstRow.create_at}" pattern="dd/MM/yyyy HH:mm:ss"/></small><br/>
                        <c:choose>
                            <c:when test="${firstRow.status == 1}"><small> Xác nhận: <fmt:formatDate value="${firstRow.updated_at}" pattern="dd/MM/yyyy HH:mm:ss"/></small></c:when>
                            <c:when test="${firstRow.status == 2}"><small> Giao hàng: <fmt:formatDate value="${firstRow.updated_at}" pattern="dd/MM/yyyy HH:mm:ss"/></small></c:when>
                            <c:when test="${firstRow.status == 3}"><small> Hoàn thành: <fmt:formatDate value="${firstRow.updated_at}" pattern="dd/MM/yyyy HH:mm:ss"/></small></c:when>
                            <c:when test="${firstRow.status == 4}"><small> Đã huỷ: <fmt:formatDate value="${firstRow.updated_at}" pattern="dd/MM/yyyy HH:mm:ss"/></small></c:when>

                            <%-- NGÀY YÊU CẦU HOÀN TRẢ --%>
                            <c:when test="${firstRow.status == 5}"><small> Ngày Y/c: <fmt:formatDate value="${firstRow.updated_at}" pattern="dd/MM/yyyy HH:mm:ss"/></small></c:when>
                            <c:when test="${firstRow.status == 6}"><small> Hoàn tiền: <fmt:formatDate value="${firstRow.updated_at}" pattern="dd/MM/yyyy HH:mm:ss"/></small></c:when>
                            <c:when test="${firstRow.status == 7}"><small> Bị từ chối: <fmt:formatDate value="${firstRow.updated_at}" pattern="dd/MM/yyyy HH:mm:ss"/></small></c:when>
                            <c:otherwise><small> Chưa có cập nhật</small></c:otherwise>
                        </c:choose>
                    </td>

                    <td>
                        <c:if test="${firstRow.status == 0 && sessionScope.user.role == 1}">
                            <form action="${pageContext.request.contextPath}/confirmOrder" method="post" style="margin-bottom: 5px;">
                                <input type="hidden" name="orderId" value="${firstRow.order_id}">
                                <button type="submit" class="btn-confirm" onclick="return confirm('Xác nhận xử lý đơn hàng này?')">Xác nhận</button>
                            </form>
                        </c:if>

                        <c:if test="${(firstRow.status == 0 || firstRow.status == 1) && sessionScope.user.role == 1}">
                            <form action="${pageContext.request.contextPath}/cancelOrder" method="post" style="margin-bottom: 5px;">
                                <input type="hidden" name="orderId" value="${firstRow.order_id}">
                                <button type="submit" class="btn-cancel" onclick="return confirm('Bạn chắc chắn muốn hủy đơn này?')">Hủy</button>
                            </form>
                        </c:if>

                            <%-- NÚT XỬ LÝ HOÀN TRẢ DÀNH CHO ADMIN --%>
                        <c:if test="${firstRow.status == 5 && sessionScope.user.role == 1}">
                            <button class="btn-return" onclick="openReviewModal('${firstRow.order_id}')"
                                    style="background-color: #f39c12; color: white; padding: 6px 12px; border: none; border-radius: 4px; cursor: pointer; font-weight: bold;">
                                Xử lý
                            </button>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </section>
</div>

<div id="adminReviewModal" class="modal-overlay">
    <div class="modal-box">
        <span class="close-btn" onclick="closeReviewModal()">&times;</span>
        <h2 style="margin-top:0; border-bottom: 2px solid #f3f3f3; padding-bottom: 10px;">Xử lý Hoàn trả - Đơn #<span id="displayOrderId" style="color:#ee4d2d;"></span></h2>

        <div style="margin-top: 15px; font-size: 15px;">
            <p><strong>Lý do:</strong> <span id="displayReason" style="color:#e74c3c;">Đang tải dữ liệu...</span></p>
            <p><strong>Mô tả chi tiết:</strong> <span id="displayDesc">Đang tải dữ liệu...</span></p>
            <p><strong>Hình ảnh minh chứng:</strong></p>
            <div style="text-align: center; background: #f9f9f9; padding: 10px; border-radius: 4px; border: 1px dashed #ccc;">
                <img id="displayImg" src="" alt="Ảnh minh chứng" style="max-width: 100%; max-height: 250px; border-radius: 4px; display: none;">
                <span id="imgLoading">Đang tải ảnh...</span>
            </div>
        </div>

        <form action="${pageContext.request.contextPath}/adminProcessReturn" method="post" style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 25px;">
            <input type="hidden" id="processOrderId" name="orderId">
            <button type="submit" name="action" value="reject" onclick="return confirm('Bạn từ chối hoàn trả đơn hàng này?')"
                    style="padding: 8px 15px; background: #fff; border: 1px solid #e74c3c; color: #e74c3c; border-radius: 4px; cursor: pointer; font-weight: bold;">
                Từ chối
            </button>
            <button type="submit" name="action" value="accept" onclick="return confirm('Xác nhận thu hồi hàng và Hoàn tiền?')"
                    style="padding: 8px 15px; background: #2ecc71; border: none; color: white; border-radius: 4px; cursor: pointer; font-weight: bold;">
                Chấp nhận Hoàn Tiền
            </button>
        </form>
    </div>
</div>

<script>
    function openReviewModal(orderId) {
        document.getElementById('adminReviewModal').style.display = 'block';
        document.getElementById('displayOrderId').innerText = orderId;
        document.getElementById('processOrderId').value = orderId;

        // Reset nội dung
        document.getElementById('displayReason').innerText = "Đang tải dữ liệu...";
        document.getElementById('displayDesc').innerText = "Đang tải dữ liệu...";
        document.getElementById('displayImg').style.display = 'none';
        document.getElementById('imgLoading').style.display = 'inline';

        // Gọi Fetch API để lấy dữ liệu từ bảng return_requests
        fetch('${pageContext.request.contextPath}/getReturnDetails?orderId=' + orderId)
            .then(res => res.json())
            .then(data => {
                if(data.success) {
                    document.getElementById('displayReason').innerText = data.reason;
                    document.getElementById('displayDesc').innerText = data.description || 'Không có mô tả thêm';
                    document.getElementById('displayImg').src = '${pageContext.request.contextPath}/' + data.proofImg;

                    document.getElementById('displayImg').style.display = 'inline-block';
                    document.getElementById('imgLoading').style.display = 'none';
                } else {
                    document.getElementById('displayReason').innerText = "Lỗi: Không tìm thấy dữ liệu hoàn trả!";
                    document.getElementById('displayDesc').innerText = "";
                    document.getElementById('imgLoading').innerText = "Không có ảnh";
                }
            })
            .catch(err => console.error("Lỗi fetch dữ liệu: ", err));
    }

    function closeReviewModal() {
        document.getElementById('adminReviewModal').style.display = 'none';
    }
</script>
</body>
</html>