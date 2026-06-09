<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Quản Lý Đánh Giá - Admin</title>
    <meta charset="UTF-8">
    <link href="https://fonts.googleapis.com/css2?family=Roboto&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <script src="https://kit.fontawesome.com/a076d05399.js" crossorigin="anonymous"></script>
    <style>
        .pagination-container {
            display: flex;
            justify-content: center;
            align-items: center;
            margin-top: 30px;
            margin-bottom: 20px;
            flex-direction: column;
            gap: 15px;
        }
        .page-info { font-weight: bold; color: #555; font-size: 14px; }
        .pagination {
            display: flex;
            list-style: none;
            padding: 0;
            gap: 8px;
            margin: 0;
        }
        .pagination li a, .pagination li span {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 35px;
            height: 35px;
            border: 1px solid #dee2e6;
            background: #fff;
            cursor: pointer;
            text-decoration: none;
            color: #333;
            border-radius: 4px;
            font-weight: 500;
            transition: all 0.2s ease-in-out;
        }
        .pagination li a:hover {
            background-color: #e9ecef;
        }
        .pagination li.active a {
            background-color: #17a2b8;
            color: white;
            border-color: #17a2b8;
            cursor: default;
        }
        .pagination li.disabled span {
            background: transparent;
            border: none;
            cursor: default;
            color: #6c757d;
        }
    </style>
</head>
<body>

<%@include file="ad-menu.jsp" %>

<div class="main-content">
    <header>
        <h1>Quản Lý Đánh Giá Sản Phẩm</h1>
    </header>

    <section class="comment-management">
        <table class="product-table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Sản phẩm</th>
                <th>Người dùng</th>
                <th>Đánh giá</th>
                <th>Nội dung</th>
                <th>Thời gian</th>
                <th>Hành động</th>
            </tr>
            </thead>
            <tbody id="comment-table-body">
            <tr><td colspan="7" style="text-align:center;">Đang tải dữ liệu...</td></tr>
            </tbody>
        </table>

        <div class="pagination-container">
            <div class="page-info" id="page-info">
            </div>
            <ul class="pagination" id="pagination-controls">
            </ul>
        </div>
    </section>
</div>

<script>
    const contextPath = '${pageContext.request.contextPath}';
    const isAdmin = ${sessionScope.user.role == 1 ? 'true' : 'false'};
    let currentGlobalPage = 1;

    // Tự động tải trang 1 khi màn hình vừa mở lên
    document.addEventListener("DOMContentLoaded", () => {
        loadComments(1);
    });

    // Lấy dữ liệu và hiển thị
    function loadComments(page) {
        currentGlobalPage = page;
        fetch(`\${contextPath}/adminComments?action=list_ajax&page=\${page}`)
            .then(response => response.json())
            .then(data => {
                renderTable(data.comments);
                renderPagination(data.currentPage, data.totalPages, data.totalComments);
            })
            .catch(err => console.error("Lỗi khi tải comment:", err));
    }

    // vẽ danh sách vào bảng
    function renderTable(comments) {
        const tbody = document.getElementById("comment-table-body");
        tbody.innerHTML = "";

        if (comments.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">Không có đánh giá nào.</td></tr>`;
            return;
        }

        comments.forEach(cmt => {
            let deleteBtn = "";
            if (isAdmin) {
                deleteBtn = `
                    <button class="btn-delete" onclick="deleteComment(\${cmt.id})" style="cursor:pointer; padding:5px 10px; background:#dc3545; color:white; border:none; border-radius:4px;">
                        <i class="fa-solid fa-trash"></i> Xoá
                    </button>
                `;
            }

            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>\${cmt.id}</td>
                <td>\${cmt.productName || 'SP #' + cmt.productId}</td>
                <td>\${cmt.userName || 'Ẩn danh'}</td>
                <td>\${cmt.rating} <i class="fa-solid fa-star" style="color: #f5c518;"></i></td>
                <td>\${cmt.content}</td>
                <td>\${cmt.createdAt}</td>
                <td>\${deleteBtn}</td>
            `;
            tbody.appendChild(tr);
        });
    }

    // điều hướng phân trang
    function renderPagination(currentPage, totalPages, totalComments) {
        // Vẫn giữ lại text thông tin tổng (nếu bạn muốn hiển thị)
        document.getElementById("page-info").innerText =
            `Trang \${currentPage}/\${totalPages} – \${totalComments} đánh giá`;

        const ul = document.getElementById("pagination-controls");
        ul.innerHTML = "";

        if (totalPages <= 1) return;

        if (currentPage > 1) {
            ul.innerHTML += `<li><a onclick="loadComments(\${currentPage - 1})"><</a></li>`;
        }

        // Logic hiển thị dấu "..." nếu có quá nhiều trang
        let startPage = Math.max(1, currentPage - 1);
        let endPage = Math.min(totalPages, currentPage + 1);

        if (currentPage === 1) {
            endPage = Math.min(totalPages, 3);
        } else if (currentPage === totalPages) {
            startPage = Math.max(1, totalPages - 2);
        }

        if (startPage > 1) {
            ul.innerHTML += `<li><a onclick="loadComments(1)">1</a></li>`;
            if (startPage > 2) {
                ul.innerHTML += `<li class="disabled"><span>...</span></li>`;
            }
        }

        // Các số trang ở giữa
        for (let i = startPage; i <= endPage; i++) {
            const activeClass = (i === currentPage) ? "active" : "";
            ul.innerHTML += `<li class="\${activeClass}"><a onclick="loadComments(\${i})">\${i}</a></li>`;
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) {
                ul.innerHTML += `<li class="disabled"><span>...</span></li>`;
            }
            ul.innerHTML += `<li><a onclick="loadComments(\${totalPages})">\${totalPages}</a></li>`;
        }

        // Nút ">" (Sau)
        if (currentPage < totalPages) {
            ul.innerHTML += `<li><a onclick="loadComments(\${currentPage + 1})">></a></li>`;
        }
    }

    // Xóa comment bằng AJAX
    function deleteComment(id) {
        if (confirm("Bạn có chắc muốn xoá đánh giá này?")) {
            fetch(`\${contextPath}/adminComments`, {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: `action=delete&id=\${id}&ajax=true`
            })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        alert("Xóa thành công!");
                        // Xóa xong thì load lại đúng trang hiện tại
                        loadComments(currentGlobalPage);
                    } else {
                        alert("Có lỗi xảy ra khi xóa.");
                    }
                })
                .catch(err => console.error("Lỗi xóa comment:", err));
        }
    }
</script>

</body>
</html>