const formatCurrency = (amount) => new Intl.NumberFormat('vi-VN').format(amount) + 'đ';

function openPurchaseDetailPopup(orderId) {
    const modal = document.getElementById("purchaseModal");
    modal.style.display = "block";

    document.getElementById('modal-loading').style.display = 'block';
    document.getElementById('modal-body').style.display = 'none';
    document.getElementById('modal-footer').style.display = 'none';

    document.getElementById('modal-order-id').innerText = orderId;

    fetch(contextPath + '/api/order-detail?orderId=' + orderId)
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                renderOrderDetails(data.order);
            } else {
                alert("Không tải được chi tiết đơn hàng: " + (data.message || "Lỗi không xác định"));
                closePurchaseDetailPopup();
            }
        })
        .catch(err => {
            console.error("Lỗi AJAX:", err);
            alert("Hệ thống đang bận, vui lòng thử lại sau.");
            closePurchaseDetailPopup();
        });
}

function closePurchaseDetailPopup() {
    document.getElementById("purchaseModal").style.display = "none";
}

window.onclick = function (event) {
    const modal = document.getElementById("purchaseModal");
    if (event.target === modal) {
        closePurchaseDetailPopup();
    }
};

function renderOrderDetails(orderData) {
    renderTimeline(orderData.status);
    document.getElementById('modal-payment-method').innerText = orderData.paymentMethod || 'COD (Tiền mặt)';
    document.getElementById('modal-order-status').innerHTML = getStatusBadge(orderData.status);
    const productListDiv = document.getElementById('modal-product-list');
    productListDiv.innerHTML = '';

    let subTotal = 0;
    let totalDiscount = 0;
    let shippingFee = orderData.shippingFee || 0;

    orderData.details.forEach(item => {
        subTotal += item.price * item.quantity;
        totalDiscount += item.discountAmount;

        const html = `
            <div style="display: flex; gap: 15px; margin-bottom: 15px; align-items: center; border-bottom: 1px solid #f9f9f9; padding-bottom: 10px;">
                <img src="${contextPath}/${item.productImg}" alt="${item.productName}" style="width: 60px; height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #ddd;">
                <div style="flex-grow: 1;">
                    <div style="font-weight: 500; font-size: 13px; margin-bottom: 3px; color: #333;">${item.productName}</div>
                    <div style="color: #757575; font-size: 12px;">Đơn giá: ${formatCurrency(item.price)}</div>
                    <div style="color: #757575; font-size: 12px;">Số lượng: x${item.quantity}</div>
                </div>
                <div style="text-align: right; min-width: 80px;">
                    <div style="font-weight: bold; color: #ee4d2d; font-size: 14px;">${formatCurrency(item.price * item.quantity)}</div>
                </div>
            </div>
        `;
        productListDiv.innerHTML += html;
    });

    const finalTotal = subTotal + shippingFee - totalDiscount;
    document.getElementById('modal-subtotal').innerText = formatCurrency(subTotal);
    document.getElementById('modal-shipping').innerText = formatCurrency(shippingFee);
    document.getElementById('modal-discount').innerText = '-' + formatCurrency(totalDiscount);
    document.getElementById('modal-total').innerText = formatCurrency(finalTotal);
    document.getElementById('modal-loading').style.display = 'none';
    document.getElementById('modal-body').style.display = 'block';
    document.getElementById('modal-footer').style.display = 'block';
}

function renderTimeline(status) {
    const container = document.getElementById('modal-timeline-container');

    // Đơn bị hủy
    if (status === 4) {
        container.innerHTML = `
            <div class="order-timeline">
                <div class="timeline-step active"><div class="timeline-icon"><i class="fa-solid fa-file-invoice"></i></div><div class="timeline-text">Đã Đặt Hàng</div></div>
                <div class="timeline-step cancelled"><div class="timeline-icon"><i class="fa-solid fa-circle-xmark"></i></div><div class="timeline-text">Đã Hủy</div></div>
            </div>
        `;
        return;
    }

    //5 (Yêu cầu), 6 (Đã hoàn tiền), 7 (Từ chối)
    if (status >= 5) {
        let step3Class = '';
        let step3Icon = 'fa-money-bill-transfer';
        let step3Text = 'Đang Xử Lý';

        if (status === 6) {
            step3Class = 'active';
            step3Icon = 'fa-check';
            step3Text = 'Đã Hoàn Tiền';
        } else if (status === 7) {
            step3Class = 'cancelled';
            step3Icon = 'fa-xmark';
            step3Text = 'Từ Chối';
        }

        container.innerHTML = `
            <div class="order-timeline">
                <div class="timeline-step active"><div class="timeline-icon"><i class="fa-solid fa-box-open"></i></div><div class="timeline-text">Đã Nhận Hàng</div></div>
                <div class="timeline-step active"><div class="timeline-icon"><i class="fa-solid fa-rotate-left"></i></div><div class="timeline-text">Yêu Cầu Hoàn Trả</div></div>
                <div class="timeline-step ${step3Class}"><div class="timeline-icon"><i class="fa-solid ${step3Icon}"></i></div><div class="timeline-text">${step3Text}</div></div>
            </div>
        `;
        return;
    }

    // 0 (Chờ), 1 (Xác nhận), 2 (Đang giao), 3 (Hoàn thành)
    const step1 = status >= 0 ? 'active' : '';
    const step2 = status >= 1 ? 'active' : '';
    const step3 = status >= 2 ? 'active' : '';
    const step4 = status === 3 ? 'active' : '';

    container.innerHTML = `
        <div class="order-timeline">
            <div class="timeline-step ${step1}"><div class="timeline-icon"><i class="fa-solid fa-file-invoice"></i></div><div class="timeline-text">Đã Đặt</div></div>
            <div class="timeline-step ${step2}"><div class="timeline-icon"><i class="fa-solid fa-money-check-dollar"></i></div><div class="timeline-text">Đã Xác Nhận</div></div>
            <div class="timeline-step ${step3}"><div class="timeline-icon"><i class="fa-solid fa-truck-fast"></i></div><div class="timeline-text">Đang Giao</div></div>
            <div class="timeline-step ${step4}"><div class="timeline-icon"><i class="fa-solid fa-star"></i></div><div class="timeline-text">Thành Công</div></div>
        </div>
    `;
}

function getStatusBadge(status) {
    switch(status) {
        case 0: return '<span style="color: #f39c12; font-weight: bold;">Chờ xác nhận</span>';
        case 1: return '<span style="color: #3498db; font-weight: bold;">Đã xác nhận</span>';
        case 2: return '<span style="color: #2980b9; font-weight: bold;">Đang giao hàng</span>';
        case 3: return '<span style="color: #2ecc71; font-weight: bold;">Đã hoàn thành</span>';
        case 4: return '<span style="color: #e74c3c; font-weight: bold;">Đã huỷ</span>';
        case 5: return '<span style="color: #e67e22; font-weight: bold;">Yêu cầu hoàn trả</span>';
        case 6: return '<span style="color: #8e44ad; font-weight: bold;">Đã hoàn tiền</span>';
        case 7: return '<span style="color: #7f8c8d; font-weight: bold;">Từ chối hoàn trả</span>';
        default: return 'Không xác định';
    }
}