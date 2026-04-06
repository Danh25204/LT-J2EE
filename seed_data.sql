-- ============================================================
-- SEED DATA - Antique Warehouse (IDEMPOTENT - có thể chạy lại nhiều lần)
-- ============================================================
-- HƯỚNG DẪN:
-- 1. Khởi động ứng dụng 1 lần để Hibernate tạo bảng và
--    DataInitializer tạo roles + users (admin/nhanvien1)
-- 2. Tắt ứng dụng
-- 3. Chạy script này:
--      mysql -u root antique_warehouse < seed_data.sql
-- 4. Khởi động lại ứng dụng → log in với admin/admin123
--
-- Có thể chạy lại bất kỳ lúc nào - script tự xóa data cũ trước.
-- ============================================================

USE antique_warehouse;

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 0. XÓA DATA CŨ + RESET AUTO_INCREMENT
-- Thứ tự: child tables trước, parent tables sau
-- ============================================================
DELETE FROM warehouse_activities;
DELETE FROM antique_inspections;
DELETE FROM export_receipt_details;
DELETE FROM import_receipt_details;
DELETE FROM export_receipts;
DELETE FROM import_receipts;
DELETE FROM inventory;
DELETE FROM antiques;
DELETE FROM categories;

ALTER TABLE warehouse_activities   AUTO_INCREMENT = 1;
ALTER TABLE antique_inspections    AUTO_INCREMENT = 1;
ALTER TABLE export_receipt_details AUTO_INCREMENT = 1;
ALTER TABLE import_receipt_details AUTO_INCREMENT = 1;
ALTER TABLE export_receipts        AUTO_INCREMENT = 1;
ALTER TABLE import_receipts        AUTO_INCREMENT = 1;
ALTER TABLE inventory              AUTO_INCREMENT = 1;
ALTER TABLE antiques               AUTO_INCREMENT = 1;
ALTER TABLE categories             AUTO_INCREMENT = 1;

-- ============================================================
-- 1. CATEGORIES (danh mục loại đồ cổ)
-- ============================================================
INSERT INTO categories (ma_loai, ten_loai, mo_ta, created_at) VALUES
('GOC', 'Gốm sứ',          'Bát, bình, chén, lọ hoa bằng gốm và sứ cổ từ các triều đại Việt Nam và Trung Quốc', NOW()),
('KIM', 'Kim loại',        'Đồ đồng, đồ bạc, tượng đồng, đồ dùng kim loại cổ', NOW()),
('GO',  'Gỗ & Nội thất',   'Bàn ghế, tủ, sập gụ, tranh khắc gỗ từ thời phong kiến', NOW()),
('GIA', 'Giấy & Tranh',    'Thư pháp, tranh dân gian, tài liệu lịch sử, ấn triện', NOW()),
('DA',  'Đá & Đất nung',   'Tượng đá, phù điêu, gạch cổ, vật dụng bằng đất nung', NOW()),
('VAI', 'Vải & Trang phục','Áo cung đình, khăn đóng, trang phục triều đại, thêu thùa cổ', NOW());

