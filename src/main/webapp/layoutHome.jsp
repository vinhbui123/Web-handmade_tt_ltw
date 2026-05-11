<%@ page contentType="text/html;charset=UTF-8" language="java" import="vn.edu.hcmuaf.fit.Web_ban_hang.model.User" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HandMade</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/index.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/product.css">
    <script src="${pageContext.request.contextPath}/js/product.js" defer></script>
    <script src="${pageContext.request.contextPath}/js/cart.js" defer></script>

    <%-- CSS cho Welcome Popup --%>
    <style>
        /* ===== WELCOME POPUP OVERLAY ===== */
        #welcome-overlay {
            position: fixed;
            inset: 0;
            z-index: 9999;
            background: rgba(0, 0, 0, 0.55);
            backdrop-filter: blur(6px);
            display: flex;
            align-items: center;
            justify-content: center;
            animation: fadeInOverlay 0.4s ease;
        }
        @keyframes fadeInOverlay {
            from { opacity: 0; }
            to   { opacity: 1; }
        }
        #welcome-overlay.hiding {
            animation: fadeOutOverlay 0.5s ease forwards;
        }
        @keyframes fadeOutOverlay {
            from { opacity: 1; }
            to   { opacity: 0; }
        }

        .welcome-card {
            position: relative;
            background: linear-gradient(135deg, #ffffff 0%, #f0fdf4 100%);
            border-radius: 24px;
            padding: 48px 40px 36px;
            max-width: 480px;
            width: 90%;
            text-align: center;
            box-shadow: 0 30px 80px rgba(0,0,0,0.25), 0 0 0 1px rgba(255,255,255,0.6) inset;
            animation: popIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
        }
        @keyframes popIn {
            from { opacity: 0; transform: scale(0.7) translateY(30px); }
            to   { opacity: 1; transform: scale(1) translateY(0);      }
        }

        .welcome-icon-circle {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            background: linear-gradient(135deg, #22c55e, #16a34a);
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            box-shadow: 0 8px 24px rgba(34,197,94,0.4);
            animation: bounceIn 0.6s 0.2s both;
        }
        @keyframes bounceIn {
            0%   { transform: scale(0); }
            70%  { transform: scale(1.15); }
            100% { transform: scale(1); }
        }
        .welcome-icon-circle i {
            font-size: 36px;
            color: #fff;
        }

        .welcome-badge {
            display: inline-block;
            background: linear-gradient(135deg, #bbf7d0, #dcfce7);
            color: #15803d;
            font-size: 12px;
            font-weight: 700;
            letter-spacing: 1.5px;
            text-transform: uppercase;
            padding: 4px 14px;
            border-radius: 20px;
            margin-bottom: 12px;
        }

        .welcome-card h2 {
            font-size: 26px;
            font-weight: 800;
            color: #111827;
            margin: 0 0 10px;
            line-height: 1.3;
        }
        .welcome-card h2 span {
            background: linear-gradient(135deg, #16a34a, #15803d);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }

        .welcome-card p {
            font-size: 15px;
            color: #6b7280;
            line-height: 1.6;
            margin: 0 0 28px;
        }

        .welcome-btn-group {
            display: flex;
            gap: 12px;
            justify-content: center;
        }
        .welcome-btn-primary {
            flex: 1;
            padding: 13px 20px;
            background: linear-gradient(135deg, #22c55e, #16a34a);
            color: #fff;
            border: none;
            border-radius: 12px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            box-shadow: 0 4px 16px rgba(34,197,94,0.35);
            transition: transform 0.15s ease, box-shadow 0.15s ease;
        }
        .welcome-btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba(34,197,94,0.45);
        }
        .welcome-btn-secondary {
            padding: 13px 20px;
            background: #f3f4f6;
            color: #374151;
            border: none;
            border-radius: 12px;
            font-size: 15px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.15s ease;
        }
        .welcome-btn-secondary:hover { background: #e5e7eb; }

        /* Auto-close progress bar */
        .welcome-progress {
            position: absolute;
            bottom: 0;
            left: 0;
            height: 4px;
            border-radius: 0 0 24px 24px;
            background: linear-gradient(90deg, #22c55e, #86efac);
            width: 100%;
            animation: shrinkBar 6s linear forwards;
        }
        @keyframes shrinkBar {
            from { width: 100%; }
            to   { width: 0%;   }
        }

        /* Confetti particles */
        .confetti-particle {
            position: fixed;
            width: 10px;
            height: 10px;
            border-radius: 2px;
            opacity: 0;
            pointer-events: none;
            z-index: 10000;
            animation: confettiFall linear forwards;
        }
        @keyframes confettiFall {
            0%   { transform: translateY(-20px) rotate(0deg);    opacity: 1; }
            100% { transform: translateY(100vh) rotate(720deg); opacity: 0; }
        }
    </style>
</head>

<body data-context-path="${pageContext.request.contextPath}">

<%-- Giả định header.jsp đã được include ở đây --%>
<%@ include file="header.jsp" %>

<div class="mainBody">

    <%-- Khu vực Banner đã được tối ưu cho JS mới --%>
    <div id="content-banner" class="mainBanner">
        <div class="banner-show">
            <button class="prev" onclick="changeBanner(-1)">&#10094;</button>
            <div class="list-images">
                <img src="${pageContext.request.contextPath}/images/banner-index1.png" class="slide"
                     style="display: none;" alt="">
                <img src="${pageContext.request.contextPath}/images/banner-index2.png" class="slide"
                     style="display: none;" alt="">
                <img src="${pageContext.request.contextPath}/images/banner-index3.png" class="slide"
                     style="display: none;" alt="">
                <img src="${pageContext.request.contextPath}/images/banner-index4.png" class="slide"
                     style="display: none;" alt="">
                <img src="${pageContext.request.contextPath}/images/banner-index5.png" class="slide"
                     style="display: none;" alt="">
            </div>
            <button class="next" onclick="changeBanner(1)">&#10095;</button>
            <%-- Có thể thêm Dots/Pagination ở đây nếu cần --%>
        </div>
    </div>

    <%-- Khu vực Sản phẩm đã được cấu trúc lại --%>
    <div id="product-body-container">
        <h3>SẢN PHẨM CÓ LƯỢT XEM NHIỀU NHẤT</h3>
        <input type="hidden" id="config-items-per-page" value="${itemsPerPageConfig}">
        <div class="product-list">
            <c:forEach var="p" items="${productViewest}">
                <div class="product-box">
                    <div class="product-id hidden">${p.id}</div>
                    <a href="product-detail?id=${p.id}">
                        <c:if test="${p.discount != 0}">
                            <div class="discount">-${p.discount}%</div>
                        </c:if>
                        <div class="hinh-sp">
                            <img src="${p.img}" alt="${p.name}">
                        </div>
                        <p class="ten-sp">${p.name}</p>
                        <p class="gia-tien">
                            <c:choose>
                                <c:when test="${p.discount > 0}">
                                    <f:formatNumber
                                            value="${p.price - (p.price * p.discount / 100)}"
                                            pattern="#,##0đ"/>
                                    <span class="gia-cu"><f:formatNumber value="${p.price}" pattern="#,##0đ"/></span>
                                </c:when>
                                <c:otherwise>
                                    <f:formatNumber value="${p.price}"
                                                    pattern="#,##0đ"/>
                                </c:otherwise>
                            </c:choose>
                        </p>
                        <div class="add">
                            <p class="view">Lượt xem: ${p.view}</p>
                            <button type="button" class="add-to-cart"
                                    style="margin-left: auto">
                                <i class="fa-solid fa-cart-plus"></i>
                            </button>
                            <p class="hidden stock-quantity">Còn lại: ${p.stock}</p>
                        </div>
                    </a>
                </div>
            </c:forEach>
        </div>
        <div id="cart-popup" class="popup hidden">
            <div class="popup-content">
                <p>Sản phẩm đã được thêm vào giỏ hàng thành công!</p>
            </div>
        </div>
    </div>
</div>

<%@ include file="footer.jsp" %>

<%-- ===== WELCOME POPUP (chỉ hiển thị 1 lần sau khi đăng ký) ===== --%>
<%
    Boolean welcomeFlag = (Boolean) session.getAttribute("welcomeNewUser");
    if (Boolean.TRUE.equals(welcomeFlag)) {
        session.removeAttribute("welcomeNewUser"); // Chỉ hiện 1 lần
%>
<%
    User welcomeUser = (User) session.getAttribute("user");
    String displayName = (welcomeUser != null && welcomeUser.getFirstName() != null)
        ? welcomeUser.getFirstName() + " " + welcomeUser.getLastName()
        : "Bạn";
%>
<div id="welcome-overlay">
    <div class="welcome-card">
        <div class="welcome-icon-circle">
            <i class="fas fa-check"></i>
        </div>
        <div class="welcome-badge">🎉 Chào mừng thành viên mới</div>
        <h2>Xin chào, <span><%= displayName %>!</span></h2>
        <p>Tài khoản của bạn đã được tạo thành công.<br>
           Khám phá ngay những sản phẩm thủ công tinh tế của chúng tôi!</p>
        <div class="welcome-btn-group">
            <button class="welcome-btn-primary" onclick="closeWelcome()">
                <i class="fas fa-store" style="margin-right:8px"></i>Khám phá ngay
            </button>
            <button class="welcome-btn-secondary" onclick="closeWelcome()">
                Đóng
            </button>
        </div>
        <div class="welcome-progress" id="welcome-progress"></div>
    </div>
</div>

<script>
    (function() {
        // Spawn confetti particles
        var colors = ['#22c55e','#86efac','#fbbf24','#f472b6','#60a5fa','#a78bfa'];
        for (var i = 0; i < 60; i++) {
            (function(idx) {
                setTimeout(function() {
                    var p = document.createElement('div');
                    p.className = 'confetti-particle';
                    p.style.left = Math.random() * 100 + 'vw';
                    p.style.top = '-20px';
                    p.style.background = colors[idx % colors.length];
                    var size = (Math.random() * 8 + 6) + 'px';
                    p.style.width  = size;
                    p.style.height = size;
                    var dur = (Math.random() * 2 + 2) + 's';
                    p.style.animationDuration = dur;
                    p.style.animationDelay    = '0s';
                    document.body.appendChild(p);
                    setTimeout(function() { p.remove(); }, parseFloat(dur) * 1000 + 200);
                }, idx * 60);
            })(i);
        }

        // Auto close after 6 seconds
        var autoTimer = setTimeout(function() { closeWelcome(); }, 6000);

        window.closeWelcome = function() {
            clearTimeout(autoTimer);
            var overlay = document.getElementById('welcome-overlay');
            if (!overlay) return;
            overlay.classList.add('hiding');
            setTimeout(function() { overlay.remove(); }, 500);
        };
    })();
</script>
<% } %>

<script>

    // --- BANNER LOGIC (Đã thêm Auto-Play và Stop-on-Hover) ---
    let currentIndex = 0;
    let slideInterval;
    const slides = document.querySelectorAll('.slide');
    const bannerContainer = document.querySelector('.mainBanner');

    function showSlide(index) {
        if (slides.length === 0) return;

        currentIndex = (index + slides.length) % slides.length;

        slides.forEach(slide => slide.style.display = 'none');
        if (slides[currentIndex]) {
            slides[currentIndex].style.display = 'block';
        }
    }

    function changeBanner(step) {
        stopAutoPlay(); // Dừng nếu người dùng tương tác
        showSlide(currentIndex + step);
        startAutoPlay(); // Chạy lại sau khi chuyển
    }

    //tự di chuyển banner trong vòng 3 giây khi người dùng không di chuột vào
    function startAutoPlay() {
        if (slideInterval) clearInterval(slideInterval);
        slideInterval = setInterval(() => {
            showSlide(currentIndex + 1);
        }, 4000); // Tự động chuyển sau 3 giây
    }

    function stopAutoPlay() {
        clearInterval(slideInterval);
    }

    document.addEventListener("DOMContentLoaded", function () {
        if (slides.length > 0) {
            showSlide(0); // Hiển thị slide đầu tiên
            startAutoPlay();

            // Dừng/Chạy khi hover
            if (bannerContainer) {
                bannerContainer.addEventListener('mouseenter', stopAutoPlay);
                bannerContainer.addEventListener('mouseleave', startAutoPlay);
            }
        }
    });

    // --- PRODUCT ADD TO CART LOGIC (AJAX) ---
    function addToCart(button, productId) {
        // Tắt nút và hiển thị loading/icon
        const originalIcon = button.innerHTML;
        button.disabled = true;
        button.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>'; // Icon loading

        fetch('${pageContext.request.contextPath}/add-cart-ajax', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: 'id=' + encodeURIComponent(productId)
        })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Thành công: Hiển thị icon checkmark
                    button.innerHTML = '<i class="fa-solid fa-check"></i>';

                    // Trả về icon ban đầu sau 1 giây
                    setTimeout(() => {
                        button.disabled = false;
                        button.innerHTML = originalIcon;
                    }, 1000);
                } else {
                    alert('Lỗi: ' + data.message);
                    button.disabled = false;
                    button.innerHTML = originalIcon;
                }
            })
            .catch(error => {
                console.error('Lỗi AJAX:', error);
                alert('Có lỗi mạng xảy ra.');
                button.disabled = false;
                button.innerHTML = originalIcon;
            });
    }

</script>
</body>
</html>