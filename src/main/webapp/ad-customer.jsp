<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="vn.edu.hcmuaf.fit.Web_ban_hang.services.UserService" %>
<%@ page import="vn.edu.hcmuaf.fit.Web_ban_hang.model.User" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    // Lấy danh sách khách hàng từ service
    UserService userService = new UserService();
    List<User> users = userService.getAllUsers();

    // Lấy role của user đang đăng nhập
    User currentUser = (User) session.getAttribute("user");
    int currentRole = (currentUser != null) ? currentUser.getRole() : 0;
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản Lý Tài Khoản - Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" href="css/admin.css">
    <script src="js/admin.js"></script>
</head>
<body>

<%@include file="ad-menu.jsp" %>

<div class="main-content">
    <header>
        <h1>Quản Lý Tài Khoản</h1>
    </header>
    <%
        String message = null;
        if (session != null && session.getAttribute("message") != null) {
            message = (String) session.getAttribute("message");
            session.removeAttribute("message");
        }
    %>
    <% if (message != null) { %>
    <div class="alert alert-info" style="margin: 10px 0; color: green;"><%= message %></div>
    <% } %>

    <section class="customer-management">
        <table class="customer-table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Hành Động</th>
            </tr>
            </thead>
            <tbody>
            <% if (users != null && !users.isEmpty()) {
                for (User user : users) {
                    int roleId = user.getRole();
                    String roleName = "Không xác định";
                    String roleColor = "#7f8c8d";
                    switch (roleId) {
                        case 0: roleName = "User"; roleColor = "#3498db"; break;
                        case 1: roleName = "Admin"; roleColor = "#e74c3c"; break;
                        case 2: roleName = "Seller"; roleColor = "#27ae60"; break;
                        case 3: roleName = "Mod Nhập Hàng"; roleColor = "#f39c12"; break;
                        case 4: roleName = "Kiểm Duyệt Viên"; roleColor = "#9b59b6"; break;
                        default: break;
                    }
            %>
            <tr>
                <td><%= user.getId() %></td>
                <td><%= user.getUsername() %></td>
                <td><%= user.getEmail() %></td>
                <td>
                    <% String badgeStyle = "background-color:" + roleColor + ";color:white;padding:4px 10px;border-radius:12px;font-size:13px;font-weight:bold;"; %>
                    <span style="<%= badgeStyle %>"><%= roleName %></span>
                </td>
                <td><%= user.getStatus() == 1 ? "Hoạt động" : "Bị khóa" %></td>
                <td>
                    <c:if test="${sessionScope.user.role == 1 || sessionScope.user.role == 4}">
                        <i class="fa-solid fa-pen-to-square btn-edit"
                           onclick="openCustomerModal('<%= user.getId() %>', '<%= user.getRole() %>', '<%= user.getStatus() %>')">
                        </i>
                    </c:if>
                </td>
            </tr>
            <% }
            } else { %>
            <tr>
                <td colspan="6" style="text-align: center;">Không có dữ liệu khách hàng</td>
            </tr>
            <% } %>
            </tbody>
        </table>
    </section>

    <!-- Modal chỉnh sửa khách hàng -->
    <div id="customerModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeCustomerModal()">&times;</span>
            <h2>Chỉnh Sửa Quyền Hạn Tài Khoản</h2>
            <form method="post" action="${pageContext.request.contextPath}/adminUsers">
                <input type="hidden" id="customerId" name="customerId">

                <label for="customerRole">Vai Trò:</label>
                <select id="customerRole" name="customerRole">
                    <option value="0">User</option>
                    <% if (currentRole == 1) { %>
                    <option value="1">Admin</option>
                    <% } %>
                    <option value="2">Seller</option>
                    <option value="3">Mod Nhập Hàng</option>
                    <option value="4">Kiểm Duyệt Viên</option>
                </select>
                <label for="customerStatus">Trạng Thái:</label>
                <select id="customerStatus" name="customerStatus">
                    <option value="1">Hoạt Động</option>
                    <option value="0">Bị Khóa</option>
                </select>
                <button type="submit" class="btn-save">Lưu</button>
            </form>
        </div>
    </div>

</div>

<script>
    function openCustomerModal(userId, userRole, userStatus) {
        document.getElementById("customerId").value = userId;
        document.getElementById("customerRole").value = userRole;
        document.getElementById("customerStatus").value = userStatus;
        document.getElementById("customerModal").style.display = "block";
    }

    function closeCustomerModal() {
        document.getElementById("customerModal").style.display = "none";
    }
</script>

</body>
</html>