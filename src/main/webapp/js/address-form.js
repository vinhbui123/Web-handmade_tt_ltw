
// Tải danh sách Tỉnh/Thành
async function fetchProvinces() {
    const pSelect = document.getElementById('province');
    if (!pSelect) return;
    try {
        const res = await fetch(`${contextPath}/provinces`);
        const result = await res.json();

        pSelect.innerHTML = '<option value="" disabled selected>Chọn Tỉnh/Thành phố</option>';
        if (result.code === 200) {
            result.data.forEach(p => {
                // Lưu ID vào value, lưu Tên vào text hiển thị
                const nameLower = p.name.toLowerCase();
                if (!nameLower.includes("test") && !nameLower.includes("alert") && nameLower !== "hà nội 02") {
                    pSelect.options[pSelect.options.length] = new Option(p.name, p.id);
                }
            });
        }
    } catch (err) { console.error("Lỗi tải API Tỉnh:", err); }
}

// Khi chọn Tỉnh -> Tải Quận/Huyện
async function onProvinceChange() {
    const pId = document.getElementById('province').value;
    const dSelect = document.getElementById('district');
    const wSelect = document.getElementById('ward');

    // Reset dropdown cấp dưới
    dSelect.innerHTML = '<option value="" disabled selected>Chọn Quận/Huyện</option>';
    dSelect.disabled = true;
    wSelect.innerHTML = '<option value="" disabled selected>Chọn Phường/Xã</option>';
    wSelect.disabled = true;

    if (!pId) return;
    try {
        const res = await fetch(`${contextPath}/districts?province_id=${pId}`);
        const result = await res.json();

        if (result.code === 200) {
            result.data.forEach(d => {
                dSelect.options[dSelect.options.length] = new Option(d.name, d.id);
            });
            dSelect.disabled = false;
        }
    } catch (err) { console.error("Lỗi tải API Quận/Huyện:", err); }
}

// Khi chọn Quận -> Tải Phường/Xã
async function onDistrictChange() {
    const dId = document.getElementById('district').value;
    const wSelect = document.getElementById('ward');

    wSelect.innerHTML = '<option value="" disabled selected>Chọn Phường/Xã</option>';
    wSelect.disabled = true;

    if (!dId) return;
    try {
        const res = await fetch(`${contextPath}/wards?district_id=${dId}`);
        const result = await res.json();

        if (result.code === 200) {
            result.data.forEach(w => {
                wSelect.options[wSelect.options.length] = new Option(w.name, w.id);
            });
            wSelect.disabled = false;
        }
    } catch (err) { console.error("Lỗi tải API Phường/Xã:", err); }
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
    document.getElementById("addressDetail").value = "";

    document.getElementById("province").innerHTML = '<option value="" disabled selected>Chọn Tỉnh/Thành phố</option>';

    const dSelect = document.getElementById("district");
    dSelect.innerHTML = '<option value="" disabled selected>Chọn Quận/Huyện</option>';
    dSelect.disabled = true;

    const wSelect = document.getElementById("ward");
    wSelect.innerHTML = '<option value="" disabled selected>Chọn Phường/Xã</option>';
    wSelect.disabled = true;

    document.querySelectorAll('input[name="addressType"]').forEach(r => r.checked = false);
    document.getElementById("isDefault").checked = false;
}

// ==================== XỬ LÝ CHỈNH SỬA & LƯU ĐỊA CHỈ ====================

