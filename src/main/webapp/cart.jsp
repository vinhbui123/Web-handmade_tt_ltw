<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
        <%@ page contentType="text/html;charset=UTF-8" language="java" %>
            <html>

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Giỏ Hàng</title>
                <link href="https://fonts.googleapis.com/css2?family=Roboto&display=swap&subset=vietnamese"
                    rel="stylesheet">
                <link rel="stylesheet"
                    href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/cart.css">
            </head>

            <body data-context-path="${pageContext.request.contextPath}">
                <%@include file="header.jsp" %>

                    <!-- Tiêu đề chính -->
                    <div class="cart-header">
                        <div class="logo">HAND MADE STUDIO</div>
                        <div class="title">| Giỏ Hàng</div>
                    </div>

                    <!-- Giỏ hàng -->
                    <div class="bounder-box" style="min-height: 50%">
                        <div class="cart-container">
                            <!-- Tiêu đề cột -->
                            <div class="menu-info">
                                <div class="checkbox-info"></div>
                                <div class="product-info">Sản Phẩm</div>
                                <div class="price-info">Đơn Giá</div>
                                <div class="quantity-info">Số Lượng</div>
                                <div class="total-info">Số Tiền</div>
                                <div class="action-info">Thao Tác</div>
                            </div>

                            <!-- Thông báo lỗi nếu có -->
                            <c:if test="${param.error == 'outofstock'}">
                                <div class="alert alert-warning"
                                    style="color: red; padding: 10px; border: 1px solid red; background-color: #f8d7da; margin-top: 20px;">
                                    Không đủ hàng trong kho!
                                </div>
                            </c:if>

                            <!-- Kiểm tra xem giỏ hàng có trống không -->
                            <c:choose>
                                <c:when
                                    test="${isCartEmpty or empty sessionScope.cart or empty sessionScope.cart.list}">
                                    <div class="empty-cart" style="text-align: center; padding: 50px;">
                                        <p><i class="fas fa-shopping-cart" style="font-size: 3em; color: #ccc;"></i></p>
                                        <p>Giỏ hàng của bạn đang trống.</p>
                                        <a href="${pageContext.request.contextPath}/list-product"
                                            class="continue-shopping-btn"
                                            style="display: inline-block; padding: 10px 20px; background-color: #5a9153; color: white; text-decoration: none; border-radius: 5px; margin-top: 10px;">
                                            <i class="fas fa-arrow-left"></i> Tiếp tục mua sắm
                                        </a>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${sessionScope.cart.list}" var="cp">
                                        <!-- Sản phẩm trong giỏ -->
                                        <div class="cart-item" data-product-id="${cp.id}">
                                            <div class="checkbox-info">
                                                <input type="checkbox" class="product-checkbox" ${cp.selected
                                                    ? 'checked' : '' }
                                                    onchange="updateSelection(${cp.id}, this.checked)">
                                            </div>

                                            <div class="product-info">
                                                <img src="${cp.img}" alt="${cp.name}" class="product-img">
                                                <div class="product-detail">
                                                    <span class="product-name">${cp.name}</span>
                                                    <span class="stock-quantity">Còn lại: ${cp.stock}</span>
                                                </div>
                                            </div>

                                            <div class="price-info">
                                                <h4>
                                                    <c:choose>
                                                        <c:when test="${cp.discount > 0}">
                                                            <f:formatNumber
                                                                value="${cp.price - (cp.price * cp.discount / 100)}"
                                                                pattern="#,##0đ" />
                                                            <span class="gia-cu"
                                                                style="text-decoration: line-through; color: #999; font-size: 0.8em;">
                                                                <f:formatNumber value="${cp.price}" pattern="#,##0đ" />
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <f:formatNumber value="${cp.price}" pattern="#,##0đ" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                </h4>
                                            </div>

                                            <div class="quantity-info">
                                                <form action="${pageContext.request.contextPath}/cart" method="post"
                                                    class="quantity-form" style="display: flex; align-items: center;">
                                                    <input type="hidden" name="action" value="update">
                                                    <input type="hidden" name="id" value="${cp.id}">
                                                    <button type="submit" name="quantity" value="${cp.quantity - 1}"
                                                        class="quantity-btn" ${cp.quantity <=1 ? 'disabled' : '' }>-
                                                    </button>
                                                    <input type="text" value="${cp.quantity}" readonly
                                                        class="quantity-input" style="width: 40px; text-align: center;">
                                                    <button type="submit" name="quantity" value="${cp.quantity + 1}"
                                                        class="quantity-btn" ${cp.quantity>= cp.stock ? 'disabled' :
                                                        ''}>+
                                                    </button>
                                                </form>
                                            </div>

                                            <div class="total-info">
                                                <h4>
                                                    <c:choose>
                                                        <c:when test="${cp.discount > 0}">
                                                            <f:formatNumber
                                                                value="${(cp.price - (cp.price * cp.discount / 100)) * cp.quantity}"
                                                                pattern="#,##0đ" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <f:formatNumber value="${cp.price * cp.quantity}"
                                                                pattern="#,##0đ" />
                                                        </c:otherwise>
                                                    </c:choose>
                                                </h4>
                                            </div>

                                            <div class="action-info">
                                                <a href="${pageContext.request.contextPath}/cart?action=remove&id=${cp.id}"
                                                    class="remove-btn"
                                                    onclick="return confirm('Bạn có chắc muốn xóa sản phẩm này?')">
                                                    Xoá
                                                </a>
                                            </div>
                                        </div>
                                    </c:forEach>

                                    <!-- Phần Nhập Mã Giảm Giá -->
                                    <div class="coupon-section"
                                        style="padding: 15px; background: #fff; border-radius: 5px; margin-bottom: 15px; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 3px rgba(0,0,0,0.1);">
                                        <div style="display: flex; align-items: center; gap: 10px;">
                                            <i class="fas fa-ticket-alt" style="color: #e74c3c; font-size: 1.2rem;"></i>
                                            <span style="font-weight: bold; font-size: 1.1rem;">Khuyến mãi</span>
                                        </div>
                                        <div style="display: flex; gap: 10px;">
                                            <input type="text" id="coupon-code" placeholder="Nhập mã giảm giá..."
                                                style="padding: 10px 15px; border: 1px solid #ccc; border-radius: 4px; outline: none; width: 250px;">
                                            <button type="button" onclick="applyCoupon()"
                                                style="padding: 10px 20px; background-color: #5a9153; color: white; border: none; border-radius: 4px; cursor: pointer; font-weight: bold; transition: background 0.3s;">
                                                Áp Dụng
                                            </button>
                                        </div>
                                    </div>

                                    <!-- Thanh toán -->
                                    <div class="cart-summary">
                                        <div class="left-summary">
                                            <input type="checkbox" id="select-all" checked
                                                onchange="selectAll(this.checked)">
                                            <label for="select-all">Chọn tất cả</label>
                                        </div>
                                        <div class="product-total">
                                            Tổng số lượng: <span
                                                id="cart-total-quantity">${sessionScope.cart.selectedQuantity}</span>
                                            <c:if
                                                test="${not empty requestScope.discountAmount and requestScope.discountAmount > 0}">
                                                <span id="discount-container">
                                                    | Giảm giá: <span id="cart-discount-amount"
                                                        style="color: #e74c3c; font-weight: bold;">-
                                                        <f:formatNumber value="${requestScope.discountAmount}"
                                                            pattern="#,##0đ" />
                                                    </span>
                                                    <a href="javascript:void(0)" onclick="removeCoupon()"
                                                        style="color: #e74c3c; margin-left: 5px;"
                                                        title="Hủy mã giảm giá"><i class="fas fa-times"></i></a>
                                                </span>
                                            </c:if>
                                            | Tổng tiền: <span id="cart-total-price">
                                                <f:formatNumber
                                                    value="${requestScope.finalTotal != null ? requestScope.finalTotal : sessionScope.cart.selectedTotalWithDiscount}"
                                                    pattern="#,##0đ" />
                                            </span>
                                        </div>
                                        <a href="${pageContext.request.contextPath}/checkout" class="checkout-btn">
                                            Thanh Toán
                                        </a>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <!-- Popup thông báo -->
                    <div id="cart-popup" class="popup hidden">
                        <div class="popup-content">
                            <p>Thông báo</p>
                        </div>
                    </div>

                    <%@include file="footer.jsp" %>

                        <script>
                            function formatVND(amount) {
                                return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
                            }

                            function applyCoupon() {
                                console.log("Nút áp dụng đã được bấm!");
                                var contextPath = document.body.dataset.contextPath || '';
                                var codeInput = document.getElementById("coupon-code");
                                var code = codeInput ? codeInput.value.trim() : '';

                                if (!code) {
                                    showCartPopup("Vui lòng nhập mã giảm giá!", false);
                                    return;
                                }

                                var formData = new URLSearchParams();
                                formData.append("code", code);

                                fetch(contextPath + "/apply-coupon", {
                                    method: "POST",
                                    headers: {
                                        "Content-Type": "application/x-www-form-urlencoded"
                                    },
                                    body: formData
                                })
                                    .then(function (response) {
                                        if (!response.ok) {
                                            throw new Error("Server trả về lỗi: " + response.status);
                                        }
                                        return response.json();
                                    })
                                    .then(function (data) {
                                        if (data.success) {
                                            showCartPopup(data.message, true);

                                            // Cập nhật hiển thị số tiền giảm
                                            var discountAmount = data.discountAmount || 0;
                                            if (discountAmount > 0) {
                                                var discountDisplay = document.getElementById("cart-discount-amount");
                                                var productTotalDiv = document.querySelector(".product-total");

                                                if (!discountDisplay && productTotalDiv) {
                                                    var discountHtml = '<span id="discount-container"> | Giảm giá: <span id="cart-discount-amount" style="color: #e74c3c; font-weight: bold;">-' + formatVND(discountAmount) + '</span><a href="javascript:void(0)" onclick="removeCoupon()" style="color: #e74c3c; margin-left: 5px;" title="Hủy mã giảm giá"><i class="fas fa-times"></i></a></span>';
                                                    productTotalDiv.insertAdjacentHTML('beforeend', discountHtml);
                                                } else if (discountDisplay) {
                                                    discountDisplay.innerText = '-' + formatVND(discountAmount);
                                                }

                                                if (data.newTotal !== undefined) {
                                                    var totalPriceDisplay = document.getElementById("cart-total-price");
                                                    if (totalPriceDisplay) {
                                                        totalPriceDisplay.innerText = formatVND(data.newTotal);
                                                    }
                                                }

                                                // Xóa nội dung input sau khi áp dụng thành công
                                                if (codeInput) codeInput.value = '';
                                            }
                                        } else {
                                            showCartPopup(data.message, false);
                                        }
                                    })
                                    .catch(function (error) {
                                        console.error("Lỗi apply coupon:", error);
                                        showCartPopup("Có lỗi xảy ra khi áp dụng mã giảm giá!", false);
                                    });
                            }
                        </script>

                <script>
                    function removeCoupon() {
                        var contextPath = document.body.dataset.contextPath || '';

                        fetch(contextPath + "/remove-coupon", {
                            method: "POST"
                        })
                            .then(function (response) {
                                if (!response.ok) {
                                    throw new Error("Server trả về lỗi: " + response.status);
                                }
                                return response.json();
                            })
                            .then(function (data) {
                                if (data.success) {
                                    showCartPopup(data.message, true);

                                    var discountContainer = document.getElementById("discount-container");
                                    if (discountContainer) {
                                        discountContainer.remove();
                                    }

                                    var totalPriceDisplay = document.getElementById("cart-total-price");
                                    if (totalPriceDisplay && data.newTotal !== undefined) {
                                        totalPriceDisplay.innerText = formatVND(data.newTotal);
                                    }

                                    var codeInput = document.getElementById("coupon-code");
                                    if (codeInput) {
                                        codeInput.value = '';
                                    }
                                } else {
                                    showCartPopup(data.message, false);
                                }
                            })
                            .catch(function (error) {
                                console.error("Lỗi remove coupon:", error);
                                showCartPopup("Có lỗi xảy ra khi hủy mã giảm giá!", false);
                            });
                    }
                </script> <c:if test="${not empty stockWarning}">
                    <script>
                        document.addEventListener("DOMContentLoaded", function() {
                            var warningMessage = `<c:out value="${stockWarning}" escapeXml="false"/>`;
                            alert("THÔNG BÁO TỪ GIỎ HÀNG:\n\n" + warningMessage);
                        });
                    </script>
                </c:if>

                <script src="${pageContext.request.contextPath}/js/cart.js?v=<%= System.currentTimeMillis() %>"></script>
            </body>
            </html>