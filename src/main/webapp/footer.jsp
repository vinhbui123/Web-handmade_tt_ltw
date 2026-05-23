<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/footer.css">
<footer>
    <div class="container-footer">
        <div class="footer-content">
            <h3 class="nameShop">HAND MADE CRAFT</h3>
            <p>Sản phẩm mang tính thủ công đem đến sự mộc mạc giản dị mang một chất riêng</p>

            <ul class="address-footer">
                <li><i class="fa-solid fa-location-dot"></i>Stown Thủ Đức, Bình Chiểu, Thủ Đức, TPHCM</li>
                <li><i class="fa-solid fa-phone"></i>0343 031 030</li>
                <li><i class="fa-solid fa-envelope"></i>handmadedcraft@gmail.com</li>
            </ul>
        </div>
        <div class="footer-content">

            <h3>THÔNG TIN</h3>
            <ul class="list">
                <li><a href="">Trang Chủ</a></li>
                <li><a href="about.html">Giới Thiệu</a></li>
                <li><a href="product.html">Sản Phẩm</a></li>
                <li><a href="contact.html">Liên Hệ</a></li>

            </ul>
        </div>
        <div class="footer-content">
            <h3>CHÍNH SÁCH</h3>
            <ul class="list">
                <li><a href="#">Chính sách mua hàng</a></li>
                <li><a href="#">Chính sách bảo mật</a></li>

                <li><a href="#">Phương thức thanh toán</a></li>
                <li><a href="#">Chính sách đổi trả</a></li>
            </ul>
        </div>
        <div class="footer-content">
            <h3>LIÊN HỆ</h3>
            <ul class="list-brands">
                <li><a href="https://www.facebook.com/"><i class="fa-brands fa-facebook"></i></a></li>
                <li><a href="https://www.instagram.com/"><i class="fa-brands fa-instagram"></i></a></li>
                <li><a href="https://www.tiktok.com/"><i class="fa-brands fa-tiktok"></i></a></li>
            </ul>
        </div>
    </div>
    <div class="footer-copyright">
        <div class="text-copyright">
            <p>Copy Right @ 2025 HAND MADE CRAFT Powered</p>
        </div>
    </div>
</footer>

<%-- ===== AI CHATBOX WIDGET ===== --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/chatbox.css">

<!-- Chat Toggle Button -->
<button class="chatbox-toggle" id="chatbox-toggle" aria-label="Mở chat AI">
    <span class="chat-icon-open"><i class="fas fa-comments"></i></span>
    <span class="chat-icon-close"><i class="fas fa-times"></i></span>
    <span class="chatbox-badge" id="chatbox-badge">1</span>
</button>

<!-- Chat Window -->
<div class="chatbox-window" id="chatbox-window">
    <div class="chatbox-header">
        <div class="chatbox-avatar">
            <i class="fas fa-robot"></i>
        </div>
        <div class="chatbox-header-info">
            <div class="chatbox-header-title">HandMade Craft AI</div>
            <div class="chatbox-header-status">
                <span class="status-dot"></span> Trực tuyến
            </div>
        </div>
        <button class="chatbox-header-close" id="chatbox-close" aria-label="Đóng chat">
            <i class="fas fa-minus"></i>
        </button>
    </div>
    <div class="chatbox-messages" id="chatbox-messages"></div>
    <div class="chatbox-input">
        <input type="text" id="chatbox-input" placeholder="Nhập tin nhắn..." autocomplete="off">
        <button class="chatbox-send-btn" id="chatbox-send" aria-label="Gửi">
            <i class="fas fa-paper-plane"></i>
        </button>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/chatbox.js" defer></script>