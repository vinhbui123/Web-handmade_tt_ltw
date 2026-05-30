<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
  Created by IntelliJ IDEA.
  User: Admin
  Date: 1/7/2025
  Time: 1:47 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Quản Lý Sản Phẩm - Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <script src="${pageContext.request.contextPath}/js/admin.js"></script>
    <script>
        window.contextPath = "<%= request.getContextPath() %>";
    </script>
    <script src="${pageContext.request.contextPath}/js/product.js"></script>
    <style>
        th.sortable {
            cursor: pointer;
            transition: color 0.3s;
            position: relative;
            user-select: none;
            white-space: nowrap;
        }
        th.sortable:hover {
            color: #f39c12;
        }
        th.active-sort {
            color: #e74c3c;
            font-weight: bold;
        }
        th select {
            margin-top: 5px;
            padding: 4px;
            border-radius: 4px;
            border: 1px solid #ccc;
            width: 100%;
            font-size: 13px;
            cursor: pointer;
            outline: none;
            color: #333;
        }
        .product-table th i {
            margin-left: 5px;
            vertical-align: middle;
        }
        .material-tags {
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
            justify-content: center;
            align-items: center;
        }

        .material-badge {
            background-color: #f8f9fa;
            color: #212529;
            font-weight: 550;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 12px;
            border: 1px solid #dee2e6;
            white-space: nowrap;
            display: inline-block;
            transition: all 0.2s ease;
        }

        .material-badge:hover {
            background-color: #e9ecef;
            border-color: #ced4da;
        }
    </style>

