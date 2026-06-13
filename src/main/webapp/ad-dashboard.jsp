<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="java.util.Map" %>
        <%@ page import="java.text.NumberFormat" %>
            <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

                <% Map<String, Object> stats = (Map<String, Object>) request.getAttribute("stats");
                        NumberFormat format = NumberFormat.getInstance();
                        %>

                        <html>

                        <head>
                            <title>Thống kê đơn hàng</title>
                            <meta charset="UTF-8">
                            <link href="https://fonts.googleapis.com/css2?family=Roboto&display=swap" rel="stylesheet">
                            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
                        </head>

                        <body>

                            <%@include file="ad-menu.jsp" %>

                                <c:set var="role" value="${sessionScope.user.role}" />

                                <div class="main-content">
                                    <header>
                                        <h1>
                                            <c:choose>
                                                <c:when test="${role == 1}">Thống Kê Tổng Quan</c:when>
                                                <c:when test="${role == 2}">Thống Kê Bán Hàng</c:when>
                                                <c:when test="${role == 3}">Quản Lý Nhập Hàng</c:when>
                                                <c:when test="${role == 4}">Quản Lý Người Dùng</c:when>
                                                <c:otherwise>Bảng Điều Khiển</c:otherwise>
                                            </c:choose>
                                        </h1>
                                    </header>

                                    <%--===Chào mừng===--%>
                                        <div
                                            style="margin: 20px 0; padding: 15px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 10px; color: white;">
                                            <h2 style="color: white; margin-bottom: 5px;">Xin chào,
                                                ${sessionScope.user.firstName} ${sessionScope.user.lastName}!</h2>
                                            <p style="opacity: 0.9; font-size: 14px;">
                                                <c:choose>
                                                    <c:when test="${role == 1}">Bạn đang đăng nhập với quyền Admin —
                                                        Toàn quyền quản trị hệ thống.</c:when>
                                                    <c:when test="${role == 2}">Bạn đang đăng nhập với quyền Seller —
                                                        Quản lý sản phẩm, đơn hàng và kho hàng.</c:when>
                                                    <c:when test="${role == 3}">Bạn đang đăng nhập với quyền Mod Nhập
                                                        Hàng — Nhập hàng và kiểm tra tồn kho.</c:when>
                                                    <c:when test="${role == 4}">Bạn đang đăng nhập với quyền Kiểm Duyệt
                                                        Viên — Quản lý tài khoản và đánh giá.</c:when>
                                                </c:choose>
                                            </p>
                                        </div>

                                        <div class="stat-row">

                                            <%--===Thống kê Đơn Hàng: Admin (1), Seller (2)===--%>
                                                <c:if test="${role == 1 || role == 2}">
                                                    <div class="stat-card total-orders">
                                                        <div class="title">Tổng số đơn hàng</div>
                                                        <div class="value">
                                                            <%= stats.get("total_orders") %>
                                                        </div>
                                                    </div>

                                                    <div class="stat-card pending-orders">
                                                        <div class="title">Đơn chờ xử lý</div>
                                                        <div class="value">
                                                            <%= stats.get("pending_orders") %>
                                                        </div>
                                                    </div>

                                                    <div class="stat-card confirmed-orders">
                                                        <div class="title">Đã xác nhận</div>
                                                        <div class="value">
                                                            <%= stats.get("confirmed_orders") %>
                                                        </div>
                                                    </div>

                                                    <div class="stat-card done-orders">
                                                        <div class="title">Hoàn thành</div>
                                                        <div class="value">
                                                            <%= stats.get("done_orders") %>
                                                        </div>
                                                    </div>

                                                    <div class="stat-card cancelled-orders">
                                                        <div class="title">Đã huỷ</div>
                                                        <div class="value">
                                                            <%= stats.get("cancelled_orders") %>
                                                        </div>
                                                    </div>

                                                    <div class="stat-card total-revenue">
                                                        <div class="title">Tổng doanh thu</div>
                                                        <div class="value">
                                                            <%= stats.get("total_revenue") !=null ?
                                                                format.format(stats.get("total_revenue")) : "0" %> VNĐ
                                                        </div>
                                                    </div>
                                                </c:if>

                                                <%--===Thống kê User: Admin (1), Kiểm Duyệt Viên (4)===--%>
                                                    <c:if test="${role == 1 || role == 4}">
                                                        <div class="stat-card total-users"
                                                            style="background-color:#34495e;">
                                                            <div class="title"> Tổng người dùng</div>
                                                            <div class="value">${userStats.total_users}</div>
                                                        </div>

                                                        <div class="stat-card total-lock-users"
                                                            style="background-color:#7f8c8d;">
                                                            <div class="title">Người dùng bị khóa</div>
                                                            <div class="value">${userStats.locked_users}</div>
                                                        </div>
                                                    </c:if>

                                                    <%--===Mod Nhập Hàng (3): Thông tin nhanh===--%>
                                                        <c:if test="${role == 3}">
                                                            <div class="stat-card"
                                                                style="background-color:#17a2b8; flex: 0 0 100%; max-width: 500px;">
                                                                <div class="title"> Truy cập nhanh</div>
                                                                <div class="value" style="font-size: 16px;">
                                                                    <a href="adminInventory"
                                                                        style="color: white; text-decoration: underline;">→
                                                                        Đi đến trang Xuất Nhập Kho</a>
                                                                </div>
                                                            </div>
                                                            <div class="stat-card"
                                                                style="background-color:#20c997; flex: 0 0 100%; max-width: 500px;">
                                                                <div class="title"> Xem sản phẩm</div>
                                                                <div class="value" style="font-size: 16px;">
                                                                    <a href="adminProducts"
                                                                        style="color: white; text-decoration: underline;">→
                                                                        Xem danh sách sản phẩm</a>
                                                                </div>
                                                            </div>
                                                        </c:if>

                                        </div>
                                </div>

                        </body>

                        </html>