-- ============================================================
-- 2. ANTIQUES (dùng subquery để lấy category_id theo ma_loai)
-- ============================================================
INSERT INTO antiques (ma_do_co, ten_do_co, category_id, nam_san_xuat, trieu_dai, xuat_xu, chat_lieu, kich_thuoc, tinh_trang, mo_ta, gia_nhap, gia_ban_du_kien, so_luong_mac_dinh, anh_chinh, created_at, updated_at) VALUES
-- Gốm sứ
('DC-2024-001', 'Bình gốm men ngọc triều Lý',      (SELECT id FROM categories WHERE ma_loai='GOC'), 1150, 'Triều Lý',     'Bắc Ninh',  'Gốm men ngọc',  'Cao 28cm, ĐK 14cm', 'TOT', 'Bình gốm men ngọc thời Lý, hoa văn sen nổi, còn nguyên vẹn',          45000000,  120000000, 1, NULL, NOW(), NOW()),
('DC-2024-002', 'Chén bát tràng thế kỷ XVIII',     (SELECT id FROM categories WHERE ma_loai='GOC'), 1750, 'Hậu Lê',      'Hà Nội',    'Gốm hoa lam',   'Cao 6cm, ĐK 12cm',  'KHA', 'Chén gốm hoa lam vẽ tay, hình cá chép, gốc Bát Tràng',               5000000,   15000000, 1, NULL, NOW(), NOW()),
('DC-2024-003', 'Lọ hoa sứ Trung Hoa - Thanh Hoa', (SELECT id FROM categories WHERE ma_loai='GOC'), 1820, 'Nhà Thanh',   'Trung Quốc','Sứ hoa lam',    'Cao 35cm, ĐK 18cm', 'TOT', 'Lọ sứ hoa lam vẽ tay, hình sơn thủy, thời vua Đạo Quang',           80000000,  200000000, 1, NULL, NOW(), NOW()),
-- Kim loại
('DC-2024-004', 'Tượng phật đồng thau tọa thiền',  (SELECT id FROM categories WHERE ma_loai='KIM'), 1900, 'Đầu TK XX',   'Huế',       'Đồng thau',     'Cao 22cm',          'TOT', 'Tượng phật Thích Ca tọa thiền, đồng thau nguyên khối, còn sắc nét', 25000000,   70000000, 1, NULL, NOW(), NOW()),
('DC-2024-005', 'Đồng hồ đồng phong cách Pháp',    (SELECT id FROM categories WHERE ma_loai='KIM'), 1890, 'Pháp thuộc',  'Hà Nội',    'Đồng mạ vàng',  'Cao 30cm, R 20cm',  'KHA', 'Đồng hồ để bàn phong cách Art Nouveau, máy còn chạy',               18000000,   45000000, 1, NULL, NOW(), NOW()),
('DC-2024-006', 'Kiếm thép triều Nguyễn',           (SELECT id FROM categories WHERE ma_loai='KIM'), 1830, 'Triều Nguyễn','Huế',       'Thép & đồng',   'Dài 88cm',          'TOT', 'Kiếm ngự lâm quân, chuôi đồng chạm rồng, bao gỗ bọc da',           55000000,  150000000, 1, NULL, NOW(), NOW()),
-- Gỗ & Nội thất
('DC-2024-007', 'Sập gụ chân quỳ thế kỷ XIX',      (SELECT id FROM categories WHERE ma_loai='GO'),  1860, 'Triều Nguyễn','Hà Nội',    'Gỗ gụ',         'D220xR120xC40cm',   'TOT', 'Sập gụ nguyên khối, chân quỳ chạm hoa sen, không mọt',             120000000,  350000000, 1, NULL, NOW(), NOW()),
('DC-2024-008', 'Tranh khắc gỗ Đông Hồ',           (SELECT id FROM categories WHERE ma_loai='GO'),  1920, 'Đầu TK XX',   'Bắc Ninh',  'Gỗ & giấy dó',  '40x60cm',           'KHA', 'Tranh Đông Hồ in khắc gỗ, hình Lý Ngư Vọng Nguyệt, còn màu sắc',   3000000,    8000000, 1, NULL, NOW(), NOW()),
-- Giấy & Tranh
('DC-2024-009', 'Thư pháp chữ Hán trên lụa',       (SELECT id FROM categories WHERE ma_loai='GIA'), 1880, 'Hậu Nguyễn',  'Huế',       'Mực tàu & lụa', '80x40cm',           'TOT', 'Bức thư pháp chữ Hán - Phúc Lộc Thọ, khung gỗ gụ chạm',           15000000,   40000000, 1, NULL, NOW(), NOW()),
-- Đá & Đất nung
('DC-2024-010', 'Nghiên mực đá mài Trung Quốc',    (SELECT id FROM categories WHERE ma_loai='DA'),  1800, 'Nhà Thanh',   'Trung Quốc','Đá tự nhiên',   '25x15x4cm',         'TOT', 'Nghiên mực đá xanh Đoan Khê, chạm khắc hoa cúc, còn nguyên',       12000000,   30000000, 1, NULL, NOW(), NOW()),
('DC-2024-011', 'Tượng thạch anh Quan Âm',          (SELECT id FROM categories WHERE ma_loai='DA'),  1950, 'Đầu TK XX',   'Quảng Ninh','Thạch anh',     'Cao 45cm',          'TOT', 'Tượng Phật Bà Quan Âm đứng, thạch anh trắng trong, dáng mỹ nghệ', 35000000,   90000000, 1, NULL, NOW(), NOW()),
-- Vải & Trang phục
('DC-2024-012', 'Áo long bào thêu rồng hoàng gia', (SELECT id FROM categories WHERE ma_loai='VAI'), 1910, 'Triều Nguyễn','Huế',       'Lụa & chỉ vàng','Co M-L hoang toc',   'KHA', 'Áo long bào màu vàng thêu chỉ vàng hình rồng 5 móng, lụa tơ tằm',200000000,  500000000, 1, NULL, NOW(), NOW());

