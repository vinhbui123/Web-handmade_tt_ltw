// ==================== LOGIC API 34 TỈNH (ESGOO) - BỎ QUA QUẬN/HUYỆN ====================

async function fetchProvinces() {
    const pSelect = document.getElementById('province');
    if (!pSelect) return;
    try {
        const res = await fetch('https://esgoo.net/api-tinhthanh-new/1/0.htm');
        const result = await res.json();

        pSelect.innerHTML = '<option value="" disabled selected>Chọn Tỉnh/Thành phố</option>';
        if (result.error === 0) {
            result.data.forEach(p => {
                pSelect.options[pSelect.options.length] = new Option(p.full_name, p.id);
            });
        }
    } catch (err) { console.error("Lỗi tải API Tỉnh:", err); }
}

async function onProvinceChange() {
    const pCode = document.getElementById('province').value;
    const dSelect = document.getElementById('district');
    const wSelect = document.getElementById('ward');
    if (dSelect) dSelect.style.display = "none";
    // Khóa và làm mờ ô Quận/Huyện
    dSelect.innerHTML = '<option value="" selected>--- Bỏ qua Quận/Huyện ---</option>';
    dSelect.disabled = true;
    dSelect.style.opacity = "0.5";

    wSelect.innerHTML = '<option value="" disabled selected>Chọn Phường/Xã</option>';
    wSelect.disabled = true;

    if (!pCode) return;
    try {
        // Lấy dữ liệu Phường/Xã (Cấp 2 của Esgoo)
        const res = await fetch(`https://esgoo.net/api-tinhthanh-new/2/${pCode}.htm`);
        const result = await res.json();

        if (result.error === 0) {
            result.data.forEach(w => {
                wSelect.options[wSelect.options.length] = new Option(w.full_name, w.id);
            });
            wSelect.disabled = false;
        }
    } catch (err) { console.error("Lỗi tải API Phường/Xã:", err); }
}

async function onDistrictChange() {
}
// ==================== ĐIỀU KHIỂN POPUP VÀ GIAO DIỆN ====================
async function openAddressPopup() {
    document.getElementById("addressModal").style.display = "block";
    document.getElementById('addressListView').style.display = 'block';
    document.getElementById('addressFormView').style.display = 'none';
    await reloadAddressList();
}

function closeAddressPopup() {
    document.getElementById("addressModal").style.display = "none";
}

function showAddressFormOnly() {
    document.getElementById('addressListView').style.display = 'none';
    document.getElementById('addressFormView').style.display = 'block';
}

function backToAddressList() {
    document.getElementById('addressFormView').style.display = 'none';
    document.getElementById('addressListView').style.display = 'block';
}

function clearAddressForm() {
    document.getElementById("addressId").value = "";
    document.getElementById("fullName").value = "";
    document.getElementById("phone").value = "";
    document.getElementById("province").innerHTML = '<option value="" disabled selected>Chọn Tỉnh/Thành phố</option>';
    const dSelect = document.getElementById("district");
    if (dSelect) {
        dSelect.innerHTML = '';
        dSelect.style.display = "none";
    }
    dSelect.innerHTML = '<option value="" disabled selected>Chọn Quận/Huyện</option>';
    dSelect.disabled = true;
    dSelect.style.opacity = "1";

    document.getElementById("ward").innerHTML = '<option value="" disabled selected>Chọn Phường/Xã</option>';
    document.getElementById("ward").disabled = true;
    document.getElementById("addressDetail").value = "";
    document.querySelectorAll('input[name="addressType"]').forEach(r => r.checked = false);
    document.getElementById("isDefault").checked = false;
}

