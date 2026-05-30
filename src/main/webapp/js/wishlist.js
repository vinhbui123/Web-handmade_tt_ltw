/**
 * wishlist.js — Xử lý AJAX cho chức năng Wishlist (Danh sách yêu thích)
 */
(function () {
    'use strict';

    // Lấy context path từ body hoặc header
    var contextPath = document.body.getAttribute('data-context-path')
        || (window.contextPath ? window.contextPath : '');

    /**
     * Toggle wishlist (thêm/xóa) cho một sản phẩm
     */
    window.toggleWishlist = function (productId, btnElement, event) {
        // Ngăn sự kiện click lan ra thẻ <a> cha
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }

        fetch(contextPath + '/api/wishlist', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({productId: productId})
        })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.redirect) {
                    window.location.href = contextPath + '/' + data.redirect;
                    return;
                }
                if (data.status) {
                    // Toggle icon heart
                    var icon = btnElement.querySelector('i');
                    if (data.action === 'added') {
                        icon.classList.remove('far');
                        icon.classList.add('fas');
                        btnElement.classList.add('wishlisted');
                    } else {
                        icon.classList.remove('fas');
                        icon.classList.add('far');
                        btnElement.classList.remove('wishlisted');
                    }

                    // Cập nhật badge count trên header
                    updateWishlistBadge(data.count);

                    // Hiển thị toast thông báo
                    showWishlistToast(data.message, data.action === 'added');
                } else {
                    alert(data.message || 'Có lỗi xảy ra!');
                }
            })
            .catch(function (err) {
                console.error('Lỗi wishlist:', err);
                alert('Có lỗi mạng xảy ra.');
            });
    };

    /**
     * Xóa sản phẩm khỏi wishlist (dùng trong trang wishlist.jsp)
     */
    window.removeFromWishlist = function (productId, btnElement, event) {
        if (event) {
            event.preventDefault();
            event.stopPropagation();
        }

        fetch(contextPath + '/api/wishlist', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({productId: productId, action: 'remove'})
        })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.status) {
                    // Xóa card sản phẩm khỏi DOM
                    var card = btnElement.closest('.wishlist-item');
                    if (card) {
                        card.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                        card.style.opacity = '0';
                        card.style.transform = 'scale(0.8)';
                        setTimeout(function () {
                            card.remove();
                            // Kiểm tra nếu hết sản phẩm -> hiện empty state
                            var grid = document.querySelector('.wishlist-grid');
                            if (grid && grid.children.length === 0) {
                                var emptyDiv = document.querySelector('.wishlist-empty');
                                if (emptyDiv) emptyDiv.style.display = 'block';
                                if (grid) grid.style.display = 'none';
                            }
                        }, 300);
                    }

                    updateWishlistBadge(data.count);
                    showWishlistToast(data.message, false);
                }
            })
            .catch(function (err) {
                console.error('Lỗi xóa wishlist:', err);
            });
    };

    /**
     * Cập nhật số lượng badge trên header
     */
    function updateWishlistBadge(count) {
        var badge = document.querySelector('.wishlist-count');
        if (badge) {
            if (count > 0) {
                badge.textContent = count;
                badge.style.display = 'inline-block';
            } else {
                badge.style.display = 'none';
            }
        }
    }

    /**
     * Hiển thị toast notification
     */
    function showWishlistToast(message, isAdded) {
        // Xóa toast cũ nếu có
        var old = document.querySelector('.wishlist-toast');
        if (old) old.remove();

        var toast = document.createElement('div');
        toast.className = 'wishlist-toast ' + (isAdded ? 'toast-added' : 'toast-removed');
        toast.innerHTML = '<i class="fas fa-heart" style="margin-right: 8px;"></i>' + message;
        document.body.appendChild(toast);

        // Trigger animation
        setTimeout(function () { toast.classList.add('show'); }, 10);

        // Tự ẩn sau 2.5 giây
        setTimeout(function () {
            toast.classList.remove('show');
            setTimeout(function () { toast.remove(); }, 300);
        }, 2500);
    }
})();
