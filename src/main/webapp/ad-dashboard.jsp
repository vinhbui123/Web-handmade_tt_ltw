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
                                            <i class="fa-solid fa-list-ul"></i>
                                            <c:choose>
                                                <c:when test="${role == 1}">Thống Kê Tổng Quan</c:when>
                                                <c:when test="${role == 2}">Thống Kê Bán Hàng</c:when>
                                                <c:when test="${role == 3}">Quản Lý Nhập Hàng</c:when>
                                                <c:when test="${role == 4}">Quản Lý Người Dùng</c:when>
                                                <c:otherwise>Bảng Điều Khiển</c:otherwise>
                                            </c:choose>
                                        </h1>
                                        <span class="header-role-tag">
                                            <c:choose>
                                                <c:when test="${role == 1}">Admin</c:when>
                                                <c:when test="${role == 2}">Seller</c:when>
                                                <c:when test="${role == 3}">Mod Nhập Hàng</c:when>
                                                <c:when test="${role == 4}">Kiểm Duyệt Viên</c:when>
                                            </c:choose>
                                        </span>
                                    </header>

                                    <%--===Chào mừng===--%>
                                        <div class="stat-card" style="flex: 1 1 100%; max-width: 100%; display: flex; flex-direction: row; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                                            <div>
                                                <h2 style="margin-bottom: 5px; color: #111;">Xin chào, ${sessionScope.user.firstName} ${sessionScope.user.lastName}!</h2>
                                                <p style="color: #666; font-size: 14px;">
                                                    <c:choose>
                                                        <c:when test="${role == 1}">Đăng nhập với quyền Admin — Toàn quyền quản trị hệ thống.</c:when>
                                                        <c:when test="${role == 2}">Đăng nhập với quyền Seller — Quản lý sản phẩm, đơn hàng và kho hàng.</c:when>
                                                        <c:when test="${role == 3}">Đăng nhập với quyền Mod Nhập Hàng — Nhập hàng và kiểm tra tồn kho.</c:when>
                                                        <c:when test="${role == 4}">Đăng nhập với quyền Kiểm Duyệt Viên — Quản lý tài khoản và đánh giá.</c:when>
                                                    </c:choose>
                                                </p>
                                            </div>
                                            <i class="fa-solid fa-box-open" style="font-size: 45px; color: #f39c12; opacity: 0.9;"></i>
                                        </div>

                                        <div class="stat-row">

                                            <%--===Thống kê Đơn Hàng: Admin (1), Seller (2)===--%>
                                                <c:if test="${role == 1 || role == 2}">
                                                    <div class="stat-card">
                                                        <div class="title">Tổng số đơn hàng</div>
                                                        <div class="value"><%= stats.get("total_orders") %></div>
                                                        <div class="sub-text positive">Cập nhật hôm nay</div>
                                                    </div>

                                                    <div class="stat-card">
                                                        <div class="title">Đơn chờ xử lý</div>
                                                        <div class="value"><%= stats.get("pending_orders") %></div>
                                                        <div class="sub-text negative">Cần xác nhận</div>
                                                    </div>

                                                    <div class="stat-card">
                                                        <div class="title">Đã xác nhận</div>
                                                        <div class="value"><%= stats.get("confirmed_orders") %></div>
                                                        <div class="sub-text">Đang giao hàng</div>
                                                    </div>

                                                    <div class="stat-card">
                                                        <div class="title">Hoàn thành</div>
                                                        <div class="value"><%= stats.get("done_orders") %></div>
                                                        <div class="sub-text positive">Thành công</div>
                                                    </div>

                                                    <div class="stat-card">
                                                        <div class="title">Đã huỷ</div>
                                                        <div class="value"><%= stats.get("cancelled_orders") %></div>
                                                        <div class="sub-text negative">Giao thất bại / Khách hủy</div>
                                                    </div>

                                                    <div class="stat-card">
                                                        <div class="title">Tổng doanh thu</div>
                                                        <div class="value"><%= stats.get("total_revenue") !=null ? format.format(stats.get("total_revenue")) : "0" %> ₫</div>
                                                        <div class="sub-text positive">Tăng trưởng tốt</div>
                                                    </div>
                                                </c:if>

                                                <%--===Thống kê User: Admin (1), Kiểm Duyệt Viên (4)===--%>
                                                    <c:if test="${role == 1 || role == 4}">
                                                        <div class="stat-card">
                                                            <div class="title">Tổng người dùng</div>
                                                            <div class="value">${userStats.total_users}</div>
                                                            <div class="sub-text">Thành viên hệ thống</div>
                                                        </div>

                                                        <div class="stat-card">
                                                            <div class="title">Người dùng bị khóa</div>
                                                            <div class="value">${userStats.locked_users}</div>
                                                            <div class="sub-text negative">Vi phạm chính sách</div>
                                                        </div>
                                                    </c:if>

                                                    <%--===Mod Nhập Hàng (3): Thông tin nhanh===--%>
                                                    <c:if test="${role == 3}">
                                                        <div class="stat-card">
                                                            <div class="title">Đơn nhập hôm nay</div>
                                                            <div class="value">12</div>
                                                            <div class="sub-text positive">↑ 3 so với hôm qua</div>
                                                        </div>
                                                        <div class="stat-card">
                                                            <div class="title">Sản phẩm tồn kho</div>
                                                            <div class="value">348</div>
                                                            <div class="sub-text">Cập nhật liên tục</div>
                                                        </div>
                                                        <div class="stat-card">
                                                            <div class="title">Sắp hết hàng</div>
                                                            <div class="value">5</div>
                                                            <div class="sub-text negative">Cần nhập thêm</div>
                                                        </div>

                                                        <div style="flex: 1 1 100%;">
                                                            <div class="quick-actions">
                                                                <a href="adminInventory" class="qa-card">
                                                                    <div class="qa-icon bg-mint"><i class="fa-solid fa-right-left"></i></div>
                                                                    <h3>Xuất nhập kho</h3>
                                                                    <p>Ghi nhận phiếu nhập, xuất hàng theo lô</p>
                                                                    <span class="qa-link">Đi đến trang <i class="fa-solid fa-arrow-right"></i></span>
                                                                </a>
                                                                <a href="adminProducts" class="qa-card">
                                                                    <div class="qa-icon bg-orange"><i class="fa-solid fa-list"></i></div>
                                                                    <h3>Danh sách sản phẩm</h3>
                                                                    <p>Xem và tìm kiếm toàn bộ sản phẩm trong kho</p>
                                                                    <span class="qa-link">Xem danh sách <i class="fa-solid fa-arrow-right"></i></span>
                                                                </a>
                                                            </div>
                                                        </div>
                                                    </c:if>

                                        </div>
                                </div>

                        </body>

                        </html>