</head>
<body>
<%@include file="ad-menu.jsp" %>
<div class="main-content">
    <header>
        <h1>Quản Lý Sản Phẩm</h1>

    </header>
    <c:if test="${not empty sessionScope.message}">
        <div class="alert ${sessionScope.messageType}">
            <p>${sessionScope.message}</p>
        </div>
        <c:remove var="message" scope="session"/>
        <c:remove var="messageType" scope="session"/>
    </c:if>

    <div class="top-toolbar" style="display: flex; justify-content: flex-start; align-items: center; gap: 20px; margin-bottom: 25px; margin-top: 10px;">

        <div class="search-box" style="display: flex; gap: 0; box-shadow: 0 2px 5px rgba(0,0,0,0.1); border-radius: 8px; overflow: hidden;">
            <div style="background: #fff; padding: 10px 15px; display: flex; align-items: center; border: 1px solid #ddd; border-right: none; border-radius: 8px 0 0 8px;">
                <i class="fas fa-search" style="color: #888;"></i>
            </div>
            <input type="text" id="searchInput" placeholder="Nhập ID hoặc Tên sản phẩm..."
                   value="${searchKeyword}"
                   onkeypress="if(event.keyCode == 13) executeSearch()"
                   style="padding: 10px 15px 10px 5px; width: 350px; border: 1px solid #ddd; border-left: none; outline: none; font-size: 14px;">
            <button onclick="executeSearch()" style="padding: 10px 20px; background: #2c3e50; color: white; border: none; cursor: pointer; font-size: 14px; font-weight: bold; transition: 0.2s;">
                Tìm kiếm
            </button>
        </div>

        <c:choose>
            <c:when test="${sessionScope.user.role == 1}">
                <button class="btn-add" onclick="openModal('add')" style="margin: 0; padding: 10px 20px; font-size: 14px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                    <i class="fa-solid fa-plus"></i> Thêm Sản Phẩm
                </button>
            </c:when>
            <c:otherwise>
                <button class="btn-add disabled" style="margin: 0; padding: 10px 20px; font-size: 14px; border-radius: 8px; opacity: 0.5; cursor: not-allowed;"
                        onclick="alert('Bạn không có quyền thêm sản phẩm!')">
                    <i class="fa-solid fa-plus"></i> Thêm Sản Phẩm
                </button>
            </c:otherwise>
        </c:choose>
    </div>

    <section class="product-management">
        <table class="product-table">
            <thead>
            <tr>
                <th>Ảnh</th>
                <th class="sortable" data-col="id" onclick="sortData('id')">ID <i class="fas fa-sort"></i></th>
                <th class="sortable" data-col="name" onclick="sortData('name')">Tên Sản Phẩm <i class="fas fa-sort"></i></th>
                <th class="sortable" data-col="price" onclick="sortData('price')">Giá <i class="fas fa-sort"></i></th>
                <th class="sortable" data-col="stock" onclick="sortData('stock')">Số Lượng <i class="fas fa-sort"></i></th>

                <th>
                    Danh Mục <br>
                    <select id="filterCategory" onchange="filterData()">
                        <option value="">-- Tất cả --</option>
                        <c:forEach var="cat" items="${category}">
                            <option value="${cat.id}" ${cat.id == selectedCategoryId ? 'selected' : ''}>${cat.name}</option>
                        </c:forEach>
                    </select>
                </th>

                <th>
                    Chất Liệu <br>
                    <select id="filterMaterial" onchange="filterData()">
                        <option value="">-- Tất cả --</option>
                        <c:forEach var="mat" items="${materials}">
                            <option value="${mat.id}" ${mat.id == selectedMaterialId ? 'selected' : ''}>${mat.name}</option>
                        </c:forEach>
                    </select>
                </th>

                <th>Hành Động</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="product" items="${products}">
                <tr>
                    <td><img src="${product.img}" alt="${product.name}"
                             style="width: 50px; height: 50px; object-fit: cover;"></td>
                    <td>${product.id}</td>
                    <td>${product.name}</td>
                    <td><f:formatNumber value="${product.price}" pattern="#,##0đ"/></td>
                    <td>${product.stock}</td>
                    <td>
                        <c:forEach var="cat" items="${category}">
                            <c:if test="${cat.id == product.catalog_id}">
                                ${cat.name}
                            </c:if>
                        </c:forEach>
                    </td>
                    <td>
                        <div class="material-tags">
                            <c:forEach var="m" items="${product.materials}">
                                <span class="material-badge">${m.name}</span>
                            </c:forEach>
                        </div>
                    </td>
                    <td>
                        <c:choose>
                            <c:when test="${sessionScope.user.role == 1}">
                                <i class="fa-solid fa-pen-to-square btn-edit"
                                   onclick="openModal('edit', ${product.id})"></i>
                            </c:when>
                            <c:otherwise>
                                <i class="fa-solid fa-pen-to-square btn-disabled"
                                   onclick="alert('Bạn không có quyền chỉnh sửa sản phẩm!');"></i>
                            </c:otherwise>
                        </c:choose>

                        <c:choose>
                            <c:when test="${sessionScope.user.role == 1}">
                                <form action="${pageContext.request.contextPath}/adminRemove" method="post"
                                      style="display: inline;">
                                    <input type="hidden" name="productId" value="${product.id}">
                                    <button type="submit" class="btn-delete"
                                            onclick="return confirm('Bạn có chắc chắn muốn xóa sản phẩm này?');">
                                        <i class="fa-solid fa-trash"></i>
                                    </button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <i class="fa-solid fa-trash btn-disabled"
                                   onclick="alert('Bạn không có quyền xoá sản phẩm!');"></i>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <div class="server-pagination" style="display: flex; justify-content: center; align-items: center; gap: 8px; margin: 30px 0;">
            <c:if test="${currentPage > 1}">
                <a href="javascript:void(0);" onclick="goToPage(${currentPage - 1})" class="btn-page">&lt;</a>
            </c:if>

            <c:forEach begin="1" end="${totalPages}" var="i">
                <c:choose>
                    <c:when test="${i == 1 || i == totalPages || (i >= currentPage - 1 && i <= currentPage + 1) || (currentPage == 1 && i <= 3) || (currentPage == totalPages && i >= totalPages - 2)}">
                        <a href="javascript:void(0);" onclick="goToPage(${i})" class="btn-page ${i == currentPage ? 'active' : ''}">
                                ${i}
                        </a>
                    </c:when>
                    <c:when test="${(i == 2 && currentPage > 3) || (i == totalPages - 1 && currentPage < totalPages - 2)}">
                        <span style="padding: 8px 4px; color: #7f8c8d; font-weight: bold; letter-spacing: 2px;">...</span>
                    </c:when>
                </c:choose>
            </c:forEach>

            <c:if test="${currentPage < totalPages}">
                <a href="javascript:void(0);" onclick="goToPage(${currentPage + 1})" class="btn-page">&gt;</a>
            </c:if>
        </div>
    </section>


    <!-- Modal Thêm Sản Phẩm -->
    <div id="addProductModal" class="modal">
        <div class="modal-content ">
            <span class="close" onclick="closeModal('addProductModal')">&times;</span>
            <h2>Thêm Sản Phẩm</h2>
            <form id="addProductForm" action="${pageContext.request.contextPath}/adminAdd" method="post"
                  enctype="multipart/form-data">
                <label for="addProductName">Tên Sản Phẩm:</label>
                <input type="text" id="addProductName" name="name" placeholder="Nhập tên sản phẩm" required>

                <label for="addPrice">Giá:</label>
                <input type="number" id="addPrice" name="price" placeholder="Nhập giá sản phẩm" required>

                <label for="addQuantity">Số Lượng:</label>
                <input type="number" id="addQuantity" name="quantity" placeholder="Nhập số lượng sản phẩm" required>
                <label>Chất liệu:</label>
                <div class="material-checkbox-group">
                    <c:forEach var="m" items="${materials}">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" name="materialIds" value="${m.id}" id="material_${m.id}">
                            <label class="form-check-label" for="material_${m.id}">${m.name}</label>
                        </div>
                    </c:forEach>
                </div>


                <label for="addCategory">Danh Mục:</label>
                <select id="addCategory" name="category" required>
                    <option value="">Chọn Danh Mục</option>
                    <c:forEach var="category" items="${category}">
                        <option value="${category.id}">${category.name}</option>
                    </c:forEach>
                </select>

                <label for="addDescription">Mô Tả:</label>
                <input type="text" id="addDescription" name="description" placeholder="Nhập mô tả sản phẩm" required>

                <label for="addImage">Ảnh sản phẩm:</label>
                <!-- Thẻ input ảnh -->
                <input type="file" name="image" accept="image/*" required><br>

                <!-- Ảnh xem trước -->
                <div style="position: relative; display: inline-block;">
                    <img id="addPreviewImage" src="" alt="Ảnh xem trước"
                         style="max-width: 150px; margin-top: 10px; border: 1px solid #ccc; border-radius: 8px;">

                    <!-- Nút x để xóa ảnh -->
                    <span id="removeImageBtn"
                          style="position: absolute; top: 0; right: 0; background: red; color: white; padding: 2px 6px; border-radius: 50%; cursor: pointer; font-weight: bold; display: none;">&times;</span>
                </div>

                <button type="submit" class="btn-save">Lưu Sản Phẩm</button>
            </form>
        </div>
    </div>

    <!-- Modal Sửa Sản Phẩm -->
    <div id="editProductModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal('editProductModal')">&times;</span>
            <h2>Sửa Sản Phẩm</h2>
            <form id="editProductForm" method="post" enctype="multipart/form-data">
                <input type="hidden" id="editProductId" name="productId">

                <label for="editProductName">Tên Sản Phẩm:</label>
                <input type="text" id="editProductName" name="name" required>

                <label for="editPrice">Giá:</label>
                <input type="number" id="editPrice" name="price" required>
                <label for="editCategory">Danh Mục:</label>
                <select id="editCategory" name="category" required>
                    <option value="">Chọn Danh Mục</option>
                    <c:forEach var="category" items="${category}">
                        <option value="${category.id}">${category.name}</option>
                    </c:forEach>
                </select>
                <label>Chất liệu:</label>
                <div class="material-checkbox-group" id="editMaterialGroup">
                    <c:forEach var="m" items="${materials}">
                        <div class="form-check">
                            <input class="form-check-input" type="checkbox" name="materialIds" value="${m.id}" id="edit_material_${m.id}">
                            <label class="form-check-label" for="edit_material_${m.id}">${m.name}</label>
                        </div>
                    </c:forEach>
                </div>



                <label for="editDescription">Mô Tả:</label>
                <input type="text" id="editDescription" name="description" required>

                <label for="editImage">Ảnh sản phẩm:</label>
                <input type="file" name="image" accept="image/*"><br>

                <!-- Hiển thị ảnh hiện tại -->
                <img id="editPreviewImage" src="" alt="Ảnh hiện tại" style="max-width: 150px; margin-top: 10px;">

                <!-- Lưu tên ảnh cũ -->
                <input type="hidden" name="oldImage" id="editOldImage">

                <button type="submit" class="btn-save">Cập Nhật Sản Phẩm</button>
            </form>
        </div>
    </div>
