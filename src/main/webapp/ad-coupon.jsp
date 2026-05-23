<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Quản Lý Mã Giảm Giá - Admin</title>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
  <script src="${pageContext.request.contextPath}/js/admin.js"></script>
  <style>
    /* Tùy chỉnh thêm để phù hợp với đặc thù của Coupon */
    .type-badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; color: white; }
    .badge-money { background-color: #3498db; }
    .badge-percent { background-color: #9b59b6; }
    .status-valid { color: #2ecc71; font-weight: bold; }
    .status-expired { color: #e74c3c; font-weight: bold; }
  </style>
</head>
<body>
<%@include file="ad-menu.jsp" %>
<div class="main-content">
  <header>
    <h1>Quản Lý Mã Giảm Giá</h1>
  </header>

  <section class="product-management">
    <button class="btn-add" onclick="openCouponModal('add')">
      <i class="fa-solid fa-plus"></i> Thêm Mã Mới
    </button>

    <table class="product-table" style="margin-top: 20px;">
      <thead>
      <tr>
        <th>ID</th>
        <th>Mã Code</th>
        <th>Loại</th>
        <th>Giá Trị</th>
        <th>Đơn tối thiểu</th>
        <th>Giảm tối đa</th>
        <th>Thời hạn</th>
        <th>Hành Động</th>
      </tr>
      </thead>
      <tbody>
      <c:forEach var="cp" items="${coupons}">
        <tr>
          <td>${cp.id}</td>
          <td style="font-weight: bold; color: #2c3e50;">${cp.code}</td>
          <td>
                        <span class="type-badge ${cp.type == 0 ? 'badge-money' : 'badge-percent'}">
                            ${cp.type == 0 ? 'Tiền mặt' : 'Phần trăm'}
                        </span>
          </td>
          <td>
            <c:choose>
              <c:when test="${cp.type == 0}">
                <f:formatNumber value="${cp.discountValue}" pattern="#,##0đ"/>
              </c:when>
              <c:otherwise>${cp.discountPercent}%</c:otherwise>
            </c:choose>
          </td>
          <td><f:formatNumber value="${cp.minOrderAmount}" pattern="#,##0đ"/></td>
          <td>
            <c:choose>
              <c:when test="${cp.maxDiscountValue != null}">
                <f:formatNumber value="${cp.maxDiscountValue}" pattern="#,##0đ"/>
              </c:when>
              <c:otherwise>---</c:otherwise>
            </c:choose>
          </td>
          <td style="font-size: 13px;">
              ${cp.formattedExpiredDate}
          </td>
          <td>
            <i class="fa-solid fa-pen-to-square btn-edit"
               data-id="${cp.id}"
               data-code="${cp.code}"
               data-type="${cp.type}"
               data-val="${cp.discountValue}"
               data-per="${cp.discountPercent}"
               data-min="${cp.minOrderAmount}"
               data-max="${cp.maxDiscountValue}"
               data-start="${cp.startDate}"
               data-end="${cp.endDate}"
               onclick="openCouponModal('edit', this)"></i>

            <form action="adminCoupons" method="post" style="display: inline;">
              <input type="hidden" name="action" value="delete">
              <input type="hidden" name="id" value="${cp.id}">
              <button type="submit" class="btn-delete" onclick="return confirm('Xóa mã này?')">
                <i class="fa-solid fa-trash"></i>
              </button>
            </form>
          </td>
        </tr>
      </c:forEach>
      </tbody>
    </table>
  </section>

  <div id="couponModal" class="modal">
    <div class="modal-content" style="width: 40%;">
      <span class="close" onclick="closeModal('couponModal')">&times;</span>
      <h2 id="modalTitle">Thêm Mã Giảm Giá</h2>
      <form action="adminCoupons" method="post">
        <input type="hidden" name="action" id="formAction" value="add">
        <input type="hidden" name="id" id="couponId">

        <label>Mã Code:</label>
        <input type="text" name="code" id="couponCode" placeholder="Ví dụ: GIAM50K" required>

        <label>Loại giảm giá:</label>
        <select name="type" id="couponType" onchange="toggleCouponFields()" required>
          <option value="0">Giảm theo tiền mặt (đ)</option>
          <option value="1">Giảm theo phần trăm (%)</option>
        </select>

        <div id="divValue">
          <label>Số tiền giảm (đ):</label>
          <input type="number" name="discountValue" id="couponValue">
        </div>

        <div id="divPercent" style="display:none;">
          <label>Phần trăm giảm (%):</label>
          <input type="number" name="discountPercent" id="couponPercent">
        </div>

        <label>Đơn hàng tối thiểu (đ):</label>
        <input type="number" name="minOrderAmount" id="couponMin" value="0" required>

        <label>Giảm tối đa (đ):</label>
        <input type="number" name="maxDiscountValue" id="couponMax">

        <div style="display: flex; gap: 10px;">
          <div style="flex: 1;">
            <label>Ngày bắt đầu:</label>
            <input type="datetime-local" name="startDate" id="couponStart" required>
          </div>
          <div style="flex: 1;">
            <label>Ngày kết thúc:</label>
            <input type="datetime-local" name="endDate" id="couponEnd" required>
          </div>
        </div>

        <button type="submit" class="btn-save" style="width: 100%; margin-top: 20px;">Lưu Thông Tin</button>
      </form>
    </div>
  </div>
</div>

<script>
  function toggleCouponFields() {
    const type = document.getElementById('couponType').value;
    document.getElementById('divValue').style.display = (type == "0") ? "block" : "none";
    document.getElementById('divPercent').style.display = (type == "1") ? "block" : "none";
  }

  function openCouponModal(mode, element = null) {
    // Mở modal lên trước
    document.getElementById('couponModal').style.display = 'block';

    // Lấy nhanh các thẻ inputs
    const modalTitle = document.getElementById('modalTitle');
    const formAction = document.getElementById('formAction');
    const couponId = document.getElementById('couponId');
    const couponCode = document.getElementById('couponCode');
    const couponType = document.getElementById('couponType');
    const couponMin = document.getElementById('couponMin');
    const couponMax = document.getElementById('couponMax');
    const couponValue = document.getElementById('couponValue');
    const couponPercent = document.getElementById('couponPercent');
    const couponStart = document.getElementById('couponStart');
    const couponEnd = document.getElementById('couponEnd');

    if (mode === 'add') {
      modalTitle.innerText = 'Thêm Mã Giảm Giá';
      formAction.value = 'add';
      couponId.value = '';
      couponCode.value = '';
      couponType.value = '0';
      couponMin.value = '0';
      couponMax.value = '';
      couponValue.value = '';
      couponPercent.value = '';
      couponStart.value = '';
      couponEnd.value = '';
      toggleCouponFields();
    } else if (mode === 'edit' && element) {
      modalTitle.innerText = 'Sửa Mã Giảm Giá';
      formAction.value = 'update';

      // Đọc data từ các thẻ data-* của chính cái nút vừa bấm
      couponId.value = element.getAttribute('data-id');
      couponCode.value = element.getAttribute('data-code');
      couponType.value = element.getAttribute('data-type');
      couponMin.value = element.getAttribute('data-min');
      couponMax.value = element.getAttribute('data-max') || '';
      couponValue.value = element.getAttribute('data-val');
      couponPercent.value = element.getAttribute('data-per');

      // Xử lý cắt chuỗi ngày tháng cho khớp định dạng yyyy-MM-ddTHH:mm của HTML5
      let startVal = element.getAttribute('data-start');
      let endVal = element.getAttribute('data-end');

      if (startVal && startVal.length >= 16) couponStart.value = startVal.substring(0, 16);
      if (endVal && endVal.length >= 16) couponEnd.value = endVal.substring(0, 16);

      toggleCouponFields();
    }
  }

  function closeModal(id) {
    document.getElementById(id).style.display = 'none';
  }
</script>
</body>
</html>