// ===== Review Modal Logic =====

// Mở review modal cho 1 đơn hàng cụ thể
function rateOrder(orderId) {
    const modal = document.getElementById('reviewModal');
    const container = document.getElementById('review-items-container');
    const orderIdInput = document.getElementById('review-order-id');

    // Tìm order-block tương ứng theo data-order-id
    const orderBlock = document.querySelector(`.order-block[data-order-id="${orderId}"]`);

    if (!orderBlock) {
        alert('Không tìm thấy đơn hàng!');
        return;
    }

    renderReviewItems(orderBlock, container);

    orderIdInput.value = orderId;
    resetStarRating();
    document.getElementById('review-content').value = '';
    modal.style.display = 'block';
    document.body.style.overflow = 'hidden';
}

function renderReviewItems(orderBlock, container) {
    container.innerHTML = '';

    const items = orderBlock.querySelectorAll('.order-item');
    const purchaseItems = orderBlock.querySelectorAll('.purchase-item');

    items.forEach((item, index) => {
        const img = item.querySelector('.product-image');
        const name = item.querySelector('.order-details h4');
        const quantity = item.querySelector('.order-details p:nth-child(2)');

        // Lấy productId từ purchase-item hidden div
        let productId = '';
        if (purchaseItems[index]) {
            productId = purchaseItems[index].textContent.trim();
        }

        const reviewItem = document.createElement('div');
        reviewItem.className = 'review-product-item';
        reviewItem.setAttribute('data-product-id', productId);
        reviewItem.innerHTML = `
            <img src="${img ? img.src : ''}" alt="${name ? name.textContent : ''}">
            <div class="review-product-info">
                <h4>${name ? name.textContent : 'Sản phẩm'}</h4>
                <p>${quantity ? quantity.textContent : ''}</p>
            </div>
        `;

        // Click vào sản phẩm để chọn đánh giá cho sản phẩm đó
        reviewItem.addEventListener('click', function () {
            // Bỏ highlight tất cả
            container.querySelectorAll('.review-product-item').forEach(el =>
                el.style.borderColor = '#f0e6e0'
            );
            // Highlight sản phẩm được chọn
            this.style.borderColor = '#FF5722';
            document.getElementById('review-product-id').value = productId;
        });

        container.appendChild(reviewItem);
    });

    // Tự động chọn sản phẩm đầu tiên
    const firstItem = container.querySelector('.review-product-item');
    if (firstItem) {
        firstItem.click();
    }
}

// Đóng modal
function closeReviewModal() {
    const modal = document.getElementById('reviewModal');
    modal.style.display = 'none';
    document.body.style.overflow = '';
}

// Star rating logic
document.addEventListener('DOMContentLoaded', function () {
    const starContainer = document.getElementById('star-rating');
    if (!starContainer) return;

    const stars = starContainer.querySelectorAll('i');
    const ratingInput = document.getElementById('review-rating-value');

    stars.forEach(star => {
        // Hover effect
        star.addEventListener('mouseenter', function () {
            const val = parseInt(this.getAttribute('data-value'));
            stars.forEach(s => {
                const sVal = parseInt(s.getAttribute('data-value'));
                if (sVal <= val) {
                    s.classList.add('hovered');
                } else {
                    s.classList.remove('hovered');
                }
            });
        });

        // Click để chọn
        star.addEventListener('click', function () {
            const val = parseInt(this.getAttribute('data-value'));
            ratingInput.value = val;
            stars.forEach(s => {
                const sVal = parseInt(s.getAttribute('data-value'));
                if (sVal <= val) {
                    s.classList.remove('far');
                    s.classList.add('fas', 'selected');
                } else {
                    s.classList.remove('fas', 'selected');
                    s.classList.add('far');
                }
            });
        });
    });

    // Reset hover khi rời khỏi star container
    starContainer.addEventListener('mouseleave', function () {
        stars.forEach(s => s.classList.remove('hovered'));
    });
});

function resetStarRating() {
    const stars = document.querySelectorAll('#star-rating i');
    const ratingInput = document.getElementById('review-rating-value');
    ratingInput.value = 0;
    stars.forEach(s => {
        s.classList.remove('fas', 'selected', 'hovered');
        s.classList.add('far');
    });
}

// Gửi đánh giá
function submitReview() {
    const productId = document.getElementById('review-product-id').value;
    const rating = document.getElementById('review-rating-value').value;
    const content = document.getElementById('review-content').value.trim();

    if (!productId) {
        alert('Vui lòng chọn sản phẩm cần đánh giá!');
        return;
    }

    if (rating == 0) {
        alert('Vui lòng chọn số sao đánh giá!');
        return;
    }

    if (!content) {
        alert('Vui lòng nhập nội dung đánh giá!');
        return;
    }

    // Gửi dưới dạng form submit (POST) đến /comment
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = contextPath + '/comment';
    form.style.display = 'none';

    const fields = {
        productId: productId,
        rating: rating,
        content: content
    };

    for (const [key, value] of Object.entries(fields)) {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = key;
        input.value = value;
        form.appendChild(input);
    }

    document.body.appendChild(form);
    form.submit();
}

// Đóng modal khi click bên ngoài
window.addEventListener('click', function (event) {
    const modal = document.getElementById('reviewModal');
    if (event.target === modal) {
        closeReviewModal();
    }
});