// Đổ dữ liệu khi bấm nút Sửa
async function editAddress(data) {
    clearAddressForm();

    document.getElementById("addressId").value = data.id || "";
    document.getElementById("fullName").value = data.fullName || "";
    document.getElementById("phone").value = data.phone || "";
    document.getElementById("addressDetail").value = data.addressDetail || "";

    // 1. Tải Tỉnh và chọn đúng Tỉnh
    await fetchProvinces();
    const pSelect = document.getElementById('province');
    selectOptionByText(pSelect, data.province);

    // 2. Tải Xã và chọn đúng Xã (Huyện đã bị khóa)
    await onProvinceChange();
    const wSelect = document.getElementById('ward');
    selectOptionByText(wSelect, data.ward);

    document.querySelectorAll('input[name="addressType"]').forEach(radio => {
        radio.checked = (radio.value === data.addressType ||
            (data.addressType === 'HOME' && radio.value === 'Nhà riêng') ||
            (data.addressType === 'OFFICE' && radio.value === 'Văn phòng'));
    });

    document.getElementById("isDefault").checked = (data.isDefault === true || data.isDefault === 'true');
}

// Hàm hỗ trợ chọn option dựa trên text hiển thị
function selectOptionByText(selectElem, text) {
    if(!text) return;
    const cleanText = text.trim();
    for (let i = 0; i < selectElem.options.length; i++) {
        if (selectElem.options[i].text === cleanText) {
            selectElem.selectedIndex = i;
            break;
        }
    }
}

async function handleEditButton(btn) {
    const data = {
        id: parseInt(btn.dataset.id),
        fullName: btn.dataset.fullname,
        phone: btn.dataset.phone,
        province: btn.dataset.province.trim(),
        district: "", // Không lấy huyện cũ nữa
        ward: btn.dataset.ward.trim(),
        addressDetail: btn.dataset.detail,
        addressType: btn.dataset.type,
        isDefault: btn.dataset.default === 'true'
    };
    showAddressFormOnly();
    await editAddress(data);
}

function addNewAddress() {
    clearAddressForm();
    showAddressFormOnly();
    fetchProvinces();
}

