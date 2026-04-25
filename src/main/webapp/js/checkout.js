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
    let freeShipping = 0;
    let shippingFeeElem = document.querySelector('.shipping-fee');
    if (shippingFeeElem) {
        freeShipping = parseInt(shippingFeeElem.textContent.replace(/[^\d]/g, '')) || 0;
    }

    let details = [];
    document.querySelectorAll('.product-item').forEach((item) => {
        let productId = item.querySelector('.product-id').innerText;
        let price = item.querySelector('.price-info').innerText.replace("₫", "").trim();
        let quantity = item.querySelector('.quantity-info').innerText;
        details.push({
            productId: parseInt(productId),
            price: parseInt(price),
            quantity: parseInt(quantity)
        });
    });

    let orderData = {
        userId: parseInt(userId),
        status: status,
        freeShipping: parseInt(freeShipping),
        paymentTypeId: paymentMethod === 'cod' ? 1 : 2,
        details: details
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
                alert("Đặt hàng thành công!");
                window.location.href = `${contextPath}/purchase`;
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

