function showPayment(method) {
    document.querySelectorAll('.payment-content').forEach((content) => {
        content.classList.add('hidden');
    });

    document.getElementById(method).classList.remove('hidden');

    document.querySelectorAll('.tab').forEach((tab) => {
        tab.classList.remove('active');
    });
    document.querySelector(`.tab[onclick="showPayment('${method}')"]`).classList.add('active');
}

function parseNumber(value) {
    const digits = String(value || '').replace(/[^\d]/g, '');
    return parseInt(digits || '0', 10);
}

function placeOrder() {
    // --- BƯỚC KIỂM TRA ĐỊA CHỈ TRƯỚC KHI ĐẶT HÀNG ---
    const addressIdCheck = document.getElementById('address-id-check');
    if (!addressIdCheck || addressIdCheck.value === "") {
        alert("⚠️ Vui lòng thêm địa chỉ nhận hàng trước khi đặt hàng!");
        if (typeof openAddressPopup === 'function') openAddressPopup();
        return;
    }

    let activePayment = document.querySelector('.payment-content:not(.hidden)');
    let paymentMethod = activePayment ? activePayment.id : "unknown";

    const status = 0;
    let shippingFee = 0;
    let shippingFeeElem = document.querySelector('.shipping-fee');
    if (shippingFeeElem) {
        shippingFee = parseInt(shippingFeeElem.textContent.replace(/[^\d]/g, '')) || 0;
    }

    let details = [];
    document.querySelectorAll('.product-item').forEach((item) => {
        const productId = parseNumber(item.dataset.productId || item.querySelector('.product-id')?.innerText);
        let price = parseNumber(item.querySelector('.current-price').innerText.replace("₫", "").trim());
        const quantity = parseNumber(item.dataset.quantity || item.querySelector('.quantity-info')?.innerText);

        details.push({
            productId: productId,
            price: price,
            quantity: quantity
        });
    });

    // Kiểm tra mode Mua Ngay
    const isBuyNowElem = document.getElementById('isBuyNow');
    const isBuyNow = isBuyNowElem && isBuyNowElem.value === 'true';

    let orderData = {
        userId: parseInt(userId),
        status: status,
        shippingFee: parseInt(shippingFee),
        paymentTypeId: paymentMethod === 'cod' ? 1 : 2,
        details: details,
        buyNow: isBuyNow
    };

    fetch(`${contextPath}/checkout`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(orderData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                if (data.redirectUrl) {
                    window.location.href = data.redirectUrl;
                } else {
                    alert("Đặt hàng thành công!");
                    window.location.href = `${contextPath}/purchase`;
                }
            } else {
                alert("Đặt hàng thất bại, vui lòng thử lại!" + data.message);
            }
        })
        .catch(error => console.error('Lỗi:', error));
}

function applyCoupon(code) {
    if (!code) {
        alert("Vui lòng nhập mã giảm giá!");
        return;
    }

    var formData = new URLSearchParams();
    formData.append("code", code);

    fetch(contextPath + "/apply-coupon", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: formData
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert(data.message);

                const discountAmount = data.discountAmount || 0;
                const totalText = document.querySelector('#total-order-value').textContent.replace(/[^\d]/g, '');
                const shippingText = document.querySelector('.shipping-fee').textContent.replace(/[^\d]/g, '');

                const total = parseInt(totalText || '0');
                const shipping = parseInt(shippingText || '0');
                const newTotal = total + shipping - discountAmount;

                const formatter = new Intl.NumberFormat('vi-VN');
                const formattedNewTotal = formatter.format(newTotal);
                const formattedDiscount = formatter.format(discountAmount);

                document.querySelector('.order-summary strong span').textContent = `${formattedNewTotal} VND`;

                document.getElementById('applied-coupon-info').innerHTML = `
                <div style="display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
                    <div style="text-align: right;">
                        <p style="margin: 0; color: #e74c3c;">Mã đã áp dụng: <strong>${code}</strong></p>
                        <p style="margin: 0; color: #e74c3c;">Đã giảm: <strong>-${formattedDiscount} VND</strong></p>
                    </div>
                    <a href="javascript:void(0)" onclick="removeCoupon()" style="color: #e74c3c; font-size: 1.2rem; margin-left: 5px;" title="Hủy mã giảm giá"><i class="fas fa-times"></i></a>
                </div>
            `;

                document.getElementById('selectedCouponCode').value = code;
                const couponInput = document.getElementById('coupon-code-checkout');
                if (couponInput) couponInput.value = '';

            } else {
                alert(data.message);
            }
        });
}

