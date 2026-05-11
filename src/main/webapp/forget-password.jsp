<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Quên Mật Khẩu - Handmade Shop</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">

  <style>
    *, *::before, *::after { box-sizing: border-box; }

    body {
      font-family: 'Inter', sans-serif;
      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      margin: 0;
      padding: 20px;
    }

    .fp-card {
      background: rgba(255,255,255,0.04);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border: 1px solid rgba(255,255,255,0.12);
      border-radius: 20px;
      padding: 48px 44px 40px;
      width: 100%;
      max-width: 440px;
      box-shadow: 0 24px 60px rgba(0,0,0,0.5);
      animation: slideUp 0.5s ease;
    }

    @keyframes slideUp {
      from { opacity:0; transform:translateY(30px); }
      to   { opacity:1; transform:translateY(0); }
    }

    .fp-icon-wrap {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 72px;
      height: 72px;
      background: linear-gradient(135deg, #e2b96f, #c9973a);
      border-radius: 50%;
      margin: 0 auto 24px;
      box-shadow: 0 8px 24px rgba(226,185,111,0.4);
    }

    .fp-icon-wrap i { font-size: 30px; color: #1a1a2e; }

    .fp-title {
      text-align: center;
      color: #ffffff;
      font-size: 24px;
      font-weight: 700;
      margin: 0 0 10px;
      letter-spacing: 0.3px;
    }

    .fp-subtitle {
      text-align: center;
      color: rgba(255,255,255,0.55);
      font-size: 14px;
      line-height: 1.65;
      margin: 0 0 32px;
    }

    /* Alerts */
    .alert {
      display: flex;
      align-items: flex-start;
      gap: 10px;
      padding: 14px 16px;
      border-radius: 10px;
      font-size: 14px;
      line-height: 1.5;
      margin-bottom: 20px;
      animation: fadeIn 0.3s ease;
    }
    @keyframes fadeIn { from{opacity:0} to{opacity:1} }

    .alert-success {
      background: rgba(34,197,94,0.15);
      border: 1px solid rgba(34,197,94,0.35);
      color: #86efac;
    }
    .alert-success i { color: #22c55e; margin-top: 2px; }

    .alert-danger {
      background: rgba(239,68,68,0.15);
      border: 1px solid rgba(239,68,68,0.35);
      color: #fca5a5;
    }
    .alert-danger i { color: #ef4444; margin-top: 2px; }

    /* Form */
    .fp-label {
      display: block;
      color: rgba(255,255,255,0.75);
      font-size: 13px;
      font-weight: 500;
      margin-bottom: 8px;
      letter-spacing: 0.3px;
    }

    .fp-input-wrap {
      position: relative;
      margin-bottom: 24px;
    }

    .fp-input-wrap input[type="email"] {
      width: 100%;
      padding: 14px 46px 14px 16px;
      background: rgba(255,255,255,0.07);
      border: 1px solid rgba(255,255,255,0.15);
      border-radius: 10px;
      color: #fff;
      font-size: 15px;
      font-family: 'Inter', sans-serif;
      outline: none;
      transition: border-color 0.25s, box-shadow 0.25s, background 0.25s;
    }

    .fp-input-wrap input[type="email"]::placeholder { color: rgba(255,255,255,0.3); }

    .fp-input-wrap input[type="email"]:focus {
      border-color: #e2b96f;
      background: rgba(255,255,255,0.10);
      box-shadow: 0 0 0 3px rgba(226,185,111,0.18);
    }

    .fp-input-wrap .fp-field-icon {
      position: absolute;
      right: 14px;
      top: 50%;
      transform: translateY(-50%);
      color: rgba(255,255,255,0.35);
      font-size: 16px;
      pointer-events: none;
      transition: color 0.25s;
    }

    .fp-input-wrap input:focus ~ .fp-field-icon { color: #e2b96f; }

    .fp-submit-btn {
      width: 100%;
      padding: 14px;
      background: linear-gradient(135deg, #e2b96f, #c9973a);
      border: none;
      border-radius: 10px;
      color: #1a1a2e;
      font-size: 16px;
      font-weight: 700;
      font-family: 'Inter', sans-serif;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s, opacity 0.2s;
      letter-spacing: 0.3px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }

    .fp-submit-btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(226,185,111,0.45);
      opacity: 0.95;
    }

    .fp-submit-btn:active { transform: translateY(0); }

    .fp-back-link {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      margin-top: 22px;
      color: rgba(255,255,255,0.5);
      text-decoration: none;
      font-size: 14px;
      transition: color 0.2s;
    }
    .fp-back-link:hover { color: #e2b96f; }
    .fp-back-link i { font-size: 13px; }
  </style>
</head>
<body>

<div class="fp-card">

  <!-- Icon -->
  <div class="fp-icon-wrap">
    <i class="fa-solid fa-unlock-keyhole"></i>
  </div>

  <h1 class="fp-title">Quên mật khẩu?</h1>
  <p class="fp-subtitle">
    Nhập địa chỉ email của bạn. Chúng tôi sẽ gửi liên kết đặt lại mật khẩu đến hộp thư.
  </p>

  <!-- Thông báo -->
  <c:if test="${not empty message}">
    <div class="alert alert-success">
      <i class="fa-solid fa-circle-check"></i>
      <span>${message}</span>
    </div>
  </c:if>
  <c:if test="${not empty error}">
    <div class="alert alert-danger">
      <i class="fa-solid fa-circle-exclamation"></i>
      <span>${error}</span>
    </div>
  </c:if>

  <!-- Form -->
  <form action="${pageContext.request.contextPath}/forgot-password" method="post">
    <label for="email" class="fp-label">Địa chỉ email</label>
    <div class="fp-input-wrap">
      <input type="email" id="email" name="email"
             placeholder="example@email.com"
             value="${param.email}"
             required autocomplete="email">
      <i class="fa-solid fa-envelope fp-field-icon"></i>
    </div>

    <button type="submit" class="fp-submit-btn">
      <i class="fa-solid fa-paper-plane"></i>
      Gửi OTP đặt lại mật khẩu
    </button>
  </form>

  <a href="${pageContext.request.contextPath}/login" class="fp-back-link">
    <i class="fa-solid fa-arrow-left"></i>
    Quay lại trang đăng nhập
  </a>

</div>

</body>
</html>
