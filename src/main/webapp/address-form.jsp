<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/address-form.css">
<script src="${pageContext.request.contextPath}/js/address-form.js"></script>

<button onclick="openAddressPopup()">Cập nhật địa chỉ</button>

<div id="addressModal" class="modal">
    <div class="modal-content">
        <span class="close" onclick="closeAddressPopup()">&times;</span>

        <div id="addressListView">
            <h3>Danh sách địa chỉ</h3>
            <div id="addressList" class="address-list">
                <c:forEach var="address" items="${addressList}">
                    <div class="address-card" data-address-id="${address.id}">
                        <p><strong>${address.fullName}</strong> - ${address.phone}</p>
                        <p>${address.addressDetail}, ${address.ward}, ${address.district}, ${address.province}</p>
                        <p>
                            Loại: ${address.addressType}
                            <c:if test="${address.isDefault}">
                                <span class="default-badge">Mặc định</span>
                            </c:if>
                        </p>

                        <div class="address-actions">
                            <label style="display: flex; align-items: center; cursor: pointer;">
                                <input type="checkbox" name="defaultAddress" <c:if test="${address.isDefault}">checked</c:if> onchange="setDefaultAddress(${address.id})" style="margin: 0 6px 0 0;">
                                Đặt làm mặc định2
                            </label>

                            <button type="button" class="edit-btn" data-id="${address.id}"
                                    data-fullname="${address.fullName}" data-phone="${address.phone}"
                                    data-province="${address.province}" data-district="${address.district}"
                                    data-ward="${address.ward}" data-detail="${address.addressDetail}"
                                    data-type="${address.addressType}"
                                    data-default="${address.isDefault ? 'true' : 'false'}"
                                    onclick="handleEditButton(this)">
                                Chỉnh sửa
                            </button>

                            <button type="button" class="delete-btn" onclick="deleteAddress(${address.id}, ${address.isDefault ? 'true' : 'false'});">
                                Xóa
                            </button>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <button onclick="clearAddressForm(); addNewAddress();" class="add-address-btn">+ Thêm địa chỉ mới</button>
        </div>

        <div id="addressFormView" style="display: none;">
            <input type="hidden" name="id" id="addressId" />
            <h3>Địa chỉ của tôi</h3>
            <div class="form-row">
                <input type="text" name="fullName" id="fullName" placeholder="Họ và tên" required />
                <input type="tel" name="phone" id="phone" placeholder="Số điện thoại" required />
            </div>

            <div class="form-row">
                <select id="province" class="form-control-api" onchange="onProvinceChange()" required>
                    <option value="" disabled selected>Chọn Tỉnh/Thành phố</option>
                </select>
            </div>
            <div class="form-row" style="display: none;">
                <select id="district" class="form-control-api" onchange="onDistrictChange()" disabled required style="display: none;">
                    <option value="" disabled selected>Chọn Quận/Huyện</option>
                </select>
            </div>
            <div class="form-row">
                <select id="ward" class="form-control-api" disabled required>
                    <option value="" disabled selected>Chọn Phường/Xã</option>
                </select>
            </div>

            <textarea name="addressDetail" id="addressDetail" class="address-detail-fixed" placeholder="Địa chỉ cụ thể" required></textarea>

            <label style="padding-top: 5px;">Loại địa chỉ:</label>
            <div class="address-type">
                <label><input type="radio" name="addressType" value="Nhà riêng" /> Nhà Riêng</label>
                <label><input type="radio" name="addressType" value="Văn phòng" /> Văn Phòng</label>
            </div>

            <label for="isDefault" style="display: flex; align-items: center; cursor: pointer; margin-top: 5px; margin-bottom: 10px;">
                <input type="checkbox" name="isDefault" id="isDefault" value="true" checked style="margin: 0 8px 0 0; width: 16px; height: 16px; cursor: pointer;" />
                <span style="font-size: 14px; padding-top: 2px; color: #222; font-weight: 600;">Đặt làm mặc định</span>
            </label>

            <div class="actions">
                <button type="button" class="btn-cancel" onclick="backToAddressList()">Trở lại</button>
                <button type="button" class="btn-submit" onclick="submitForm()">Lưu</button>
            </div>
        </div>
    </div>
</div>