function removeCoupon() {
    fetch(contextPath + "/remove-coupon", {
        method: "POST"
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert(data.message);

                const totalText = document.querySelector('#total-order-value').textContent.replace(/[^\d]/g, '');
                const shippingText = document.querySelector('.shipping-fee').textContent.replace(/[^\d]/g, '');

                const total = parseInt(totalText || '0');
                const shipping = parseInt(shippingText || '0');
                const newTotal = total + shipping; // khong tru discountAmount vi da huy

                const formatter = new Intl.NumberFormat('vi-VN');
                const formattedNewTotal = formatter.format(newTotal);

                document.querySelector('.order-summary strong span').textContent = `${formattedNewTotal} VND`;

                document.getElementById('applied-coupon-info').innerHTML = '';
                document.getElementById('selectedCouponCode').value = '';
                const couponInput = document.getElementById('coupon-code-checkout');
                if (couponInput) couponInput.value = '';

            } else {
                alert(data.message);
            }
        })
        .catch(error => console.error('Lỗi:', error));
}

document.addEventListener("DOMContentLoaded", function() {
    const addressIdCheck = document.getElementById('address-id-check');
    // Nếu đã có địa chỉ thì tự động gọi API tính phí ship
    if (addressIdCheck && addressIdCheck.value !== "") {
        loadShippingMethods();
    }
});

function loadShippingMethods() {
    console.log("Đang tải phí vận chuyển từ GHN...");

    let products = Array.from(document.querySelectorAll('.product-item')).map(item => {
        return {
            id: parseNumber(item.dataset.productId),
            quantity: parseNumber(item.dataset.quantity)
        };
    }).filter(p => !isNaN(p.id) && p.id > 0);

    let totalOrderValue = 0;
    const totalOrderValueElem = document.getElementById('total-order-value');
    if (totalOrderValueElem) {
        totalOrderValue = parseNumber(totalOrderValueElem.textContent);
    }

    fetch(`${contextPath}/shipfee`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ products: products, totalOrderValue: totalOrderValue })
    })
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                console.error("Lỗi GHN:", data.error);
                updateOrderSummaryShipping(0);
                return;
            }
            updateOrderSummaryShipping(parseInt(data.total));
        })
        .catch(error => {
            console.error('Error:', error);
            updateOrderSummaryShipping(0);
        });
}

function updateOrderSummaryShipping(shippingFee) {
    // 1. Cập nhật chữ hiển thị phí ship
    const shippingFeeElem = document.querySelector('.shipping-fee');
    if (shippingFeeElem) {
        shippingFeeElem.textContent = new Intl.NumberFormat('vi-VN').format(shippingFee) + " đ";
    }

    // 2. Lấy lại tổng tiền hàng
    let totalOrderValue = 0;
    const totalOrderValueElem = document.getElementById('total-order-value');
    if (totalOrderValueElem) {
        totalOrderValue = parseNumber(totalOrderValueElem.textContent);
    }

    // 3. Lấy số tiền được giảm giá (nếu có)
    let discountAmount = 0;
    const discountElem = document.querySelector('#applied-coupon-info strong:last-child');
    if (discountElem && discountElem.textContent.includes('-')) {
        discountAmount = parseNumber(discountElem.textContent);
    }

    // 4. Tính toán số cuối cùng và hiển thị
    const finalTotal = totalOrderValue + shippingFee - discountAmount;
    const finalTotalElem = document.querySelector('.order-summary strong span');
    if (finalTotalElem) {
        finalTotalElem.textContent = new Intl.NumberFormat('vi-VN').format(finalTotal) + " đ";
    }
}