function submitForm() {
    const fullName = document.getElementById('fullName').value.trim();
    const pSelect = document.getElementById('province');
    const wSelect = document.getElementById('ward');
    const phone = document.getElementById('phone').value.trim();
    const addressTypeInput = document.querySelector('input[name="addressType"]:checked');
    const addressDetail = document.getElementById('addressDetail').value.trim();

    if (fullName === "") {
        alert("Vui lòng nhập Họ và tên.");
        document.getElementById('fullName').focus();
        return;
    }

    if (!/^0\d{9}$/.test(phone)) {
        alert("Số điện thoại phải bao gồm đúng 10 chữ số và bắt đầu bằng số 0.");
        return;
    }

    // Chỉ bắt buộc chọn Tỉnh và Xã
    if (pSelect.selectedIndex <= 0 || wSelect.selectedIndex <= 0) {
        alert("Vui lòng chọn đầy đủ Tỉnh/Thành phố và Phường/Xã");
        return;
    }

    if (addressDetail === "") {
        alert("Vui lòng nhập Địa chỉ cụ thể (Số nhà, tên đường...).");
        document.getElementById('addressDetail').focus();
        return;
    }

    if (!addressTypeInput) {
        alert("Vui lòng chọn Loại địa chỉ (Nhà riêng hoặc Văn phòng).");
        return;
    }

    const data = {
        id: parseInt(document.getElementById('addressId').value) || null,
        fullName: fullName,
        phone: phone,
        province: pSelect.options[pSelect.selectedIndex].text,
        district: "", // Gửi chuỗi rỗng đi
        ward: wSelect.options[wSelect.selectedIndex].text,
        addressDetail: addressDetail,
        addressType: addressTypeInput.value === 'Nhà riêng' ? 'HOME' : 'OFFICE',
        isDefault: document.getElementById('isDefault').checked
    };

    fetch(`${contextPath}/address-form`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
        .then(res => res.json())
        .then(response => {
            if (response.status) {
                alert("Lưu địa chỉ thành công!");
                if (response.addressDefault) updateAddressDetails(response.addressDefault);
                closeAddressPopup();
                reloadAddressList();
            } else {
                alert(response.message || "Có lỗi xảy ra khi lưu địa chỉ.");
            }
        })
        .catch(err => {
            console.error("Error:", err);
            alert("Có lỗi kết nối khi lưu địa chỉ.");
        });
}

// ==================== API DANH SÁCH ĐỊA CHỈ (GET/DELETE/SET-DEFAULT) ====================

async function reloadAddressList() {
    try {
        const res = await fetch(`${contextPath}/get-address-list`);
        const data = await res.json();
        if (!data.status) return;

        const addressListContainer = document.querySelector('.address-list');
        addressListContainer.innerHTML = '';

        data.addressList.forEach(address => {
            const addressCard = createAddressCard(address);
            addressListContainer.appendChild(addressCard);
        });
    } catch (error) {
        console.error('Lỗi khi load danh sách địa chỉ:', error);
    }
}

function createAddressCard(address) {
    const div = document.createElement('div');
    div.className = 'address-card';
    div.setAttribute('data-address-id', address.id);

    div.innerHTML = `
        <p><strong>${address.fullName}</strong> - ${address.phone}</p>
        <p>${address.addressDetail}, ${address.ward}, ${address.district}, ${address.province}</p>
        <p>
            Loại: ${address.addressType === 'HOME' ? 'Nhà riêng' : 'Văn phòng'}
            ${address.isDefault ? '<span class="default-badge">Mặc định</span>' : ''}
        </p>

        <div class="address-actions">
            <label style="display: flex; align-items: center; cursor: pointer; font-size: 13px;">
                <input type="checkbox" name="defaultAddress"
                       ${address.isDefault ? 'checked disabled' : ''}
                       onchange="setDefaultAddress(${address.id})" style="margin-right: 6px;">
                Đặt làm mặc định
            </label>

            <div class="button-group">
                <button type="button" class="edit-btn"
                    data-id="${address.id}" data-fullname="${address.fullName}"
                    data-phone="${address.phone}" data-province="${address.province}"
                    data-district="${address.district}" data-ward="${address.ward}"
                    data-detail="${address.addressDetail}" data-type="${address.addressType}"
                    data-default="${address.isDefault ? 'true' : 'false'}"
                    onclick="handleEditButton(this)">Chỉnh sửa
                </button>

                <button type="button" class="delete-btn" onclick="deleteAddress(${address.id}, ${address.isDefault});">Xóa</button>
            </div>
        </div>
    `;
    return div;
}

function deleteAddress(addressId, isDefault) {
    if (isDefault) {
        alert('Không thể xóa địa chỉ mặc định.');
        return;
    }
    if(!confirm("Bạn có chắc chắn muốn xóa địa chỉ này?")) return;

    fetch(`${contextPath}/delete-address`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ addressId: addressId })
    })
        .then(response => response.json())
        .then(data => {
            if (data.status) {
                const addressElement = document.querySelector(`[data-address-id="${addressId}"]`);
                if (addressElement) addressElement.remove();
            } else {
                alert(data.message || 'Có lỗi xảy ra khi xóa địa chỉ.');
            }
        })
        .catch(() => alert('Có lỗi xảy ra khi xóa địa chỉ.'));
}

function setDefaultAddress(addressId) {
    fetch(`${contextPath}/default-address`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ addressId: addressId })
    })
        .then(res => res.json())
        .then(data => {
            if (data.status) {
                if (data.addressDefault) updateAddressDetails(data.addressDefault);
                reloadAddressList();
                alert("Đặt địa chỉ mặc định thành công!");
            } else {
                alert(data.message || "Không thể đặt mặc định. Đã xảy ra lỗi.");
            }
        })
        .catch(error => alert("Không thể đặt mặc định. Lỗi kết nối server."));
}

function updateAddressDetails(address) {
    const addressDetails = document.querySelector('.address-details');
    if (!addressDetails) return;

    addressDetails.innerHTML = `
        <span class="address-info-text" style="font-weight: bold">${address.fullName}, SĐT: ${address.phone}</span><br>
        <span class="address-string">${address.addressDetail}, ${address.ward}, ${address.district}, ${address.province}</span>
        <input type="hidden" id="address-id-check" value="${address.id}" />
    `;

}