-- ============================================================
-- 3. INVENTORY (dùng subquery lấy antique_id theo ma_do_co)
-- ============================================================
INSERT INTO inventory (antique_id, so_luong_ton, vi_tri_luu_tru, ghi_chu, updated_at) VALUES
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-001'), 0, 'Tủ kính A - Tầng 1 - Ô 1', 'Bọc bông chống va đập', NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-002'), 0, 'Tủ kính A - Tầng 1 - Ô 2', NULL, NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-003'), 0, 'Tủ kính A - Tầng 2 - Ô 1', 'Nhập từ đại lý Hoa Phát', NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-004'), 0, 'Tủ kính B - Tầng 1 - Ô 1', NULL, NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-005'), 0, 'Tủ kính B - Tầng 1 - Ô 2', 'Còn máy chạy tốt', NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-006'), 0, 'Kệ vũ khí - Ô trung tâm',   'Kèm bao da gốc', NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-007'), 0, 'Kho ngoại thất - Vị trí 1', 'Để trên bệ gỗ chuyên dụng', NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-008'), 0, 'Tủ kính C - Tầng 1 - Ô 1', NULL, NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-009'), 0, 'Tủ kính C - Tầng 1 - Ô 2', 'Cuộn tròn trong hộp nhựa cứng', NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-010'), 0, 'Kệ đá - Ô 1',               NULL, NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-011'), 0, 'Kệ đá - Ô 2',               NULL, NOW()),
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-012'), 0, 'Tủ vải chuyên dụng - Ô 1', 'Bảo quản trong túi chống ẩm', NOW());

-- ============================================================
-- 4. PHIẾU NHẬP (dùng subquery lấy user_id theo username)
-- ============================================================
INSERT INTO import_receipts (ma_phieu_nhap, ngay_nhap, nguon_goc, tong_gia_tri, ghi_chu, trang_thai, user_id, created_at) VALUES
('PN-202601-001', '2026-01-10', 'Đấu giá Christie Việt Nam',          125000000, 'Mua 3 món gốm, kiểm tra kỹ trước khi nhập', 'NHAP_KHO', (SELECT id FROM users WHERE username='admin'), '2026-01-10 09:00:00'),
('PN-202601-002', '2026-01-20', 'Thu mua gia đình cụ Trần Hữu',       195000000, 'Mua bộ kim loại và kiếm',                   'NHAP_KHO', (SELECT id FROM users WHERE username='admin'), '2026-01-20 10:30:00'),
('PN-202602-001', '2026-02-05', 'Đại lý Hoa Phát - Hà Nội',           338000000, 'Nhập gỗ nội thất và thạch anh',             'NHAP_KHO', (SELECT id FROM users WHERE username='admin'), '2026-02-05 08:00:00'),
('PN-202603-001', '2026-03-15', 'Thu mua từ bộ sưu tập Nguyễn Công',  227000000, 'Nhập tranh, nghiên mực, áo long bào',       'NHAP_KHO', (SELECT id FROM users WHERE username='admin'), '2026-03-15 14:00:00');

