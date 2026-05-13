<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Đặt lại Mật Khẩu - Handmade Shop</title>
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

    .rp-card {
      background: rgba(255,255,255,0.04);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border: 1px solid rgba(255,255,255,0.12);
      border-radius: 20px;
      padding: 48px 44px 40px;
      width: 100%;
      max-width: 460px;
      box-shadow: 0 24px 60px rgba(0,0,0,0.5);
      animation: slideUp 0.5s ease;
    }

    @keyframes slideUp {
      from { opacity:0; transform:translateY(30px); }
      to   { opacity:1; transform:translateY(0); }
    }

    .rp-icon-wrap {
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
    .rp-icon-wrap i { font-size: 30px; color: #1a1a2e; }

    .rp-title {
      text-align: center;
      color: #ffffff;
      font-size: 24px;
      font-weight: 700;
      margin: 0 0 10px;
    }

    .rp-subtitle {
      text-align: center;
      color: rgba(255,255,255,0.55);
      font-size: 14px;
      line-height: 1.65;
      margin: 0 0 28px;
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
    }
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

    /* Inline error */
    #client-error {
      color: #fca5a5;
      font-size: 13px;
      margin: -12px 0 16px;
      min-height: 18px;
      display: block;
    }

    /* Label */
    .rp-label {
      display: block;
      color: rgba(255,255,255,0.75);
      font-size: 13px;
      font-weight: 500;
      margin-bottom: 8px;
      letter-spacing: 0.3px;
    }

    /* Input group */
    .rp-input-wrap {
      position: relative;
      margin-bottom: 20px;
    }

    .rp-input-wrap input[type="password"],
    .rp-input-wrap input[type="text"] {
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
    .rp-input-wrap input::placeholder { color: rgba(255,255,255,0.3); }
    .rp-input-wrap input:focus {
      border-color: #e2b96f;
      background: rgba(255,255,255,0.10);
      box-shadow: 0 0 0 3px rgba(226,185,111,0.18);
    }

    .toggle-eye {
      position: absolute;
      right: 14px;
      top: 50%;
      transform: translateY(-50%);
      color: rgba(255,255,255,0.4);
      font-size: 16px;
      cursor: pointer;
      transition: color 0.2s;
      background: none;
      border: none;
      padding: 0;
    }
    .toggle-eye:hover { color: #e2b96f; }

    /* Password strength bar */
    .strength-bar-wrap {
      height: 4px;
      background: rgba(255,255,255,0.1);
      border-radius: 4px;
      margin: -12px 0 16px;
      overflow: hidden;
    }
    .strength-bar {
      height: 100%;
      border-radius: 4px;
      width: 0%;
      transition: width 0.3s, background 0.3s;
    }

    /* Submit */
    .rp-submit-btn {
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
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      margin-top: 8px;
    }
    .rp-submit-btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(226,185,111,0.45);
      opacity: 0.95;
    }
    .rp-submit-btn:active { transform: translateY(0); }

    .rp-back-link {
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
    .rp-back-link:hover { color: #e2b96f; }

    /* Disabled card overlay when error */
    .form-disabled { opacity: 0.4; pointer-events: none; }
  </style>
</head>
<body>

<div class="rp-card">

  <div class="rp-icon-wrap">
    <i class="fa-solid fa-key"></i>
  </div>

  <h1 class="rp-title">Tạo mật khẩu mới</h1>
  <p class="rp-subtitle">Nhập mật khẩu mới cho tài khoản của bạn.</p>

  <!-- Server alerts -->
  <c:if test="${not empty error}">
    <div class="alert alert-danger">
      <i class="fa-solid fa-circle-exclamation"></i>
      <span>${error}</span>
    </div>
  </c:if>

  <!-- Form đặt lại mật khẩu (được bảo vệ bởi session otpVerifiedEmail) -->
  <form action="${pageContext.request.contextPath}/reset-password" method="post"
        id="resetForm" onsubmit="return validateForm()">

    <span id="client-error"></span>

    <!-- Mật khẩu mới -->
    <label for="newPassword" class="rp-label">Mật khẩu mới</label>
    <div class="rp-input-wrap">
      <input type="password" id="newPassword" name="newPassword"
             placeholder="Ít nhất 8 ký tự" required
             oninput="updateStrength(this.value)">
      <button type="button" class="toggle-eye" onclick="togglePwd('newPassword', this)" tabindex="-1">
        <i class="fas fa-eye-slash"></i>
      </button>
    </div>
    <div class="strength-bar-wrap">
      <div class="strength-bar" id="strengthBar"></div>
    </div>

    <!-- Xác nhận mật khẩu -->
    <label for="confirmPassword" class="rp-label">Xác nhận mật khẩu</label>
    <div class="rp-input-wrap">
      <input type="password" id="confirmPassword" name="confirmPassword"
             placeholder="Nhập lại mật khẩu mới" required>
      <button type="button" class="toggle-eye" onclick="togglePwd('confirmPassword', this)" tabindex="-1">
        <i class="fas fa-eye-slash"></i>
      </button>
    </div>

    <button type="submit" class="rp-submit-btn">
      <i class="fa-solid fa-shield-halved"></i>
      Xác nhận đặt lại mật khẩu
    </button>
  </form>

  <a href="${pageContext.request.contextPath}/login" class="rp-back-link">
    <i class="fa-solid fa-arrow-left"></i>
    Quay lại trang đăng nhập
  </a>

</div>

<script>
  function togglePwd(inputId, btn) {
    const input = document.getElementById(inputId);
    const icon  = btn.querySelector('i');
    if (input.type === 'password') {
      input.type = 'text';
      icon.classList.replace('fa-eye-slash', 'fa-eye');
    } else {
      input.type = 'password';
      icon.classList.replace('fa-eye', 'fa-eye-slash');
    }
  }

  function updateStrength(val) {
    const bar = document.getElementById('strengthBar');
    if (!bar) return;
    let score = 0;
    if (val.length >= 8)  score++;
    if (/[A-Z]/.test(val)) score++;
    if (/[0-9]/.test(val)) score++;
    if (/[^A-Za-z0-9]/.test(val)) score++;

    const pct    = ['0%','30%','55%','80%','100%'][score];
    const colors = ['transparent','#ef4444','#f97316','#eab308','#22c55e'];
    bar.style.width      = pct;
    bar.style.background = colors[score];
  }

  function validateForm() {
    const errEl   = document.getElementById('client-error');
    const pwd     = document.getElementById('newPassword').value;
    const confirm = document.getElementById('confirmPassword').value;

    if (pwd.length < 8) {
      errEl.textContent = '⚠ Mật khẩu phải có ít nhất 8 ký tự.';
      return false;
    }
    if (pwd !== confirm) {
      errEl.textContent = '⚠ Mật khẩu xác nhận không khớp.';
      return false;
    }
    errEl.textContent = '';
    return true;
  }
</script>

</body>
</html>
