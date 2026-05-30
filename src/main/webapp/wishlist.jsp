<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Danh Sách Yêu Thích</title>
    <link href="https://fonts.googleapis.com/css2?family=Roboto&display=swap&subset=vietnamese" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/product.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/wishlist.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">

<%@include file="header.jsp" %>

<div class="wishlist-container">
    <div class="wishlist-header">
        <h2><i class="fas fa-heart"></i> Danh Sách Yêu Thích</h2>
        <c:if test="${not empty wishlistProducts}">
            <span class="wishlist-total">${wishlistProducts.size()} sản phẩm</span>
        </c:if>
    </div>

    <c:choose>
        <c:when test="${not empty wishlistProducts}">
            <div class="wishlist-grid">
                <c:forEach var="p" items="${wishlistProducts}">
                    <div class="wishlist-item">
                        <c:if test="${p.discount != 0}">
                            <div class="discount">-${p.discount}%</div>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">
                            <div class="wishlist-item-img">
                                <img src="${p.img}" alt="${p.name}">
                            </div>
                        </a>
                        <div class="wishlist-item-info">
                            <div class="wishlist-item-name">
                                <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}">${p.name}</a>
                            </div>
                            <div class="wishlist-item-price">
                                <c:choose>
                                    <c:when test="${p.discount > 0}">
                                        <f:formatNumber value="${p.price - (p.price * p.discount / 100)}" pattern="#,##0đ" />
                                        <span class="gia-cu"><f:formatNumber value="${p.price}" pattern="#,##0đ" /></span>
                                    </c:when>
                                    <c:otherwise>
                                        <f:formatNumber value="${p.price}" pattern="#,##0đ" />
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div class="wishlist-item-actions">
                                <a href="${pageContext.request.contextPath}/product-detail?id=${p.id}" class="btn-add-cart">
                                    <i class="fas fa-eye"></i> Xem Chi Tiết
                                </a>
                                <button class="btn-remove-wishlist"
                                        onclick="removeFromWishlist(${p.id}, this, event)">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="wishlist-empty">
                <i class="far fa-heart"></i>
                <h3>Danh sách yêu thích trống</h3>
                <p>Bạn chưa có sản phẩm yêu thích nào. Hãy khám phá và thêm sản phẩm ngay!</p>
                <a href="${pageContext.request.contextPath}/list-product?category=all" class="btn-shop-now">
                    <i class="fas fa-store"></i> Khám Phá Sản Phẩm
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<%@include file="footer.jsp" %>

<script src="${pageContext.request.contextPath}/js/wishlist.js"></script>
</body>
</html>