-- ============================================================
-- 5. IMPORT RECEIPT DETAILS (dùng subquery cho cả 2 FK)
-- ============================================================
INSERT INTO import_receipt_details (import_receipt_id, antique_id, so_luong, don_gia, thanh_tien, ghi_chu) VALUES
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202601-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-001'), 1, 45000000, 45000000, 'Bình gốm men ngọc, còn nguyên'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202601-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-002'), 1,  5000000,  5000000, 'Chén Bát Tràng, sứt nhỏ ở miệng'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202601-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-003'), 1, 75000000, 75000000, 'Lọ sứ Trung Hoa, mua qua đấu giá'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202601-002'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-004'), 1, 25000000, 25000000, 'Tượng đồng, còn sắc nét'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202601-002'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-005'), 1, 18000000, 18000000, 'Đồng hồ Pháp, máy còn chạy'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202601-002'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-006'), 1, 55000000, 55000000, 'Kiếm có bao da nguyên bản'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202602-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-007'), 1,120000000,120000000, 'Sập gụ cỡ lớn, vận chuyển riêng'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202602-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-008'), 1,  3000000,  3000000, 'Tranh Đông Hồ, màu phai nhẹ'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202602-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-011'), 1, 35000000, 35000000, 'Tượng thạch anh nhập từ Hoa Phát'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202603-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-009'), 1, 15000000, 15000000, 'Thư pháp lụa, khung gỗ nguyên bản'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202603-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-010'), 1, 12000000, 12000000, 'Nghiên mực đá Đoan Khê'),
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202603-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-012'), 1,200000000,200000000, 'Áo long bào 5 móng, bảo quản tốt');

-- Cập nhật tồn kho sau phiếu nhập (dùng subquery cho antique_id)
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-01-10 09:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-001');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-01-10 09:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-002');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-01-10 09:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-003');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-01-20 10:45:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-004');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-01-20 10:45:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-005');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-01-20 10:45:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-006');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-02-05 08:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-007');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-02-05 08:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-008');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-02-05 08:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-011');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-03-15 14:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-009');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-03-15 14:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-010');
UPDATE inventory SET so_luong_ton = 1, updated_at = '2026-03-15 14:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-012');

-- ============================================================
-- 6. PHIẾU XUẤT
-- ============================================================
INSERT INTO export_receipts (ma_phieu_xuat, ngay_xuat, ly_do, nguoi_nhan, tong_gia_tri, ghi_chu, trang_thai, user_id, created_at) VALUES
('PX-202602-001', '2026-02-20', 'BAN',      'Anh Lê Minh Tuấn - Hà Nội',       120000000, 'Bán lọ sứ Trung Hoa',              'XUAT_KHO', (SELECT id FROM users WHERE username='admin'), '2026-02-20 11:00:00'),
('PX-202603-001', '2026-03-01', 'CHO_MUON', 'Bảo tàng Lịch sử Quốc gia',                 0, 'Cho mượn trưng bày 2 tháng',   'HUY',      (SELECT id FROM users WHERE username='admin'), '2026-03-01 09:00:00'),
('PX-202604-001', '2026-04-01', 'BAN',      'Bà Nguyễn Thị Hoa - TP.HCM',       45000000, 'Bán tượng thạch anh Quan Âm',  'XUAT_KHO', (SELECT id FROM users WHERE username='admin'), '2026-04-01 10:00:00');

