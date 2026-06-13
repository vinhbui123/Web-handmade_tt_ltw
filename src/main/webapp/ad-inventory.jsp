<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Quản Lý Xuất Nhập Sản Phẩm - Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <script src="${pageContext.request.contextPath}/js/admin.js"></script>
    <script>
        window.contextPath = "<%= request.getContextPath() %>";
    </script>
    <script src="${pageContext.request.contextPath}/js/product.js"></script>

    <style>
        th.sortable { cursor: pointer; transition: color 0.3s; position: relative; user-select: none; white-space: nowrap; }
        th.sortable:hover { color: #f39c12; }
        th.active-sort { color: #e74c3c; font-weight: bold; }
        th select { margin-top: 5px; padding: 4px; border-radius: 4px; border: 1px solid #ccc; width: 100%; font-size: 13px; cursor: pointer; outline: none; color: #333; }
        .product-table th i { margin-left: 5px; vertical-align: middle; }
    </style>
</head>
<body>
<%@include file="ad-menu.jsp" %>
<div class="main-content">
    <header>
        <h1>Quản Lý Xuất Nhập Sản Phẩm</h1>
    </header>

    <c:if test="${not empty message}">
        <div class="alert ${messageType}">
            <p>${message}</p>
        </div>
    </c:if>

    <div class="top-toolbar" style="display: flex; justify-content: flex-start; align-items: center; gap: 20px; margin-bottom: 0px; margin-top: 10px;">
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
    </div>

    <div style="display: flex; gap: 20px; flex-wrap: wrap; margin-bottom: 20px; margin-top: 15px;">
        <c:if test="${sessionScope.user.role == 1 || sessionScope.user.role == 2 || sessionScope.user.role == 3}">
            <form action="${pageContext.request.contextPath}/adminInventory" method="post" class="inventory-form" style="margin: 0;">
                <input type="hidden" name="type" value="import">
                <label><strong>Nhập hàng</strong></label><br>
                <input type="number" name="productId" placeholder="ID sản phẩm" required>
                <input type="number" name="quantity" placeholder="Số lượng nhập" min="1" required>
                <button type="submit" class="btn-import">Nhập hàng</button>
            </form>
        </c:if>

        <c:if test="${sessionScope.user.role == 1 || sessionScope.user.role == 2 || sessionScope.user.role == 3}">
            <form action="${pageContext.request.contextPath}/adminInventory" method="post" class="inventory-form" style="margin: 0;">
                <input type="hidden" name="type" value="export">
                <label><strong>Xuất hàng</strong></label><br>
                <input type="number" name="productId" placeholder="ID sản phẩm" required>
                <input type="number" name="quantity" placeholder="Số lượng xuất" min="1" required>
                <button type="submit" class="btn-export">Xuất hàng</button>
            </form>
        </c:if>
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
                <th>Hành Động</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="product" items="${products}">
                <tr>
                    <td><img src="${product.img}" alt="${product.name}" style="width: 50px; height: 50px; object-fit: cover;"></td>
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
                        <c:choose>
                            <c:when test="${sessionScope.user.role == 1 || sessionScope.user.role == 2 || sessionScope.user.role == 3}">
                                <button class="btn-import" onclick="openImportModal(${product.id}, '${product.name}')">Nhập</button>
                            </c:when>
                            <c:otherwise>
                                <button class="btn-disabled" onclick="alert('Bạn không có quyền nhập hàng!')">Nhập</button>
                            </c:otherwise>
                        </c:choose>

                        <c:choose>
                            <c:when test="${sessionScope.user.role == 1 || sessionScope.user.role == 2 || sessionScope.user.role == 3}">
                                <button class="btn-export" onclick="openExportModal(${product.id}, '${product.name}')">Xuất</button>
                            </c:when>
                            <c:otherwise>
                                <button class="btn-disabled" onclick="alert('Bạn không có quyền xuất hàng!')">Xuất</button>
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
                        <a href="javascript:void(0);" onclick="goToPage(${i})" class="btn-page ${i == currentPage ? 'active' : ''}">${i}</a>
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

    <div id="importModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeImportModal()">&times;</span>
            <h2>Nhập Hàng</h2>
            <form id="importForm" action="${pageContext.request.contextPath}/adminInventory" method="post">
                <input type="hidden" id="importProductId" name="productId">
                <input type="hidden" name="type" value="import">
                <label for="importProductName">Tên Sản Phẩm:</label>
                <input type="text" id="importProductName" readonly>
                <label for="importQuantity">Số Lượng Nhập:</label>
                <input type="number" id="importQuantity" name="quantity" min="1" required>
                <button type="submit" class="btn-save">Nhập Hàng</button>
            </form>
        </div>
    </div>
    <div id="exportModal" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeExportModal()">&times;</span>
            <h2>Xuất Hàng</h2>
            <form id="exportForm" action="${pageContext.request.contextPath}/adminInventory" method="post">
                <input type="hidden" id="exportProductId" name="productId">
                <input type="hidden" name="type" value="export">
                <label for="exportProductName">Tên Sản Phẩm:</label>
                <input type="text" id="exportProductName" readonly>
                <label for="exportQuantity">Số Lượng Xuất:</label>
                <input type="number" id="exportQuantity" name="quantity" min="1" required>
                <button type="submit" class="btn-save">Xuất Hàng</button>
            </form>
        </div>
    </div>
    <hr style="margin: 40px 0;">
</div>
</body>

<script>
    let currentSortBy = '${not empty sortBy ? sortBy : "id"}';
    let currentOrder = '${not empty order ? order : "DESC"}';
    let currentPage = ${currentPage != null ? currentPage : 1};

    function executeSearch() { currentPage = 1; fetchFilteredData(); }
    function filterData() { currentPage = 1; fetchFilteredData(); }
    function goToPage(page) { currentPage = page; fetchFilteredData(); }
    function sortData(column) {
        if (currentSortBy === column) {
            currentOrder = (currentOrder === 'ASC') ? 'DESC' : 'ASC';
        } else {
            currentSortBy = column;
            currentOrder = 'ASC';
        }
        currentPage = 1;
        fetchFilteredData();
    }

    function fetchFilteredData() {
        const categoryId = document.getElementById("filterCategory").value;
        const searchKeyword = document.getElementById("searchInput").value.trim();

        const url = new URL(window.contextPath + '/adminInventory', window.location.origin);
        if (categoryId) url.searchParams.set("category", categoryId);
        if (searchKeyword) url.searchParams.set("search", searchKeyword);
        url.searchParams.set("sortBy", currentSortBy);
        url.searchParams.set("order", currentOrder);
        url.searchParams.set("page", currentPage);

        fetch(url)
            .then(response => response.text())
            .then(html => {
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, "text/html");

                document.querySelector(".product-table tbody").innerHTML = doc.querySelector(".product-table tbody").innerHTML;
                document.querySelector(".server-pagination").innerHTML = doc.querySelector(".server-pagination").innerHTML;

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