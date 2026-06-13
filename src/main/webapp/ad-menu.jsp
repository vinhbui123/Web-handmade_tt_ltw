<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>menuAdmin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>

<%-- Lấy role từ session --%>
<c:set var="userRole" value="${sessionScope.user.role}" />
<c:set var="userName" value="${sessionScope.user.username}" />

<div class="sidebar">
    <h2>Kênh Quản Trị</h2>

    <%-- Hiển thị thông tin user & role --%>
    <div class="sidebar-user-info">
        <div class="sidebar-avatar">
            <i class="fas fa-user-tie"></i>
        </div>
        <div class="sidebar-user-details">
            <span class="sidebar-username">${userName}</span>
            <span class="sidebar-role">
                <c:choose>
                    <c:when test="${userRole == 1}">Admin</c:when>
                    <c:when test="${userRole == 2}">Seller</c:when>
                    <c:when test="${userRole == 3}">Mod Nhập Hàng</c:when>
                    <c:when test="${userRole == 4}">Kiểm Duyệt Viên</c:when>
                    <c:otherwise>Không xác định</c:otherwise>
                </c:choose>
            </span>
        </div>
    </div>

    <ul>
        <%-- Trang Chủ: tất cả đều thấy --%>
        <li><a href="home"><i class="fa-solid fa-house"></i> Trang Chủ</a></li>

        <%-- Dashboard: tất cả role admin đều thấy --%>
        <li>
            <a href="admin">
                <i class="fa-solid fa-chart-line"></i>
                <c:choose>
                    <c:when test="${userRole == 1}">Thống Kê Tổng Quan</c:when>
                    <c:when test="${userRole == 2}">Thống Kê Bán Hàng</c:when>
                    <c:when test="${userRole == 3}">Bảng Điều Khiển Nhập Hàng</c:when>
                    <c:when test="${userRole == 4}">Bảng Điều Khiển Cộng Đồng</c:when>
                    <c:otherwise>Bảng Điều Khiển</c:otherwise>
                </c:choose>
            </a>
        </li>

        <%-- === Nhóm Sản Phẩm: Admin (1), Seller (2), Mod Nhập Hàng (3) === --%>
        <c:if test="${userRole == 1 || userRole == 2 || userRole == 3}">
            <li><a href="adminProducts"><i class="fa-solid fa-box-open"></i> Quản Lý Sản Phẩm</a></li>
        </c:if>

        <%-- === Danh Mục & Chất Liệu: Admin (1), Seller (2) === --%>
        <c:if test="${userRole == 1 || userRole == 2}">
            <li><a href="adminCategorys"><i class="fa-solid fa-tags"></i> Quản Lý Danh Mục</a></li>
            <li><a href="adminMaterials"><i class="fa-solid fa-layer-group"></i> Quản Lý Chất Liệu</a></li>
        </c:if>

        <%-- === Đơn Hàng: Admin (1), Seller (2) === --%>
        <c:if test="${userRole == 1 || userRole == 2}">
            <li><a href="adminOrders"><i class="fa-solid fa-receipt"></i> Quản Lý Đơn Hàng</a></li>
        </c:if>

        <%-- === Xuất Nhập Kho: Admin (1), Seller (2), Mod Nhập Hàng (3) === --%>
        <c:if test="${userRole == 1 || userRole == 2 || userRole == 3}">
            <li><a href="adminInventory"><i class="fa-solid fa-warehouse"></i> Quản Lý Xuất Nhập Kho</a></li>
        </c:if>

        <%-- === Mã Giảm Giá: Admin (1), Seller (2) === --%>
        <c:if test="${userRole == 1 || userRole == 2}">
            <li><a href="adminCoupons"><i class="fa-solid fa-ticket"></i> Quản Lý Mã Giảm Giá</a></li>
        </c:if>

        <%-- === Quản Lý Tài Khoản: Admin (1), Kiểm Duyệt Viên (4) === --%>
        <c:if test="${userRole == 1 || userRole == 4}">
            <li><a href="adminUsers"><i class="fa-solid fa-users-gear"></i> Quản Lý Tài Khoản</a></li>
        </c:if>

        <%-- === Quản Lý Đánh Giá: Admin (1), Kiểm Duyệt Viên (4) === --%>
        <c:if test="${userRole == 1 || userRole == 4}">
            <li><a href="adminComments"><i class="fa-solid fa-comments"></i> Quản Lý Đánh Giá</a></li>
        </c:if>

        <%-- Đăng Xuất: tất cả --%>
        <li class="sidebar-bottom"><a href="logout" class="btn-logout"><i class="fa-solid fa-right-from-bracket"></i> Đăng Xuất</a></li>
    </ul>
</div>

<script>
    document.addEventListener("DOMContentLoaded", function() {
        const currentPath = window.location.pathname;
        const menuLinks = document.querySelectorAll('.sidebar ul li a');
        menuLinks.forEach(link => {
            const href = link.getAttribute('href');
            if (href && href !== 'logout' && currentPath.includes(href)) {
                link.classList.add('active');
            }
        });
    });
</script>
</body>
</html>
