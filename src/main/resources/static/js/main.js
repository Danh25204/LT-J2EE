/**
 * Antique Warehouse - Main JavaScript
 * Các hàm JS dùng chung cho toàn bộ ứng dụng
 */

document.addEventListener('DOMContentLoaded', function () {

    // ===== SIDEBAR TOGGLE (mobile) =====
    initSidebarToggle();

    // ===== AUTO DISMISS ALERTS =====
    autoDismissAlerts(5000); // 5 giây

    // ===== ĐỒNG HỒ TOPNAV =====
    updateTopnavTime();
    setInterval(updateTopnavTime, 1000);

    // ===== CONFIRM TRƯỚC KHI XÓA =====
    // Dùng data-confirm="nội dung" trên thẻ <form> hoặc <a>
    initDeleteConfirm();

    // ===== LOADING KHI SUBMIT FORM =====
    initFormLoadingState();

});

/**
 * Sidebar toggle cho mobile
 * Bật/tắt class 'show' trên #sidebar và overlay
 */
function initSidebarToggle() {
    const toggleBtn = document.getElementById('sidebarToggle');
    const sidebar   = document.getElementById('sidebar');
    if (!toggleBtn || !sidebar) return;

    // Tạo overlay (lớp phủ mờ phía sau sidebar)
    let overlay = document.querySelector('.sidebar-overlay');
    if (!overlay) {
        overlay = document.createElement('div');
        overlay.className = 'sidebar-overlay';
        document.body.appendChild(overlay);
    }

    // Nút toggle
    toggleBtn.addEventListener('click', function () {
        sidebar.classList.toggle('show');
        overlay.classList.toggle('show');
    });

    // Click vào overlay để đóng sidebar
    overlay.addEventListener('click', function () {
        sidebar.classList.remove('show');
        overlay.classList.remove('show');
    });
}

/**
 * Tự động đóng alert sau một khoảng thời gian
 * @param {number} delay - thời gian delay (ms)
 */
function autoDismissAlerts(delay) {
    const alerts = document.querySelectorAll('.alert.alert-dismissible');
    alerts.forEach(function (alertEl) {
        setTimeout(function () {
            if (typeof bootstrap !== 'undefined') {
                const bsAlert = bootstrap.Alert.getOrCreateInstance(alertEl);
                bsAlert.close();
            } else {
                alertEl.remove();
            }
        }, delay);
    });
}

/**
 * Cập nhật thời gian thực trên topnav
 */
function updateTopnavTime() {
    const el = document.getElementById('currentTime');
    if (!el) return;
    const now = new Date();
    el.textContent = now.toLocaleTimeString('vi-VN', {
        hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
}

/**
 * Confirm trước khi thực hiện hành động nguy hiểm
 * Cách dùng: thêm attribute data-confirm="Bạn có chắc?" vào <form> hoặc <button>
 */
function initDeleteConfirm() {
    // Bắt sự kiện submit trên các form có data-confirm
    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            const msg = form.getAttribute('data-confirm') || 'Bạn có chắc muốn thực hiện thao tác này?';
            if (!confirm(msg)) {
                e.preventDefault();
            }
        });
    });
}

/**
 * Thêm hiệu ứng loading khi submit form
 * Tránh người dùng click nhiều lần
 */
function initFormLoadingState() {
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function () {
            const submitBtns = form.querySelectorAll('button[type="submit"]');
            submitBtns.forEach(function (btn) {
                btn.classList.add('btn-loading');
                btn.disabled = true;
                // Khôi phục sau 5s để tránh bị kẹt nếu có lỗi
                setTimeout(function () {
                    btn.classList.remove('btn-loading');
                    btn.disabled = false;
                }, 5000);
            });
        });
    });
}

// ===== UTILITY FUNCTIONS (dùng trong các template) =====

/**
 * Preview ảnh ngay khi chọn file (Phase 4 - Upload ảnh đồ cổ)
 * Cách dùng HTML: <input type="file" onchange="previewImage(this, 'previewId')">
 * @param {HTMLInputElement} input - input[type="file"]
 * @param {string} previewId - id của thẻ <img> preview
 */
function previewImage(input, previewId) {
    const preview = document.getElementById(previewId);
    if (!preview || !input.files || !input.files[0]) return;

    // Kiểm tra định dạng cho phép
    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    if (!allowedTypes.includes(input.files[0].type)) {
        alert('Chỉ chấp nhận file ảnh: JPG, PNG, WEBP, GIF');
        input.value = '';
        return;
    }

    const reader = new FileReader();
    reader.onload = function (e) {
        preview.src = e.target.result;
        preview.style.display = 'block';
    };
    reader.readAsDataURL(input.files[0]);
}

/**
 * Format số tiền theo định dạng VNĐ
 * @param {number} amount
 * @returns {string}
 */
function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        maximumFractionDigits: 0
    }).format(amount);
}

/**
 * Format số với dấu phân cách nghìn
 * @param {number} num
 * @returns {string}
 */
function formatNumber(num) {
    return new Intl.NumberFormat('vi-VN').format(num);
}