-- ============================================================
-- 7. EXPORT RECEIPT DETAILS
-- ============================================================
INSERT INTO export_receipt_details (export_receipt_id, antique_id, so_luong, don_gia, thanh_tien, ghi_chu) VALUES
((SELECT id FROM export_receipts WHERE ma_phieu_xuat='PX-202602-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-003'), 1, 120000000, 120000000, 'Lọ sứ Trung Hoa, bán trực tiếp'),
((SELECT id FROM export_receipts WHERE ma_phieu_xuat='PX-202603-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-004'), 1,         0,         0, 'Cho mượn tạm - không tính phí'),
((SELECT id FROM export_receipts WHERE ma_phieu_xuat='PX-202604-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-011'), 1,  45000000,  45000000, 'Khách đã thanh toán đủ');

-- Cập nhật tồn kho sau xuất
-- DC-2024-003 (lọ sứ): đã bán → 0
UPDATE inventory SET so_luong_ton = 0, updated_at = '2026-02-20 11:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-003');
-- DC-2024-004 (tượng phật): cho mượn rồi HUY → tồn kho giữ nguyên = 1
-- DC-2024-011 (tượng thạch anh): đã bán → 0
UPDATE inventory SET so_luong_ton = 0, updated_at = '2026-04-01 10:30:00' WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-011');