</div>
</body>
<script>
    document.addEventListener("DOMContentLoaded", function () {
        const fileInput = document.querySelector("input[name='image']");
        const previewImage = document.getElementById("addPreviewImage");
        const removeBtn = document.getElementById("removeImageBtn");

        fileInput.addEventListener("change", function () {
            const file = fileInput.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewImage.src = e.target.result;
                    removeBtn.style.display = "block";
                };
                reader.readAsDataURL(file);
            } else {
                previewImage.src = "";
                removeBtn.style.display = "none";
            }
        });

        removeBtn.addEventListener("click", function () {
            fileInput.value = ""; // Xóa file đã chọn
            previewImage.src = ""; // Xóa ảnh hiển thị
            removeBtn.style.display = "none";
        });
    });
</script>
<script>
    let currentSortBy = '${not empty sortBy ? sortBy : "id"}';
    let currentOrder = '${not empty order ? order : "DESC"}';
    let currentPage = ${currentPage != null ? currentPage : 1}; // Khởi tạo trang hiện tại
    //Hàm tìm kiếm
    function executeSearch() {
        currentPage = 1; // Reset về trang 1 khi tìm kiếm
        fetchFilteredData();
    }
    //  Khi click Sắp xếp
    function sortData(column) {
        if (currentSortBy === column) {
            currentOrder = (currentOrder === 'ASC') ? 'DESC' : 'ASC';
        } else {
            currentSortBy = column;
            currentOrder = 'ASC';
        }
        currentPage = 1; // Sắp xếp lại thì auto đưa về trang 1
        fetchFilteredData();
    }

    //  Khi chọn Lọc (Dropdown)
    function filterData() {
        currentPage = 1; // Lọc lại cũng đưa về trang 1
        fetchFilteredData();
    }

    //  Khi click Phân trang (Tránh load lại trang)
    function goToPage(page) {
        currentPage = page;
        fetchFilteredData();
    }

    // Gọi AJAX lấy dữ liệu ngầm
    function fetchFilteredData() {
        const categoryId = document.getElementById("filterCategory").value;
        const materialId = document.getElementById("filterMaterial").value;
        const searchKeyword = document.getElementById("searchInput").value.trim();

        const url = new URL(window.contextPath + '/adminProducts', window.location.origin);
        if (categoryId) url.searchParams.set("category", categoryId);
        if (materialId) url.searchParams.set("material", materialId);
        if (searchKeyword) url.searchParams.set("search", searchKeyword); // Truyền từ khóa tìm kiếm về Servlet

        url.searchParams.set("sortBy", currentSortBy);
        url.searchParams.set("order", currentOrder);
        url.searchParams.set("page", currentPage); // Truyền thêm page vào URL

        fetch(url)
            .then(response => response.text())
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, "text/html");

                // Cập nhật lại Bảng và Thanh phân trang
                document.querySelector(".product-table tbody").innerHTML = doc.querySelector(".product-table tbody").innerHTML;
                document.querySelector(".server-pagination").innerHTML = doc.querySelector(".server-pagination").innerHTML;

                // Lưu lại URL lên trình duyệt cho đẹp và giữ history
                window.history.pushState({}, '', url);
                updateSortIcons();
            })
            .catch(err => console.error("Lỗi AJAX:", err));
    }

    function updateSortIcons() {
        document.querySelectorAll('th.sortable').forEach(th => {
            th.classList.remove('active-sort');
            th.querySelector('i').className = 'fas fa-sort';

            if (th.getAttribute('data-col') === currentSortBy) {
                th.classList.add('active-sort');
                th.querySelector('i').className = (currentOrder === 'ASC') ? 'fas fa-sort-up' : 'fas fa-sort-down';
            }
        });
    }

    document.addEventListener("DOMContentLoaded", updateSortIcons);
</script>
</html>
