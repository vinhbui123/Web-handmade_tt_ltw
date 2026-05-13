<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Nhập Mã OTP - Handmade Shop</title>
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
      max-width: 460px;
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

    /* OTP input row */
    .otp-row {
      display: flex;
      justify-content: center;
      gap: 10px;
      margin-bottom: 28px;
    }

    .otp-row input[type="text"] {
      width: 52px;
      height: 60px;
      text-align: center;
      font-size: 26px;
      font-weight: 700;
      font-family: 'Inter', monospace;
      background: rgba(255,255,255,0.07);
      border: 1px solid rgba(255,255,255,0.18);
      border-radius: 10px;
      color: #fff;
      outline: none;
      transition: border-color 0.25s, box-shadow 0.25s, background 0.25s;
      caret-color: #e2b96f;
    }

    .otp-row input[type="text"]:focus {
      border-color: #e2b96f;
      background: rgba(255,255,255,0.12);
      box-shadow: 0 0 0 3px rgba(226,185,111,0.2);
    }

    /* Hidden full OTP input submitted in form */
    #otpHidden { display: none; }

    .fp-timer {
      text-align: center;
      font-size: 13px;
      color: rgba(255,255,255,0.45);
      margin-bottom: 24px;
    }
    .fp-timer span { color: #e2b96f; font-weight: 600; }

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

    .fp-resend {
      text-align: center;
      margin-top: 18px;
      font-size: 14px;
      color: rgba(255,255,255,0.45);
    }
    .fp-resend a {
      color: #e2b96f;
      text-decoration: none;
      font-weight: 500;
      transition: opacity 0.2s;
    }
    .fp-resend a:hover { opacity: 0.8; }

    .fp-back-link {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      margin-top: 20px;
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
    <i class="fa-solid fa-envelope-open-text"></i>
  </div>

  <h1 class="fp-title">Nhập mã OTP</h1>
  <p class="fp-subtitle">
    Chúng tôi đã gửi mã OTP 6 chữ số đến email của bạn.<br>
    Mã có hiệu lực trong <strong style="color:#e2b96f;">10 phút</strong>.
  </p>

  <!-- Thông báo lỗi -->
  <c:if test="${not empty error}">
    <div class="alert alert-danger">
      <i class="fa-solid fa-circle-exclamation"></i>
      <span>${error}</span>
    </div>
  </c:if>

  <!-- Form OTP -->
  <form id="otpForm" action="${pageContext.request.contextPath}/forgot-otp" method="post"
        onsubmit="combineOtp()">

    <!-- 6 ô nhập OTP (UX tốt hơn) -->
    <div class="otp-row">
      <input type="text" maxlength="1" class="otp-digit" inputmode="numeric" pattern="[0-9]" autocomplete="off">
      <input type="text" maxlength="1" class="otp-digit" inputmode="numeric" pattern="[0-9]" autocomplete="off">
      <input type="text" maxlength="1" class="otp-digit" inputmode="numeric" pattern="[0-9]" autocomplete="off">
      <input type="text" maxlength="1" class="otp-digit" inputmode="numeric" pattern="[0-9]" autocomplete="off">
      <input type="text" maxlength="1" class="otp-digit" inputmode="numeric" pattern="[0-9]" autocomplete="off">
      <input type="text" maxlength="1" class="otp-digit" inputmode="numeric" pattern="[0-9]" autocomplete="off">
    </div>

    <!-- Hidden input gộp 6 chữ số để submit lên server -->
    <input type="hidden" name="otp" id="otpHidden">

    <!-- Đếm ngược 10 phút -->
    <p class="fp-timer">Mã hết hạn sau: <span id="countdown">10:00</span></p>

    <button type="submit" class="fp-submit-btn">
      <i class="fa-solid fa-check-circle"></i>
      Xác nhận OTP
    </button>
  </form>

  <p class="fp-resend">
    Chưa nhận được mã?
    <a href="${pageContext.request.contextPath}/forgot-password">Gửi lại</a>
  </p>

  <a href="${pageContext.request.contextPath}/login" class="fp-back-link">
    <i class="fa-solid fa-arrow-left"></i>
    Quay lại trang đăng nhập
  </a>

</div>

<script>
  // ── Auto-advance khi nhập mỗi ô ──────────────────────────────────────────
  const digits = document.querySelectorAll('.otp-digit');

  digits.forEach((input, idx) => {
    input.addEventListener('input', () => {
      input.value = input.value.replace(/\D/g, ''); // chỉ số
      if (input.value && idx < digits.length - 1) {
        digits[idx + 1].focus();
      }
    });
    input.addEventListener('keydown', e => {
      if (e.key === 'Backspace' && !input.value && idx > 0) {
        digits[idx - 1].focus();
      }
    });
    // Paste toàn bộ OTP vào ô đầu tiên
    input.addEventListener('paste', e => {
      e.preventDefault();
      const pasted = (e.clipboardData || window.clipboardData).getData('text').replace(/\D/g, '');
      [...pasted].slice(0, 6).forEach((ch, i) => {
        if (digits[i]) digits[i].value = ch;
      });
      const next = Math.min(pasted.length, 5);
      digits[next].focus();
    });
  });

  // Focus ô đầu tiên khi load
  digits[0].focus();

  // ── Gộp 6 ô thành 1 hidden input trước khi submit ────────────────────────
  function combineOtp() {
    document.getElementById('otpHidden').value =
      [...digits].map(d => d.value).join('');
  }

  // ── Đếm ngược 10 phút ────────────────────────────────────────────────────
  let remaining = 10 * 60; // giây
  const countdownEl = document.getElementById('countdown');

  const timer = setInterval(() => {
    remaining--;
    if (remaining <= 0) {
      clearInterval(timer);
      countdownEl.textContent = '00:00';
      countdownEl.style.color = '#ef4444';
      return;
    }
    const m = String(Math.floor(remaining / 60)).padStart(2, '0');
    const s = String(remaining % 60).padStart(2, '0');
    countdownEl.textContent = m + ':' + s;
    if (remaining <= 60) countdownEl.style.color = '#ef4444';
  }, 1000);
</script>

</body>
</html>
