<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 5/4/2026
  Time: 10:55 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Xác minh OTP</title>
</head>
<body>
<h2>Nhập mã OTP được gửi đến số điện thoại của bạn</h2>
<form action="verify-otp" method="post">
    <input type="text" name="otp" placeholder="Mã OTP" required/>
    <button type="submit">Xác minh</button>
</form>

<p style="color:red;">
    <c:if test="${not empty errorMessage}">
        ${errorMessage}
    </c:if>
</p>
</body>
</html>