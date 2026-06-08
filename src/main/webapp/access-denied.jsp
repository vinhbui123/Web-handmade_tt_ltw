<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <!DOCTYPE html>
        <html lang="vi">

        <head>
            <title>Truy Cập Bị Từ Chối</title>
            <meta charset="UTF-8">
            <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet">
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }

                body {
                    font-family: 'Roboto', sans-serif;
                    min-height: 100vh;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
                    color: #fff;
                }

                .container {
                    text-align: center;
                    padding: 50px 40px;
                    max-width: 520px;
                    background: rgba(255, 255, 255, 0.05);
                    border: 1px solid rgba(255, 255, 255, 0.1);
                    border-radius: 20px;
                    backdrop-filter: blur(10px);
                    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
                    animation: fadeIn 0.6s ease-out;
                }

                @keyframes fadeIn {
                    from {
                        opacity: 0;
                        transform: translateY(30px);
                    }

                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }

                .icon-lock {
                    font-size: 80px;
                    margin-bottom: 20px;
                    animation: shake 0.5s ease-in-out;
                }

                @keyframes shake {

                    0%,
                    100% {
                        transform: rotate(0deg);
                    }

                    25% {
                        transform: rotate(-10deg);
                    }

                    75% {
                        transform: rotate(10deg);
                    }
                }

                h1 {
                    font-size: 48px;
                    font-weight: 700;
                    background: linear-gradient(135deg, #e74c3c, #f39c12);
                    -webkit-background-clip: text;
                    color: transparent;
                    margin-bottom: 10px;
                }

                h2 {
                    font-size: 20px;
                    font-weight: 400;
                    color: #e0e0e0;
                    margin-bottom: 20px;
                }

                .message {
                    font-size: 15px;
                    color: #aaa;
                    line-height: 1.6;
                    margin-bottom: 30px;
                }

                .role-info {
                    display: inline-block;
                    background: rgba(255, 255, 255, 0.1);
                    padding: 8px 20px;
                    border-radius: 20px;
                    font-size: 14px;
                    color: #f39c12;
                    margin-bottom: 25px;
                    border: 1px solid rgba(243, 156, 18, 0.3);
                }

                .btn-group {
                    display: flex;
                    gap: 15px;
                    justify-content: center;
                    flex-wrap: wrap;
                }

                .btn {
                    display: inline-flex;
                    align-items: center;
                    gap: 8px;
                    padding: 12px 28px;
                    border-radius: 10px;
                    text-decoration: none;
                    font-size: 15px;
                    font-weight: 600;
                    transition: all 0.3s ease;
                }

                .btn-home {
                    background: linear-gradient(135deg, #3498db, #2980b9);
                    color: white;
                }

                .btn-home:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 5px 15px rgba(52, 152, 219, 0.4);
                }

                .btn-back {
                    background: rgba(255, 255, 255, 0.1);
                    color: #ccc;
                    border: 1px solid rgba(255, 255, 255, 0.2);
                }

                .btn-back:hover {
                    background: rgba(255, 255, 255, 0.15);
                    transform: translateY(-2px);
                }
            </style>
        </head>

        <body>

            <c:set var="userRole" value="${sessionScope.user.role}" />

            <div class="container">
                <div class="icon-lock">🔒</div>
                <h1>403</h1>
                <h2>Truy Cập Bị Từ Chối</h2>

                <p class="message">
                    Bạn không có quyền truy cập vào trang này.<br>
                    Vui lòng liên hệ quản trị viên nếu bạn cho rằng đây là lỗi.
                </p>

                <c:if test="${sessionScope.user != null}">
                    <div class="role-info">
                        Vai trò hiện tại:
                        <c:choose>
                            <c:when test="${userRole == 0}">User</c:when>
                            <c:when test="${userRole == 1}">Admin</c:when>
                            <c:when test="${userRole == 2}">Seller</c:when>
                            <c:when test="${userRole == 3}">Mod Nhập Hàng</c:when>
                            <c:when test="${userRole == 4}">Kiểm Duyệt Viên</c:when>
                            <c:otherwise>Không xác định</c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <div class="btn-group">
                    <a href="<%= request.getContextPath() %>/home" class="btn btn-home">Trang Chủ</a>
                    <a href="javascript:history.back()" class="btn btn-back">← Quay Lại</a>
                </div>
            </div>

        </body>

        </html>