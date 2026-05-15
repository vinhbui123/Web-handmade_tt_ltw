/**
 * Search Autocomplete & Suggestion
 * - Debounce: chờ 300ms sau khi user ngừng gõ mới gửi request
 * - AJAX: gọi /api/search-suggestions để lấy gợi ý
 * - Keyboard navigation: Arrow Up/Down để chọn, Enter để submit, Escape để đóng
 */
(function () {
    'use strict';

    const DEBOUNCE_DELAY = 300; // ms
    const MIN_KEYWORD_LENGTH = 1; // số ký tự tối thiểu để bắt đầu gợi ý

    let debounceTimer = null;
    let activeIndex = -1; // index item đang được highlight bằng bàn phím
    let currentSuggestions = [];

    document.addEventListener('DOMContentLoaded', function () {
        const searchForm = document.getElementById('searchForm');
        if (!searchForm) return;

        const searchInput = searchForm.querySelector('input[name="keyword"]');
        if (!searchInput) return;

        // Tạo container gợi ý
        const suggestionBox = document.createElement('div');
        suggestionBox.id = 'search-suggestions';
        suggestionBox.className = 'search-suggestions';

        // Gắn vào sau search-box (parent của form)
        const searchBox = searchForm.closest('.search-box') || searchForm.parentElement;
        searchBox.style.position = 'relative'; // đảm bảo dropdown định vị đúng
        searchBox.appendChild(suggestionBox);

        // === EVENT: input (debounced) ===
        searchInput.addEventListener('input', function () {
            const keyword = this.value.trim();
            activeIndex = -1;

            if (debounceTimer) clearTimeout(debounceTimer);

            if (keyword.length < MIN_KEYWORD_LENGTH) {
                hideSuggestions(suggestionBox);
                return;
            }

            debounceTimer = setTimeout(function () {
                fetchSuggestions(keyword, suggestionBox, searchInput);
            }, DEBOUNCE_DELAY);
        });

        // === EVENT: keyboard navigation ===
        searchInput.addEventListener('keydown', function (e) {
            const items = suggestionBox.querySelectorAll('.suggestion-item');
            if (!items.length) return;

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                activeIndex = Math.min(activeIndex + 1, items.length - 1);
                updateActiveItem(items);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                activeIndex = Math.max(activeIndex - 1, -1);
                updateActiveItem(items);
                // Nếu activeIndex == -1, quay lại input ban đầu
                if (activeIndex === -1) {
                    searchInput.value = searchInput.dataset.originalValue || '';
                }
            } else if (e.key === 'Enter' && activeIndex >= 0) {
                e.preventDefault();
                const selectedItem = items[activeIndex];
                if (selectedItem) {
                    window.location.href = selectedItem.dataset.href;
                }
            } else if (e.key === 'Escape') {
                hideSuggestions(suggestionBox);
                activeIndex = -1;
            }
        });

        // === EVENT: click bên ngoài để đóng dropdown ===
        document.addEventListener('click', function (e) {
            if (!searchBox.contains(e.target)) {
                hideSuggestions(suggestionBox);
            }
        });

        // === EVENT: focus lại thì hiện nếu có data ===
        searchInput.addEventListener('focus', function () {
            if (currentSuggestions.length > 0 && this.value.trim().length >= MIN_KEYWORD_LENGTH) {
                suggestionBox.style.display = 'block';
            }
        });
    });

    /**
     * Gọi API lấy gợi ý sản phẩm
     */
    function fetchSuggestions(keyword, suggestionBox, searchInput) {
        const contextPath = window.contextPath || '';
        const url = contextPath + '/api/search-suggestions?keyword=' + encodeURIComponent(keyword);

        // Lưu giá trị gốc để phục hồi khi nhấn ArrowUp quay lại
        searchInput.dataset.originalValue = keyword;

        fetch(url)
            .then(function (response) {
                if (!response.ok) throw new Error('Network error');
                return response.json();
            })
            .then(function (products) {
                currentSuggestions = products;
                renderSuggestions(products, suggestionBox, keyword, searchInput);
            })
            .catch(function (err) {
                console.error('Lỗi fetch gợi ý:', err);
                hideSuggestions(suggestionBox);
            });
    }

    /**
     * Render dropdown gợi ý
     */
    function renderSuggestions(products, suggestionBox, keyword, searchInput) {
        activeIndex = -1;
        suggestionBox.innerHTML = '';

        if (!products || products.length === 0) {
            // Hiện thông báo "không tìm thấy"
            suggestionBox.innerHTML =
                '<div class="suggestion-empty">' +
                '<i class="fas fa-search"></i> Không tìm thấy sản phẩm nào cho "<strong>' +
                escapeHtml(keyword) + '</strong>"' +
                '</div>';
            suggestionBox.style.display = 'block';
            return;
        }

        const contextPath = window.contextPath || '';

        products.forEach(function (product, index) {
            var item = document.createElement('div');
            item.className = 'suggestion-item';
            item.dataset.href = contextPath + '/product-detail?id=' + product.id;
            item.dataset.index = index;

            // Highlight từ khoá trong tên sản phẩm
            var highlightedName = highlightKeyword(product.name, keyword);

            // Format giá
            var priceHtml = '';
            if (product.discount > 0) {
                priceHtml =
                    '<span class="suggestion-price">' + formatPrice(product.finalPrice) + '</span>' +
                    '<span class="suggestion-price-old">' + formatPrice(product.price) + '</span>' +
                    '<span class="suggestion-discount">-' + product.discount + '%</span>';
            } else {
                priceHtml = '<span class="suggestion-price">' + formatPrice(product.price) + '</span>';
            }

            item.innerHTML =
                '<div class="suggestion-img">' +
                '<img src="' + escapeHtml(product.img) + '" alt="' + escapeHtml(product.name) + '" loading="lazy">' +
                '</div>' +
                '<div class="suggestion-info">' +
                '<div class="suggestion-name">' + highlightedName + '</div>' +
                '<div class="suggestion-price-row">' + priceHtml + '</div>' +
                '</div>';

            // Click vào item → chuyển trang
            item.addEventListener('click', function () {
                window.location.href = this.dataset.href;
            });

            // Hover → update active
            item.addEventListener('mouseenter', function () {
                activeIndex = parseInt(this.dataset.index);
                var allItems = suggestionBox.querySelectorAll('.suggestion-item');
                updateActiveItem(allItems);
            });

            suggestionBox.appendChild(item);
        });

        // Thêm nút "Xem tất cả kết quả"
        var viewAll = document.createElement('div');
        viewAll.className = 'suggestion-view-all';
        viewAll.innerHTML = '<i class="fas fa-search"></i> Xem tất cả kết quả cho "<strong>' + escapeHtml(keyword) + '</strong>"';
        viewAll.addEventListener('click', function () {
            searchInput.form.submit();
        });
        suggestionBox.appendChild(viewAll);

        suggestionBox.style.display = 'block';
    }

    /**
     * Ẩn dropdown
     */
    function hideSuggestions(box) {
        box.style.display = 'none';
    }

    /**
     * Cập nhật item đang active (keyboard nav)
     */
    function updateActiveItem(items) {
        items.forEach(function (item, i) {
            if (i === activeIndex) {
                item.classList.add('active');
                item.scrollIntoView({ block: 'nearest' });
            } else {
                item.classList.remove('active');
            }
        });
    }

    /**
     * Highlight keyword trong tên sản phẩm (case-insensitive)
     */
    function highlightKeyword(text, keyword) {
        if (!keyword) return escapeHtml(text);
        // Escape regex special chars
        var escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        var regex = new RegExp('(' + escapedKeyword + ')', 'gi');
        return escapeHtml(text).replace(regex, '<mark>$1</mark>');
    }

    /**
     * Format giá tiền VND
     */
    function formatPrice(amount) {
        return amount.toLocaleString('vi-VN') + 'đ';
    }

    /**
     * Escape HTML để tránh XSS
     */
    function escapeHtml(text) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(text || ''));
        return div.innerHTML;
    }

})();