// Đổ dữ liệu khi bấm nút Sửa (Có chờ await để load xong data)
async function editAddress(data) {
    clearAddressForm();

    document.getElementById("addressId").value = data.id || "";
    document.getElementById("fullName").value = data.fullName || "";
    document.getElementById("phone").value = data.phone || "";
    document.getElementById("addressDetail").value = data.addressDetail || "";

    // 1. Tải Tỉnh và chọn
    await fetchProvinces();
    selectOptionByText(document.getElementById('province'), data.province);

    // 2. Tải Quận và chọn
    await onProvinceChange();
    selectOptionByText(document.getElementById('district'), data.district);

    // 3. Tải Phường và chọn
    await onDistrictChange();
    selectOptionByText(document.getElementById('ward'), data.ward);

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
        district: btn.dataset.district.trim(),
        district: btn.dataset.district.trim(),
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
    const dSelect = document.getElementById('district');
    const wSelect = document.getElementById('ward');
    const phone = document.getElementById('phone').value.trim();
    const addressTypeInput = document.querySelector('input[name="addressType"]:checked');
    const addressDetail = document.getElementById('addressDetail').value.trim();

    if (fullName === "") return alert("Vui lòng nhập Họ và tên.");
    if (!/^0\d{9}$/.test(phone)) return alert("Số điện thoại phải 10 số và bắt đầu bằng 0.");

    // Bắt buộc chọn cả 3 cấp
    if (pSelect.selectedIndex <= 0 || dSelect.selectedIndex <= 0 || wSelect.selectedIndex <= 0) {
        return alert("Vui lòng chọn đầy đủ Tỉnh, Quận/Huyện và Phường/Xã.");
    }

    if (addressDetail === "") return alert("Vui lòng nhập Địa chỉ cụ thể.");
    if (!addressTypeInput) return alert("Vui lòng chọn Loại địa chỉ.");

    const data = {
        id: parseInt(document.getElementById('addressId').value) || null,
        fullName: fullName,
        phone: phone,
        // Gửi TÊN ĐỊA CHỈ (text) về DB, không phải ID
        province: pSelect.options[pSelect.selectedIndex].text,
        district: dSelect.options[dSelect.selectedIndex].text,
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
                if (response.addressDefault) {
                    updateAddressDetails(response.addressDefault);
                    if(typeof loadShippingMethods === "function") {
                        loadShippingMethods();
                    }
                }
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

async function reloadAddressList() {
    try {
        const res = await fetch(`${contextPath}/get-address-list`);
        const data = await res.json();
        if (!data.status) return;

        const addressListContainer = document.querySelector('.address-list');
        addressListContainer.innerHTML = '';
        data.addressList.forEach(address => {
            addressListContainer.appendChild(createAddressCard(address));
        });
    } catch (error) { console.error('Lỗi khi load ds địa chỉ:', error); }
}

function createAddressCard(address) {
    const div = document.createElement('div');
    div.className = 'address-card';
    div.setAttribute('data-address-id', address.id);
    div.innerHTML = `
        <p><strong>${address.fullName}</strong> - ${address.phone}</p>
        <p>${address.addressDetail}, ${address.ward}, ${address.district}, ${address.province}</p>
        <p>Loại: ${address.addressType === 'HOME' ? 'Nhà riêng' : 'Văn phòng'}
            ${address.isDefault ? '<span class="default-badge">Mặc định</span>' : ''}
        </p>
        <div class="address-actions">
            <label style="display: flex; align-items: center; cursor: pointer; font-size: 13px;">
                <input type="checkbox" name="defaultAddress" ${address.isDefault ? 'checked disabled' : ''} 
                       onchange="setDefaultAddress(${address.id})" style="margin-right: 6px;"> Đặt làm mặc định
            </label>
            <div class="button-group">
                <button type="button" class="edit-btn"
                    data-id="${address.id}" data-fullname="${address.fullName}"
                    data-phone="${address.phone}" data-province="${address.province}"
                    data-district="${address.district}" data-ward="${address.ward}"
                    data-detail="${address.addressDetail}" data-type="${address.addressType}"
                    data-default="${address.isDefault ? 'true' : 'false'}"
                    onclick="handleEditButton(this)">Chỉnh sửa</button>
                <button type="button" class="delete-btn" onclick="deleteAddress(${address.id}, ${address.isDefault});">Xóa</button>
            </div>
        </div>`;
    return div;
}

function deleteAddress(addressId, isDefault) {
    if (isDefault) return alert('Không thể xóa địa chỉ mặc định.');
    if(!confirm("Bạn có chắc chắn muốn xóa địa chỉ này?")) return;

    fetch(`${contextPath}/delete-address`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ addressId: addressId })
    })
        .then(res => res.json())
        .then(data => {
            if (data.status) {
                document.querySelector(`[data-address-id="${addressId}"]`).remove();
            } else alert(data.message);
        });
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
                if (data.addressDefault) {
                    updateAddressDetails(data.addressDefault);
                    if(typeof loadShippingMethods === "function") {
                        loadShippingMethods();
                    }
                }
                reloadAddressList();
                alert("Đặt địa chỉ mặc định thành công!");
            } else alert(data.message);
        });
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