-- ============================================================
-- 8. WAREHOUSE ACTIVITIES
-- ============================================================
INSERT INTO warehouse_activities (loai_hoat_dong, mo_ta, tham_chieu, so_luong_thay_doi, gia_tri, antique_id, user_id, created_at) VALUES
('NHAP_KHO', 'Nhập 1 Bình gốm men ngọc triều Lý theo PN-202601-001',          'PN-202601-001',  1,  45000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-001'), (SELECT id FROM users WHERE username='admin'), '2026-01-10 09:30:00'),
('NHAP_KHO', 'Nhập 1 Chén bát tràng thế kỷ XVIII theo PN-202601-001',          'PN-202601-001',  1,   5000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-002'), (SELECT id FROM users WHERE username='admin'), '2026-01-10 09:30:00'),
('NHAP_KHO', 'Nhập 1 Lọ hoa sứ Trung Hoa theo PN-202601-001',                  'PN-202601-001',  1,  75000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-003'), (SELECT id FROM users WHERE username='admin'), '2026-01-10 09:30:00'),
('NHAP_KHO', 'Nhập 1 Tượng phật đồng thau tọa thiền theo PN-202601-002',       'PN-202601-002',  1,  25000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-004'), (SELECT id FROM users WHERE username='admin'), '2026-01-20 10:45:00'),
('NHAP_KHO', 'Nhập 1 Đồng hồ đồng phong cách Pháp theo PN-202601-002',         'PN-202601-002',  1,  18000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-005'), (SELECT id FROM users WHERE username='admin'), '2026-01-20 10:45:00'),
('NHAP_KHO', 'Nhập 1 Kiếm thép triều Nguyễn theo PN-202601-002',               'PN-202601-002',  1,  55000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-006'), (SELECT id FROM users WHERE username='admin'), '2026-01-20 10:45:00'),
('NHAP_KHO', 'Nhập 1 Sập gụ chân quỳ thế kỷ XIX theo PN-202602-001',           'PN-202602-001',  1, 120000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-007'), (SELECT id FROM users WHERE username='admin'), '2026-02-05 08:30:00'),
('NHAP_KHO', 'Nhập 1 Tranh khắc gỗ Đông Hồ theo PN-202602-001',               'PN-202602-001',  1,   3000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-008'), (SELECT id FROM users WHERE username='admin'), '2026-02-05 08:30:00'),
('NHAP_KHO', 'Nhập 1 Tượng thạch anh Quan Âm theo PN-202602-001',              'PN-202602-001',  1,  35000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-011'), (SELECT id FROM users WHERE username='admin'), '2026-02-05 08:30:00'),
('NHAP_KHO', 'Nhập 1 Thư pháp chữ Hán trên lụa theo PN-202603-001',            'PN-202603-001',  1,  15000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-009'), (SELECT id FROM users WHERE username='admin'), '2026-03-15 14:30:00'),
('NHAP_KHO', 'Nhập 1 Nghiên mực đá mài Trung Quốc theo PN-202603-001',         'PN-202603-001',  1,  12000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-010'), (SELECT id FROM users WHERE username='admin'), '2026-03-15 14:30:00'),
('NHAP_KHO', 'Nhập 1 Áo long bào thêu rồng hoàng gia theo PN-202603-001',      'PN-202603-001',  1, 200000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-012'), (SELECT id FROM users WHERE username='admin'), '2026-03-15 14:30:00'),
('XUAT_KHO', 'Xuất 1 Lọ hoa sứ Trung Hoa theo PX-202602-001',                  'PX-202602-001', -1, 120000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-003'), (SELECT id FROM users WHERE username='admin'), '2026-02-20 11:30:00'),
('XUAT_KHO', 'Xuất 1 Tượng phật đồng cho mượn bảo tàng theo PX-202603-001',   'PX-202603-001', -1,         0, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-004'), (SELECT id FROM users WHERE username='admin'), '2026-03-01 09:30:00'),
('HUY_XUAT', 'Hủy phiếu xuất PX-202603-001 - hoàn lại tượng phật đồng',        'PX-202603-001',  1,         0, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-004'), (SELECT id FROM users WHERE username='admin'), '2026-03-10 14:00:00'),
('XUAT_KHO', 'Xuất 1 Tượng thạch anh Quan Âm theo PX-202604-001',              'PX-202604-001', -1,  45000000, (SELECT id FROM antiques WHERE ma_do_co='DC-2024-011'), (SELECT id FROM users WHERE username='admin'), '2026-04-01 10:30:00');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- KIỂM TRA KẾT QUẢ (kết quả mong đợi ghi bên cạnh)
-- ============================================================
SELECT 'categories'          AS bang, COUNT(*) AS so_luong, '6'  AS mong_doi FROM categories
UNION ALL SELECT 'antiques',          COUNT(*), '12' FROM antiques
UNION ALL SELECT 'inventory',         COUNT(*), '12' FROM inventory
UNION ALL SELECT 'import_receipts',   COUNT(*), '4'  FROM import_receipts
UNION ALL SELECT 'import_details',    COUNT(*), '12' FROM import_receipt_details
UNION ALL SELECT 'export_receipts',   COUNT(*), '3'  FROM export_receipts
UNION ALL SELECT 'export_details',    COUNT(*), '3'  FROM export_receipt_details
UNION ALL SELECT 'warehouse_activities',COUNT(*),'16' FROM warehouse_activities;

-- ============================================================
-- 9. PHIẾU NHẬP BỊ HUY + activity HUY_NHAP (demo đủ enum)
-- ============================================================
-- Nhân viên tạo phiếu nhập nhưng bị hủy vì hàng không đạt yêu cầu
INSERT INTO import_receipts (ma_phieu_nhap, ngay_nhap, nguon_goc, tong_gia_tri, ghi_chu, trang_thai, user_id, created_at) VALUES
('PN-202602-002', '2026-02-15', 'Cửa hàng đồ cổ phố cổ Hà Nội', 50000000, 'Hàng kiểm tra không đạt - hủy phiếu', 'HUY', (SELECT id FROM users WHERE username='nhanvien1'), '2026-02-15 11:00:00');

-- Chi tiết phiếu nhập bị hủy (nhập vào bình gốm thứ 2 nhưng hủy)
INSERT INTO import_receipt_details (import_receipt_id, antique_id, so_luong, don_gia, thanh_tien, ghi_chu) VALUES
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202602-002'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-002'), 1, 50000000, 50000000, 'Bị hủy - phát hiện hàng giả');

-- Activity HUY_NHAP
INSERT INTO warehouse_activities (loai_hoat_dong, mo_ta, tham_chieu, so_luong_thay_doi, gia_tri, antique_id, user_id, created_at) VALUES
('HUY_NHAP', 'Hủy phiếu nhập PN-202602-002 - hàng kiểm tra không đạt yêu cầu', 'PN-202602-002', -1, 50000000,
 (SELECT id FROM antiques WHERE ma_do_co='DC-2024-002'),
 (SELECT id FROM users WHERE username='nhanvien1'),
 '2026-02-15 14:00:00');

-- ============================================================
-- 10. PHIẾU NHẬP/XUẤT DO NHÂN VIÊN TẠO (demo phân quyền)
-- ============================================================
-- Phiếu nhập do nhanvien1 tạo
INSERT INTO import_receipts (ma_phieu_nhap, ngay_nhap, nguon_goc, tong_gia_tri, ghi_chu, trang_thai, user_id, created_at) VALUES
('PN-202604-001', '2026-04-02', 'Thu mua từ triển lãm đồ cổ TP.HCM', 97000000, 'Nhân viên thu mua tại triển lãm', 'NHAP_KHO', (SELECT id FROM users WHERE username='nhanvien1'), '2026-04-02 09:00:00');

-- Chi tiết: nhập lại chén bát tràng + nghiên mực (hàng còn trong kho, số lượng tăng thêm)
INSERT INTO import_receipt_details (import_receipt_id, antique_id, so_luong, don_gia, thanh_tien, ghi_chu) VALUES
((SELECT id FROM import_receipts WHERE ma_phieu_nhap='PN-202604-001'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-005'), 1, 97000000, 97000000, 'Đồng hồ Pháp thu mua thêm - máy còn chạy');

-- Cập nhật tồn kho
UPDATE inventory SET so_luong_ton = 2, updated_at = '2026-04-02 09:30:00'
WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-005');

-- Activity nhập kho do nhanvien1
INSERT INTO warehouse_activities (loai_hoat_dong, mo_ta, tham_chieu, so_luong_thay_doi, gia_tri, antique_id, user_id, created_at) VALUES
('NHAP_KHO', 'Nhân viên nhập 1 Đồng hồ đồng Pháp từ triển lãm theo PN-202604-001', 'PN-202604-001', 1, 97000000,
 (SELECT id FROM antiques WHERE ma_do_co='DC-2024-005'),
 (SELECT id FROM users WHERE username='nhanvien1'),
 '2026-04-02 09:30:00');

-- Phiếu xuất do nhanvien1 tạo
INSERT INTO export_receipts (ma_phieu_xuat, ngay_xuat, ly_do, nguoi_nhan, tong_gia_tri, ghi_chu, trang_thai, user_id, created_at) VALUES
('PX-202604-002', '2026-04-04', 'DIEU_CHUYEN', 'Chi nhánh 2 - Đà Nẵng', 0, 'Điều chuyển tranh Đông Hồ sang chi nhánh Đà Nẵng', 'XUAT_KHO', (SELECT id FROM users WHERE username='nhanvien1'), '2026-04-04 10:00:00');

INSERT INTO export_receipt_details (export_receipt_id, antique_id, so_luong, don_gia, thanh_tien, ghi_chu) VALUES
((SELECT id FROM export_receipts WHERE ma_phieu_xuat='PX-202604-002'), (SELECT id FROM antiques WHERE ma_do_co='DC-2024-008'), 1, 0, 0, 'Điều chuyển nội bộ - không tính tiền');

UPDATE inventory SET so_luong_ton = 0, updated_at = '2026-04-04 10:30:00'
WHERE antique_id = (SELECT id FROM antiques WHERE ma_do_co='DC-2024-008');

INSERT INTO warehouse_activities (loai_hoat_dong, mo_ta, tham_chieu, so_luong_thay_doi, gia_tri, antique_id, user_id, created_at) VALUES
('XUAT_KHO', 'Nhân viên xuất 1 Tranh khắc gỗ Đông Hồ điều chuyển Đà Nẵng theo PX-202604-002', 'PX-202604-002', -1, 0,
 (SELECT id FROM antiques WHERE ma_do_co='DC-2024-008'),
 (SELECT id FROM users WHERE username='nhanvien1'),
 '2026-04-04 10:30:00');

-- ============================================================
-- 11. ANTIQUE INSPECTIONS (demo 3 trạng thái)
-- ============================================================
-- 1. CHO_KIEM_TRA: bình gốm tồn kho lâu (tự động tạo bởi scheduled job - giả lập)
INSERT INTO antique_inspections (antique_id, ly_do_kiem_tra, so_ngay_ton_kho, trang_thai, mo_ta, ket_qua_kiem_tra, nguoi_kiem_tra_id, ngay_kiem_tra, export_receipt_id, created_at) VALUES
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-001'), 'TON_KHO_LAU', 87, 'CHO_KIEM_TRA',
 'Đồ cổ tồn kho 87 ngày chưa có hoạt động xuất kho. Đề nghị kiểm tra tình trạng bảo quản.',
 NULL, NULL, NULL, NULL, '2026-04-06 08:00:00'),

-- 2. DA_KIEM_TRA: kiếm triều Nguyễn tồn kho lâu, đã kiểm tra xong
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-006'), 'TON_KHO_LAU', 75, 'DA_KIEM_TRA',
 'Đồ cổ tồn kho 75 ngày. Đề nghị kiểm tra tình trạng bảo quản.',
 'Kiểm tra ngày 2026-04-01: Kiếm còn tốt, bao da nguyên bản, chuôi đồng không bị rỉ. Bảo quản đúng quy trình.',
 (SELECT id FROM users WHERE username='nhanvien1'), '2026-04-01 10:00:00', NULL, '2026-03-25 08:00:00'),

-- 3. BO_QUA: tượng phật vừa được trả lại sau cho mượn (hủy PX-202603-001), đã bỏ qua kiểm tra
((SELECT id FROM antiques WHERE ma_do_co='DC-2024-004'), 'SAU_CHO_MUON', NULL, 'BO_QUA',
 'Đồ cổ vừa được trả lại sau khi cho mượn bảo tàng. Đề nghị kiểm tra tình trạng.',
 NULL, NULL, NULL, (SELECT id FROM export_receipts WHERE ma_phieu_xuat='PX-202603-001'), '2026-03-10 14:00:00');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- KIỂM TRA KẾT QUẢ (kết quả mong đợi ghi bên cạnh)
-- ============================================================
SELECT 'categories'           AS bang, COUNT(*) AS so_luong, '6'  AS mong_doi FROM categories
UNION ALL SELECT 'antiques',           COUNT(*), '12' FROM antiques
UNION ALL SELECT 'inventory',          COUNT(*), '12' FROM inventory
UNION ALL SELECT 'import_receipts',    COUNT(*), '6'  FROM import_receipts
UNION ALL SELECT 'import_details',     COUNT(*), '14' FROM import_receipt_details
UNION ALL SELECT 'export_receipts',    COUNT(*), '5'  FROM export_receipts
UNION ALL SELECT 'export_details',     COUNT(*), '5'  FROM export_receipt_details
UNION ALL SELECT 'warehouse_activities',COUNT(*),'19' FROM warehouse_activities
UNION ALL SELECT 'antique_inspections', COUNT(*),'3'  FROM antique_inspections;

-- Kiểm tra tồn kho
SELECT a.ma_do_co, a.ten_do_co,
       i.so_luong_ton,
       CASE WHEN i.so_luong_ton > 0 THEN 'Con hang' ELSE 'Het hang' END AS trang_thai
FROM inventory i
JOIN antiques a ON i.antique_id = a.id
ORDER BY a.ma_do_co;
