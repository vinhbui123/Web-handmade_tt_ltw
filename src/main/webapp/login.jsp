<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng Nhập</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">

    <style>
        .error-message {
            color: red;
            font-weight: bold;
            text-align: center;
            margin-bottom: 10px;
        }

        /* --- Show/Hide Password Toggle --- */
        .password-wrapper {
            flex: 1;
            position: relative;
            display: flex;
            align-items: center;
        }

        .password-wrapper .login__input {
            width: 100%;
            padding-right: 42px; /* room for the eye icon */
        }

        .toggle-password {
            position: absolute;
            right: 12px;
            background: none;
            border: none;
            cursor: pointer;
            color: #999;
            font-size: 16px;
            padding: 0;
            line-height: 1;
            display: none;          /* hidden until user types */
            transition: color 0.2s ease;
        }

        .toggle-password:hover {
            color: #333;
        }

        /* --- Custom UI for Social Login --- */
        .social-divider {
            display: flex;
            align-items: center;
            text-align: center;
            margin: 25px 0 15px 0;
            color: #777;
            font-size: 14px;
        }
        .social-divider::before,
        .social-divider::after {
            content: '';
            flex: 1;
            border-bottom: 1px solid #ccc;
        }
        .social-divider:not(:empty)::before {
            margin-right: .5em;
        }
        .social-divider:not(:empty)::after {
            margin-left: .5em;
        }

        .social-login-container {
            display: flex;
            justify-content: space-between;
            gap: 15px;
        }

        .social-btn {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 10px 15px;
            border-radius: 5px;
            text-decoration: none;
            color: white;
            font-weight: 500;
            font-size: 14px;
            transition: opacity 0.3s ease, transform 0.2s ease;
        }

        .social-btn:hover {
            opacity: 0.9;
            transform: translateY(-2px);
            color: white;
        }

        .social-btn i {
            margin-right: 8px;
            font-size: 18px;
        }

        .google-btn {
            background-color: #db4a39;
        }

        .facebook-btn {
            background-color: #3b5998;
        }
    </style>
</head>
<body>

<%@ include file="header.jsp" %>

<div class="container">
    <div class="screen">
        <form class="login" action="login" method="post">
            <div class="login-title"><h3>Đăng Nhập Tài Khoản!</h3></div>

            <c:if test="${not empty success}">
                <div style="padding: 15px; margin: 20px 0; border-radius: 5px; font-size: 16px; color: #155724; background-color: #d4edda; border: 1px solid #c3e6cb; text-align: center;">
                        ${success}
                </div>
            </c:if>

            <c:if test="${not empty errorMessage}">
                <div class="error-message">${errorMessage}</div>
            </c:if>

            <div class="login__field">
                <i class="login__icon fas fa-user"></i>
                <input type="text" class="login__input" name="username" placeholder="Tên đăng nhập hoặc Email" required value="${username}">
            </div>

            <div class="login__field">
                <i class="login__icon fas fa-lock"></i>
                <div class="password-wrapper">
                    <input type="password" class="login__input" id="passwordInput" name="password" placeholder="Mật khẩu" required>
                    <button type="button" class="toggle-password" id="togglePassword" title="Hiện/Ẩn mật khẩu" aria-label="Hiện/Ẩn mật khẩu">
                        <i class="fas fa-eye" id="toggleIcon"></i>
                    </button>
                </div>
            </div>

            <div class="login__field captcha-group" style="display: flex; align-items: center; justify-content: space-between; gap: 10px; margin-bottom: 20px;">
                <div style="flex: 1; position: relative; display: flex; align-items: center;">
                    <i class="login__icon fas fa-shield-alt" style="position: absolute; left: 15px; color: #333; margin: 0; font-size: 20px;"></i>
                    <input type="text" class="login__input" name="captcha" placeholder="Mã CAPTCHA" required style="padding-left: 45px; width: 100%; box-sizing: border-box; height: 42px; margin: 0;">
                </div>
                <div style="display: flex; align-items: center; gap: 5px;">
                    <img src="${pageContext.request.contextPath}/captcha" id="captchaImage" alt="CAPTCHA" style="height: 42px; border-radius: 8px; cursor: pointer; border: 1px solid #ccc; background: white;" onclick="document.getElementById('captchaImage').src='${pageContext.request.contextPath}/captcha?' + new Date().getTime();">
                    <button type="button" class="btn-refresh" onclick="document.getElementById('captchaImage').src='${pageContext.request.contextPath}/captcha?' + new Date().getTime();" style="background: none; border: none; cursor: pointer; color: #333; font-size: 20px; padding: 5px; outline: none;" title="Tải lại mã">
                        <i class="fas fa-sync-alt"></i>
                    </button>
                </div>
            </div>

            <div class="login__field" style="display: flex; align-items: center; gap: 8px; margin-bottom: 15px; padding-left: 5px;">
                <input type="checkbox" id="remember" name="remember" style="width: 16px; height: 16px; cursor: pointer;">
                <label for="remember" style="font-size: 14px; color: #555; cursor: pointer; user-select: none;">Ghi nhớ đăng nhập</label>
            </div>

            <button type="submit" class="button login__submit">
                <span class="button__text">Đăng Nhập</span>
                <i class="button__icon fas fa-chevron-right"></i>
            </button>

            <div class="login__options">
                <a href="${pageContext.request.contextPath}/forgot-password" class="login__link">Quên mật khẩu?</a>
                <a href="${pageContext.request.contextPath}/register.jsp" class="login__link">Bạn chưa có tài khoản? Đăng ký</a>
            </div>

            <div class="social-divider">Hoặc đăng nhập bằng</div>

            <div class="social-login-container">
                <a href="${pageContext.request.contextPath}/google-login" class="social-btn google-btn">
                    <i class="fab fa-google"></i> Google
                </a>
                <a href="${pageContext.request.contextPath}/facebook-login" class="social-btn facebook-btn">
                    <i class="fab fa-facebook-f"></i> Facebook
                </a>
            </div>

        </form>
    </div>
</div>
<%@ include file="footer.jsp" %>

<script>
    (function () {
        var passwordInput = document.getElementById('passwordInput');
        var toggleBtn    = document.getElementById('togglePassword');
        var toggleIcon   = document.getElementById('toggleIcon');

        // Show the eye button only when the user has typed something
        passwordInput.addEventListener('input', function () {
            toggleBtn.style.display = passwordInput.value.length > 0 ? 'block' : 'none';
        });

        // Toggle between password and text
        toggleBtn.addEventListener('click', function () {
            var isPassword = passwordInput.type === 'password';
            passwordInput.type = isPassword ? 'text' : 'password';
            toggleIcon.classList.toggle('fa-eye',      !isPassword);
            toggleIcon.classList.toggle('fa-eye-slash', isPassword);
        });
    })();
</script>

</body>
</html>