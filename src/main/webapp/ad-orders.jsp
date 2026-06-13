<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Quản Lý Đơn Hàng - Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <script src="${pageContext.request.contextPath}/js/admin.js"></script>
    <style>

        .modal-overlay { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.6); }
        .modal-box { background-color: #fff; margin: 5% auto; padding: 20px 30px; border-radius: 8px; width: 90%; max-width: 500px; box-shadow: 0 5px 15px rgba(0,0,0,0.3); position: relative; }
        .close-btn { position: absolute; right: 20px; top: 15px; font-size: 24px; cursor: pointer; color: #555; }
        .close-btn:hover { color: red; }

        .pagination-container { display: flex; justify-content: center; align-items: center; margin-top: 30px; margin-bottom: 20px; flex-direction: column; gap: 15px; }
        .page-info { font-weight: bold; color: #555; font-size: 14px; }
        .pagination { display: flex; list-style: none; padding: 0; gap: 8px; margin: 0; }
        .pagination li a, .pagination li span { display: flex; align-items: center; justify-content: center; width: 35px; height: 35px; border: 1px solid #dee2e6; background: #fff; cursor: pointer; text-decoration: none; color: #333; border-radius: 4px; font-weight: 500; transition: all 0.2s ease-in-out; }
        .pagination li a:hover { background-color: #e9ecef; }
        .pagination li.active a { background-color: #17a2b8; color: white; border-color: #17a2b8; cursor: default; }
        .pagination li.disabled span { background: transparent; border: none; cursor: default; color: #6c757d; }

        .action-buttons {
            display: flex;
            flex-direction: column;
            gap: 8px;
            align-items: center;
        }
        .btn-action {
            padding: 8px 0;
            font-size: 13px;
            font-weight: bold;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            width: 90px;
            transition: opacity 0.2s;
        }
        .btn-action:hover { opacity: 0.85; }
        .btn-confirm { background-color: #28a745; }
        .btn-cancel { background-color: #dc3545; }
        .btn-return { background-color: #f39c12; }
    </style>
</head>
<body>
<%@include file="ad-menu.jsp" %>

<div class="main-content">
    <header>
        <h1>Quản Lý Đơn Hàng</h1>
    </header>
    <div class="top-toolbar" style="display: flex; gap: 15px; margin-top: 20px; margin-bottom: 20px; align-items: center;">
        <div class="search-box" style="display: flex; box-shadow: 0 2px 5px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden; background: #fff;">
            <div style="padding: 10px 15px; display: flex; align-items: center; border: 1px solid #ddd; border-right: none; border-radius: 8px 0 0 8px;">
                <i class="fas fa-search" style="color: #888;"></i>
            </div>
            <input type="text" id="orderSearch" placeholder="Nhập Mã đơn hoặc Username..."
                   onkeypress="if(event.keyCode == 13) executeOrderSearch()"
                   style="padding: 10px 15px 10px 5px; width: 300px; border: 1px solid #ddd; border-left: none; outline: none; font-size: 14px;">
        </div>

        <select id="orderStatusFilter" onchange="executeOrderSearch()" style="padding: 10px; border: 1px solid #ddd; border-radius: 8px; font-size: 14px; outline: none; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
            <option value="-1">-- Tất cả trạng thái --</option>
            <option value="0">Đang chờ xác nhận</option>
            <option value="1">Đã xác nhận</option>
            <option value="2">Đang giao hàng</option>
            <option value="3">Đã hoàn thành</option>
            <option value="4">Đã huỷ</option>
            <option value="5">Yêu cầu hoàn trả</option>
            <option value="6">Đã hoàn tiền</option>
            <option value="7">Từ chối hoàn trả</option>
        </select>

        <button onclick="executeOrderSearch()" style="padding: 10px 20px; background: #2c3e50; color: white; border: none; cursor: pointer; font-size: 14px; font-weight: bold; border-radius: 8px; transition: 0.2s; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
            Lọc Đơn Hàng
        </button>
    </div>
    <section class="order-management">
        <table class="transaction-table">
            <thead>
            <tr>
                <th>Mã Đơn</th>
                <th>Người Đặt</th>
                <th>Sản Phẩm (ID - Tên - SL)</th>
                <th>Chi Tiết Tiền</th>
                <th>Thanh Toán</th>
                <th>Trạng Thái</th>
                <th>Thời gian</th>
                <th>Hành Động</th>
            </tr>
            </thead>
            <tbody id="order-table-body">
            <tr><td colspan="8" style="text-align:center;">Đang tải dữ liệu...</td></tr>
            </tbody>
        </table>

        <div class="pagination-container">
            <div class="page-info" id="page-info"></div>
            <ul class="pagination" id="pagination-controls"></ul>
        </div>
    </section>
</div>

<div id="adminReviewModal" class="modal-overlay">
    <div class="modal-box">
        <span class="close-btn" onclick="closeReviewModal()">&times;</span>
        <h2 style="margin-top:0; border-bottom: 2px solid #f3f3f3; padding-bottom: 10px;">Xử lý Hoàn trả - Đơn #<span id="displayOrderId" style="color:#ee4d2d;"></span></h2>
        <div style="margin-top: 15px; font-size: 15px;">
            <p><strong>Lý do:</strong> <span id="displayReason" style="color:#e74c3c;">Đang tải dữ liệu...</span></p>
            <p><strong>Mô tả chi tiết:</strong> <span id="displayDesc">Đang tải dữ liệu...</span></p>
            <p><strong>Hình ảnh minh chứng:</strong></p>
            <div style="text-align: center; background: #f9f9f9; padding: 10px; border-radius: 4px; border: 1px dashed #ccc;">
                <img id="displayImg" src="" alt="Ảnh minh chứng" style="max-width: 100%; max-height: 250px; border-radius: 4px; display: none;">
                <span id="imgLoading">Đang tải ảnh...</span>
            </div>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 25px;">
            <button onclick="submitReturnAction('reject')" style="padding: 8px 15px; background: #fff; border: 1px solid #e74c3c; color: #e74c3c; border-radius: 4px; cursor: pointer; font-weight: bold;">Từ chối</button>
            <button onclick="submitReturnAction('accept')" style="padding: 8px 15px; background: #2ecc71; border: none; color: white; border-radius: 4px; cursor: pointer; font-weight: bold;">Chấp nhận Hoàn Tiền</button>
        </div>
    </div>
</div>

<script>
    const contextPath = '${pageContext.request.contextPath}';
    const isAdmin = ${sessionScope.user.role == 1 || sessionScope.user.role == 2 ? 'true' : 'false'};
    let currentGlobalPage = 1;
    let currentReturnOrderId = null;

    document.addEventListener("DOMContentLoaded", () => {
        loadOrders(1);
    });

    function executeOrderSearch() {
        loadOrders(1); // Khi gõ tìm kiếm hoặc đổi select, luôn quay về trang 1
    }

    function loadOrders(page) {
        currentGlobalPage = page;
        const keyword = document.getElementById("orderSearch").value.trim();
        const status = document.getElementById("orderStatusFilter").value;

        fetch(`\${contextPath}/adminOrders?action=list_ajax&page=\${page}&search=\${encodeURIComponent(keyword)}&status=\${status}`)
            .then(response => response.json())
            .then(data => {
                const groupedOrders = groupOrders(data.orderDetails);
                renderTable(groupedOrders);
                renderPagination(data.currentPage, data.totalPages, data.totalOrders);
            })
            .catch(err => console.error("Lỗi khi tải đơn hàng:", err));
    }

    // Gộp dữ liệu sản phẩm theo mã đơn hàng
    function groupOrders(rawOrders) {
        const grouped = {};
        rawOrders.forEach(row => {
            const id = row.order_id;
            if (!grouped[id]) grouped[id] = [];
            grouped[id].push(row);
        });
        return grouped;
    }

    // Format Tiền tệ VN
    const formatCurrency = (amount) => new Intl.NumberFormat('vi-VN').format(amount);

    function getStatusBadge(status) {
        switch(status) {
            case 0: return '<span class="status-pending">Đang chờ xác nhận</span>';
            case 1: return '<span class="status-confirmed">Đã xác nhận</span>';
            case 2: return '<span class="status-shipping">Đang giao hàng</span>';
            case 3: return '<span class="status-done">Đã hoàn thành</span>';
            case 4: return '<span class="status-cancelled" style="color: red; font-weight: bold;">Đã huỷ</span>';
            case 5: return '<span style="color: #ee4d2d; font-weight: bold;">Yêu cầu hoàn trả</span>';
            case 6: return '<span style="color: #8e44ad; font-weight: bold;">Đã hoàn tiền</span>';
            case 7: return '<span style="color: #7f8c8d; font-weight: bold; text-decoration: line-through;">Từ chối hoàn trả</span>';
            default: return '<span class="status-unknown">Không rõ</span>';
        }
    }

    function renderTable(groupedOrders) {
        const tbody = document.getElementById("order-table-body");
        tbody.innerHTML = "";
        // Sắp xếp ID đơn hàng từ lớn nhất đến nhỏ nhất (Mới nhất lên đầu)
        const orderIds = Object.keys(groupedOrders).sort((a, b) => b - a);

        if (orderIds.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align:center;">Không có đơn hàng nào.</td></tr>`;
            return;
        }

        orderIds.forEach(orderId => {
            const products = groupedOrders[orderId];
            const first = products[0];

            let productsHtml = products.map(p => `ID: \${p.product_id} - \${p.product_name} - SL: \${p.quantity}<br/>`).join('');

            // Tính tiền
            let subTotal = 0, totalDiscount = 0;
            let shippingFee = first.shipping_fee || 0;
            products.forEach(p => {
                subTotal += p.total_money;
                totalDiscount += p.discount_amount;
            });
            let finalTotal = subTotal + shippingFee - totalDiscount;

            let moneyHtml = `
                <div style="line-height: 1.6; font-size: 0.95em;">
                    <div style="color: #555;">Tổng gốc: \${formatCurrency(subTotal)} đ</div>
                    <div style="color: #3498db;">Phí ship: \${formatCurrency(shippingFee)} đ</div>
                    \${totalDiscount > 0 ? `<div style="color: #e74c3c;"> Giảm giá: -\${formatCurrency(totalDiscount)} đ</div>` : ''}
            <div style="font-weight: bold; color: #2ecc71; font-size: 1.15em; margin-top: 5px; border-top: 1px dashed #ccc; padding-top: 5px;">
                Thực thu: \${formatCurrency(finalTotal)} đ
            </div>
        </div>`;

            let timeHtml = `<small>Ngày đặt: \${first.create_at || 'N/A'}</small><br/>`;
            if(first.status === 1) timeHtml += `<small>Xác nhận: \${first.updated_at || ''}</small>`;
            else if(first.status === 2) timeHtml += `<small>Giao hàng: \${first.updated_at || ''}</small>`;
            else if(first.status === 3) timeHtml += `<small>Hoàn thành: \${first.updated_at || ''}</small>`;
            else if(first.status === 4) timeHtml += `<small>Đã huỷ: \${first.updated_at || ''}</small>`;
            else if(first.status === 5) timeHtml += `<small>Ngày Y/c: \${first.updated_at || ''}</small>`;
            else if(first.status === 6) timeHtml += `<small>Hoàn tiền: \${first.updated_at || ''}</small>`;
            else if(first.status === 7) timeHtml += `<small>Bị từ chối: \${first.updated_at || ''}</small>`;

            // Xây dựng các nút Hành động
            let actionHtml = '<div class="action-buttons">';
            if (isAdmin) {
                if (first.status === 0) {
                    actionHtml += `<button class="btn-action btn-confirm" onclick="processAction('/confirmOrder', \${orderId}, 'Xác nhận xử lý đơn hàng này?')">Xác nhận</button>`;
        }
        if (first.status === 0 || first.status === 1) {
            actionHtml += `<button class="btn-action btn-cancel" onclick="processAction('/cancelOrder', \${orderId}, 'Bạn chắc chắn muốn hủy đơn này?')">Hủy</button>`;
        }
        if (first.status === 5) {
            actionHtml += `<button class="btn-action btn-return" onclick="openReviewModal(\${orderId})">Xử lý</button>`;
        }
    }
    actionHtml += '</div>';

    const tr = document.createElement("tr");
    tr.innerHTML = `
                <td>\${first.order_id}</td>
                <td>\${first.username}</td>
                <td><div style="line-height: 1.6;">\${productsHtml}</div></td>
                <td>\${moneyHtml}</td>
                <td>\${first.payment_code || ''}</td>
                <td>\${getStatusBadge(first.status)}</td>
                <td style="line-height: 1.5;">\${timeHtml}</td>
                <td>\${actionHtml}</td>
            `;
    tbody.appendChild(tr);
    });
    }

    // Xử lý nút Xác nhận / Hủy qua AJAX
    function processAction(urlSuffix, orderId, msg) {
        if (!confirm(msg)) return;
        fetch(`\${contextPath}\${urlSuffix}`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `orderId=\${orderId}&ajax=true`
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    loadOrders(currentGlobalPage); // Reload đúng trang hiện tại
                } else {
                    alert("Xử lý thất bại. Vui lòng kiểm tra lại!");
                }
            }).catch(err => console.error("Lỗi xử lý:", err));
    }

    // Xử lý phân trang
    function renderPagination(currentPage, totalPages, totalOrders) {
        document.getElementById("page-info").innerText = `Trang \${currentPage}/\${totalPages} – Tổng \${totalOrders} đơn hàng`;
        const ul = document.getElementById("pagination-controls");
        ul.innerHTML = "";
        if (totalPages <= 1) return;

        if (currentPage > 1) ul.innerHTML += `<li><a onclick="loadOrders(\${currentPage - 1})"><</a></li>`;
        let startPage = Math.max(1, currentPage - 1);
        let endPage = Math.min(totalPages, currentPage + 1);

        if (currentPage === 1) endPage = Math.min(totalPages, 3);
        else if (currentPage === totalPages) startPage = Math.max(1, totalPages - 2);

        if (startPage > 1) {
            ul.innerHTML += `<li><a onclick="loadOrders(1)">1</a></li>`;
            if (startPage > 2) ul.innerHTML += `<li class="disabled"><span>...</span></li>`;
        }

        for (let i = startPage; i <= endPage; i++) {
            const activeClass = (i === currentPage) ? "active" : "";
            ul.innerHTML += `<li class="\${activeClass}"><a onclick="loadOrders(\${i})">\${i}</a></li>`;
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) ul.innerHTML += `<li class="disabled"><span>...</span></li>`;
            ul.innerHTML += `<li><a onclick="loadOrders(\${totalPages})">\${totalPages}</a></li>`;
        }

        if (currentPage < totalPages) ul.innerHTML += `<li><a onclick="loadOrders(\${currentPage + 1})">></a></li>`;
    }

    // -------- LOGIC XỬ LÝ MODAL HOÀN TRẢ --------
    function openReviewModal(orderId) {
        currentReturnOrderId = orderId;
        document.getElementById('adminReviewModal').style.display = 'block';
        document.getElementById('displayOrderId').innerText = orderId;
        document.getElementById('displayReason').innerText = "Đang tải dữ liệu...";
        document.getElementById('displayDesc').innerText = "Đang tải dữ liệu...";
        document.getElementById('displayImg').style.display = 'none';
        document.getElementById('imgLoading').style.display = 'inline';

        fetch(`\${contextPath}/getReturnDetails?orderId=\${orderId}`)
            .then(res => res.json())
            .then(data => {
                if(data.success) {
                    document.getElementById('displayReason').innerText = data.reason;
                    document.getElementById('displayDesc').innerText = data.description || 'Không có mô tả thêm';
                    document.getElementById('displayImg').src = `\${contextPath}/\${data.proofImg}`;
                    document.getElementById('displayImg').style.display = 'inline-block';
                    document.getElementById('imgLoading').style.display = 'none';
                }
            }).catch(err => console.error("Lỗi fetch:", err));
    }

    function closeReviewModal() {
        document.getElementById('adminReviewModal').style.display = 'none';
    }

    function submitReturnAction(actionType) {
        let msg = actionType === 'accept' ? 'Xác nhận thu hồi hàng và Hoàn tiền?' : 'Bạn từ chối hoàn trả đơn hàng này?';
        if (!confirm(msg)) return;

        fetch(`\${contextPath}/adminProcessReturn`, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: `orderId=\${currentReturnOrderId}&action=\${actionType}&ajax=true`
        })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    closeReviewModal();
                    loadOrders(currentGlobalPage);
                } else {
                    alert("Xử lý thất bại!");
                }
            }).catch(err => console.error(err));
    }
</script>
</body>
</html>